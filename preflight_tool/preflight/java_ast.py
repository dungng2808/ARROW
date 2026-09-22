from __future__ import annotations

import re
from dataclasses import dataclass, field
from pathlib import Path

from tree_sitter import Language, Parser
import tree_sitter_java


JAVA_LANGUAGE = Language(tree_sitter_java.language())


@dataclass(frozen=True)
class JavaApi:
    kind: str  # constructor | method
    name: str
    parameters: str
    is_static: bool
    signature: str


@dataclass
class JavaClass:
    package: str
    name: str
    kind: str
    modifiers: set[str]
    annotations: set[str]
    apis: list[JavaApi]
    imports: list[str]
    extends: str = ""
    source_set: str = "unknown"
    tags: set[str] = field(default_factory=set)
    is_top_level: bool = True

    @property
    def fqn(self) -> str:
        return f"{self.package}.{self.name}" if self.package else self.name


def _node_text(node, source: bytes) -> str:
    return source[node.start_byte:node.end_byte].decode("utf-8", errors="replace")


def _tokens(node, source: bytes) -> set[str]:
    return set(re.findall(r"@[A-Za-z_$][\w$.]*|\b(?:public|private|protected|static|abstract|final)\b", _node_text(node, source)))


def _first_named(node, node_type: str):
    return next((child for child in node.named_children if child.type == node_type), None)


def _source_set(path: str) -> str:
    normal = path.replace("\\", "/").lower()
    if "/src/test/" in f"/{normal}":
        return "test"
    if "/src/main/java/" in f"/{normal}" or "/src/main/kotlin/" in f"/{normal}":
        return "production"
    return "unknown"


def _parse_declaration(node, source: bytes) -> tuple[str, set[str], set[str]]:
    name = node.child_by_field_name("name")
    modifiers = _first_named(node, "modifiers")
    modifier_text = _node_text(modifiers, source) if modifiers else ""
    return (_node_text(name, source) if name else "", _tokens(modifiers, source) if modifiers else set(), set(re.findall(r"@([A-Za-z_$][\w$.]*)", modifier_text)))


def _declarations(node):
    for child in node.named_children:
        if child.type in {"class_declaration", "interface_declaration", "enum_declaration", "record_declaration", "annotation_type_declaration"}:
            yield child
        yield from _declarations(child)


def parse_java_source(source_text: str, source_path: str, *, expected_name: str | None = None) -> JavaClass | None:
    source = source_text.encode("utf-8")
    parser = Parser(JAVA_LANGUAGE)
    root = parser.parse(source).root_node
    package = ""
    imports: list[str] = []
    declaration = None
    for child in root.named_children:
        if child.type == "package_declaration":
            package = _node_text(child, source).removeprefix("package").rstrip(";").strip()
        elif child.type == "import_declaration":
            imports.append(_node_text(child, source).removeprefix("import").rstrip(";").strip())
        elif child.type in {"class_declaration", "interface_declaration", "enum_declaration", "record_declaration", "annotation_type_declaration"}:
            if expected_name is None:
                declaration = child
                break
    if expected_name is not None:
        declaration = next((node for node in _declarations(root) if _node_text(node.child_by_field_name("name"), source) == expected_name), None)
    if declaration is None:
        return None
    kind_map = {"class_declaration": "class", "interface_declaration": "interface", "enum_declaration": "enum", "record_declaration": "record", "annotation_type_declaration": "annotation"}
    name, modifiers, annotations = _parse_declaration(declaration, source)
    body = declaration.child_by_field_name("body")
    apis: list[JavaApi] = []
    if body:
        for child in body.named_children:
            if child.type not in {"method_declaration", "constructor_declaration"}:
                continue
            api_name, api_modifiers, _ = _parse_declaration(child, source)
            if "private" in api_modifiers:
                continue
            params = child.child_by_field_name("parameters")
            params_text = _node_text(params, source) if params else "()"
            api_kind = "constructor" if child.type == "constructor_declaration" else "method"
            apis.append(JavaApi(api_kind, api_name, params_text, "static" in api_modifiers, _node_text(child, source).split("{")[0].strip()))
    if kind_map[declaration.type] == "class" and not any(api.kind == "constructor" for api in apis):
        # Java grants a default constructor with the class's accessibility.
        apis.append(JavaApi("constructor", name, "()", False, f"{name}() [implicit default constructor]"))
    extends = ""
    superclass = declaration.child_by_field_name("superclass")
    if superclass:
        extends = _node_text(superclass, source).removeprefix("extends").strip()
    tags: set[str] = set()
    combined = source_text
    imports_text = "\n".join(imports)
    if re.search(r"\b(?:DataSource|JdbcTemplate|EntityManager)\b", combined) or re.search(r"\b(?:java\.sql|javax\.persistence|jakarta\.persistence|springframework\.jdbc)", imports_text):
        tags.add("DB_DEPENDENT")
    if re.search(r"\bDriverManager\s*\.\s*getConnection\s*\(", combined):
        tags.update({"DB_DEPENDENT", "DIRECT_CONNECTION"})
    if re.search(r"\b(?:java\.net|okhttp|apache\.http|HttpClient|WebClient)\b", combined):
        tags.add("EXTERNAL_DEPENDENT")
    if re.search(r"\b(?:java\.io|java\.nio\.file|FileInputStream|Files\.)", combined):
        tags.add("EXTERNAL_DEPENDENT")
    if ("DB_DEPENDENT" in tags or "EXTERNAL_DEPENDENT" in tags) and "DIRECT_CONNECTION" not in tags:
        tags.add("REQUIRES_MOCK")
    if "Generated" in annotations or any(item.endswith(".Generated") for item in imports) or "/generated/" in f"/{source_path.lower()}/" or re.search(r"\b(?:generated by|auto-generated|do not edit)\b", source_text, re.IGNORECASE):
        tags.add("GENERATED_SOURCE")
    if re.search(r"(?:Test|Tests|IT)$", name):
        tags.add("SUSPECT_TEST_NAME")
    accessible_methods = [item for item in apis if item.kind == "method"]
    if accessible_methods and all(item.is_static for item in accessible_methods):
        tags.add("STATIC_UTILITY")
    if "public" not in modifiers:
        tags.add("PACKAGE_PRIVATE")
    if not accessible_methods and any(item.kind == "constructor" for item in apis):
        tags.add("CONSTRUCTOR_ONLY")
    non_constructor = [item for item in accessible_methods if not re.match(r"(?:get|set|is)[A-Z]|(?:equals|hashCode|toString)$", item.name)]
    if not non_constructor:
        tags.add("LOW_LOGIC")
    is_top_level = declaration.parent == root
    return JavaClass(package, name, kind_map[declaration.type], modifiers, annotations, apis, imports, extends, _source_set(source_path), tags, is_top_level)


def primitive_arguments(parameters: str) -> str:
    """Emit compile-only values. `null` is safe because the probe is never run."""
    content = parameters.strip().removeprefix("(").removesuffix(")").strip()
    if not content:
        return ""
    chunks = [piece.strip() for piece in re.split(r",(?![^<]*>)", content)]
    values: list[str] = []
    for chunk in chunks:
        compact = re.sub(r"@[\w.]+(?:\([^)]*\))?", "", chunk).replace("final ", "").strip()
        type_part = compact.rsplit(" ", 1)[0] if " " in compact else compact
        normalized = type_part.replace("...", "[]").strip()
        values.append({"boolean": "false", "byte": "(byte) 0", "short": "(short) 0", "int": "0", "long": "0L", "float": "0F", "double": "0D", "char": "'\\0'"}.get(normalized, "null"))
    return ", ".join(values)
