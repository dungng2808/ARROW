package org.arrow.analyzer;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.Position;
import com.github.javaparser.Range;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.CallableDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.comments.Comment;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.BooleanLiteralExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MemberValuePair;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import com.github.javaparser.ast.expr.NullLiteralExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.stmt.TryStmt;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

/** Implementation of the analyzer commands. Pure functions over source text. */
final class Commands {

    private static final Set<String> TEST_ANNOTATIONS =
            new HashSet<>(Arrays.asList("Test", "ParameterizedTest", "RepeatedTest", "TestFactory", "TestTemplate"));

    private Commands() {
    }

    // ── parsing ─────────────────────────────────────────────────────────

    static Optional<CompilationUnit> parse(String source) {
        ParserConfiguration configuration = new ParserConfiguration()
                .setLanguageLevel(ParserConfiguration.LanguageLevel.JAVA_17)
                .setAttributeComments(true);
        ParseResult<CompilationUnit> result = new JavaParser(configuration).parse(source);
        return result.isSuccessful() ? result.getResult() : Optional.empty();
    }

    private static String text(JsonObject request, String name) {
        JsonElement value = request.get(name);
        return value == null || value.isJsonNull() ? "" : value.getAsString();
    }

    private static List<String> strings(JsonObject request, String name) {
        List<String> values = new ArrayList<>();
        JsonElement value = request.get(name);
        if (value != null && value.isJsonArray()) {
            for (JsonElement element : value.getAsJsonArray()) {
                values.add(element.getAsString());
            }
        }
        return values;
    }

    // ── analyze_test: everything the verifier needs in one JVM start ───

    static Map<String, Object> analyzeTest(JsonObject request) {
        String source = text(request, "source");
        Map<String, Object> result = new LinkedHashMap<>();
        Optional<CompilationUnit> unit = parse(source);
        result.put("parsed", unit.isPresent());
        result.put("stripped", unit.map(u -> stripComments(source, u)).orElseGet(() -> Lexer.stripComments(source)));
        if (unit.isPresent()) {
            result.putAll(assertionCounts(unit.get()));
            result.put("test_methods", testMethods(unit.get()));
            List<Integer> lines = new ArrayList<>();
            JsonElement value = request.get("lines");
            if (value != null && value.isJsonArray()) {
                for (JsonElement element : value.getAsJsonArray()) {
                    lines.add(element.getAsInt());
                }
            }
            result.put("methods_at_lines", methodsAtLines(unit.get(), lines));
        } else {
            result.put("assertion_count", null);
            result.put("assertions_per_method", new LinkedHashMap<>());
            result.put("test_methods", new ArrayList<>());
            result.put("methods_at_lines", new LinkedHashMap<>());
        }
        return result;
    }

    // ── strip_comments (specs/03 Mục 7) ───────────────────────────────

    static Map<String, Object> stripComments(JsonObject request) {
        String source = text(request, "source");
        Optional<CompilationUnit> unit = parse(source);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("parsed", unit.isPresent());
        result.put("code", unit.map(u -> stripComments(source, u)).orElseGet(() -> Lexer.stripComments(source)));
        return result;
    }

    static String stripComments(String source, CompilationUnit unit) {
        LineIndex index = new LineIndex(source);
        List<int[]> spans = new ArrayList<>();
        for (Comment comment : unit.getAllComments()) {
            comment.getRange().ifPresent(r -> spans.add(new int[] {index.offset(r.begin), index.offset(r.end) + 1}));
        }
        spans.sort(Comparator.comparingInt(s -> s[0]));
        StringBuilder builder = new StringBuilder();
        int cursor = 0;
        for (int[] span : spans) {
            if (span[0] < cursor) {
                continue;
            }
            builder.append(source, cursor, span[0]).append(' ');
            cursor = Math.min(span[1], source.length());
        }
        builder.append(source.substring(cursor));
        return builder.toString();
    }

    // ── count_assertions (specs/03 Mục 8) ─────────────────────────────

    static Map<String, Object> countAssertions(JsonObject request) {
        Optional<CompilationUnit> unit = parse(text(request, "source"));
        if (!unit.isPresent()) {
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("parsed", false);
            result.put("assertion_count", null);
            return result;
        }
        Map<String, Object> result = assertionCounts(unit.get());
        result.put("parsed", true);
        return result;
    }

    static Map<String, Object> assertionCounts(CompilationUnit unit) {
        Map<String, Integer> perMethod = new TreeMap<>();
        int total = 0;
        for (MethodDeclaration method : unit.findAll(MethodDeclaration.class)) {
            int count = countIn(method);
            total += count;
            if (isTest(method)) {
                perMethod.put(method.getNameAsString(), perMethod.getOrDefault(method.getNameAsString(), 0) + count);
            }
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("assertion_count", total);
        result.put("assertions_per_method", perMethod);
        return result;
    }

    static int countIn(MethodDeclaration method) {
        int count = 0;
        for (AnnotationExpr annotation : method.getAnnotations()) {
            if (annotation.getNameAsString().endsWith("Test") && annotation instanceof NormalAnnotationExpr) {
                for (MemberValuePair pair : ((NormalAnnotationExpr) annotation).getPairs()) {
                    if (pair.getNameAsString().equals("expected")) {
                        count++;
                    }
                }
            }
        }
        for (MethodCallExpr call : method.findAll(MethodCallExpr.class)) {
            if (isCountedAssertion(call)) {
                count++;
            }
        }
        return count;
    }

    static boolean isCountedAssertion(MethodCallExpr call) {
        String name = call.getNameAsString();
        int arguments = call.getArguments().size();
        if (name.startsWith("assert") && !isRestAssuredChain(call)) {
            return arguments > 0 && !isTrivial(call);
        }
        if (name.equals("fail") && call.findAncestor(TryStmt.class).map(t -> isInside(call, t.getTryBlock())).orElse(false)) {
            return true;
        }
        if (name.equals("verify") && arguments > 0
                && (!call.getScope().isPresent() || call.getScope().get().toString().endsWith("Mockito"))) {
            return true;
        }
        return isRestAssuredChain(call) && arguments > 0 && !name.equals("extract");
    }

    private static boolean isInside(Node node, Node container) {
        for (Optional<Node> current = Optional.of(node); current.isPresent(); current = current.get().getParentNode()) {
            if (current.get() == container) {
                return true;
            }
        }
        return false;
    }

    /** Call made on a RestAssured response chain after {@code then()} / {@code expect()} (both without arguments). */
    static boolean isRestAssuredChain(MethodCallExpr call) {
        Optional<Expression> scope = call.getScope();
        while (scope.isPresent() && scope.get() instanceof MethodCallExpr) {
            MethodCallExpr parent = (MethodCallExpr) scope.get();
            String name = parent.getNameAsString();
            if (name.equals("extract")) {
                return false;
            }
            if ((name.equals("then") || name.equals("expect")) && parent.getArguments().isEmpty()) {
                return true;
            }
            scope = parent.getScope();
        }
        return false;
    }

    static boolean isTrivial(MethodCallExpr call) {
        List<Expression> args = call.getArguments();
        boolean oneOrWithMessage = args.size() == 1 || args.size() == 2;
        switch (call.getNameAsString()) {
            case "assertTrue":
                return oneOrWithMessage && args.stream().anyMatch(a -> isBoolean(a, true));
            case "assertFalse":
                return oneOrWithMessage && args.stream().anyMatch(a -> isBoolean(a, false));
            case "assertNull":
                return oneOrWithMessage && args.stream().anyMatch(a -> a instanceof NullLiteralExpr);
            case "assertNotNull":
                return oneOrWithMessage && args.stream().anyMatch(a -> a instanceof ObjectCreationExpr);
            case "assertEquals":
            case "assertSame":
                if (args.size() == 2) {
                    return args.get(0).equals(args.get(1));
                }
                // JUnit 4: (message, expected, actual); JUnit 5: (expected, actual, message).
                return args.size() == 3 && (args.get(1).equals(args.get(2)) || args.get(0).equals(args.get(1)));
            default:
                return false;
        }
    }

    private static boolean isBoolean(Expression expression, boolean value) {
        return expression instanceof BooleanLiteralExpr && ((BooleanLiteralExpr) expression).getValue() == value;
    }

    static boolean isTest(MethodDeclaration method) {
        return method.getAnnotations().stream().anyMatch(a -> TEST_ANNOTATIONS.contains(simple(a.getNameAsString())));
    }

    private static String simple(String name) {
        int dot = name.lastIndexOf('.');
        return dot >= 0 ? name.substring(dot + 1) : name;
    }

    static List<String> testMethods(CompilationUnit unit) {
        return unit.findAll(MethodDeclaration.class).stream().filter(Commands::isTest)
                .map(MethodDeclaration::getNameAsString).distinct().collect(Collectors.toList());
    }

    static Map<String, Object> listTestMethods(JsonObject request) {
        Optional<CompilationUnit> unit = parse(text(request, "source"));
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("parsed", unit.isPresent());
        result.put("test_methods", unit.map(Commands::testMethods).orElseGet(ArrayList::new));
        return result;
    }

    // ── method_at_line ─────────────────────────────────────────────────

    static Map<String, Object> methodAtLine(JsonObject request) {
        Optional<CompilationUnit> unit = parse(text(request, "source"));
        List<Integer> lines = new ArrayList<>();
        for (JsonElement element : request.getAsJsonArray("lines")) {
            lines.add(element.getAsInt());
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("parsed", unit.isPresent());
        result.put("methods_at_lines", unit.map(u -> methodsAtLines(u, lines)).orElseGet(LinkedHashMap::new));
        return result;
    }

    static Map<String, String> methodsAtLines(CompilationUnit unit, List<Integer> lines) {
        Map<String, String> result = new LinkedHashMap<>();
        for (int line : lines) {
            String name = null;
            int best = Integer.MAX_VALUE;
            for (CallableDeclaration<?> callable : unit.findAll(CallableDeclaration.class)) {
                Optional<Range> range = callable.getRange();
                if (range.isPresent() && range.get().begin.line <= line && line <= range.get().end.line) {
                    int size = range.get().end.line - range.get().begin.line;
                    if (size < best) {
                        best = size;
                        name = callable.getNameAsString();
                    }
                }
            }
            result.put(String.valueOf(line), name);
        }
        return result;
    }

    // ── cut_api (specs/04 Mục 6.2, COMPILE mức 2) ─────────────────────

    static Map<String, Object> cutApi(JsonObject request) {
        List<String> sources = new ArrayList<>();
        sources.add(text(request, "source"));
        sources.addAll(strings(request, "related_sources"));
        List<Map<String, Object>> types = new ArrayList<>();
        for (String source : sources) {
            parse(source).ifPresent(unit -> {
                for (TypeDeclaration<?> type : unit.findAll(TypeDeclaration.class)) {
                    Map<String, Object> entry = new LinkedHashMap<>();
                    entry.put("name", type.getFullyQualifiedName().orElse(type.getNameAsString()));
                    entry.put("kind", kindOf(type));
                    List<String> members = new ArrayList<>();
                    for (BodyDeclaration<?> member : type.getMembers()) {
                        if (member instanceof ConstructorDeclaration && !((ConstructorDeclaration) member).isPrivate()) {
                            members.add(signature((ConstructorDeclaration) member));
                        } else if (member instanceof MethodDeclaration && !((MethodDeclaration) member).isPrivate()) {
                            members.add(signature((MethodDeclaration) member));
                        } else if (member instanceof FieldDeclaration && ((FieldDeclaration) member).isPublic()) {
                            FieldDeclaration field = (FieldDeclaration) member;
                            for (VariableDeclarator variable : field.getVariables()) {
                                StringBuilder line = new StringBuilder();
                                field.getModifiers().forEach(m -> line.append(m.getKeyword().asString()).append(' '));
                                members.add(line + variable.getTypeAsString() + " " + variable.getNameAsString());
                            }
                        }
                    }
                    entry.put("members", members);
                    types.add(entry);
                }
            });
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("types", types);
        return result;
    }

    private static String kindOf(TypeDeclaration<?> type) {
        if (type.isClassOrInterfaceDeclaration()) {
            return type.asClassOrInterfaceDeclaration().isInterface() ? "interface" : "class";
        }
        if (type.isEnumDeclaration()) {
            return "enum";
        }
        if (type.isRecordDeclaration()) {
            return "record";
        }
        return "annotation";
    }

    static String signature(CallableDeclaration<?> callable) {
        StringBuilder builder = new StringBuilder();
        callable.getModifiers().forEach(m -> builder.append(m.getKeyword().asString()).append(' '));
        if (callable instanceof MethodDeclaration) {
            MethodDeclaration method = (MethodDeclaration) callable;
            if (!method.getTypeParameters().isEmpty()) {
                builder.append('<').append(method.getTypeParameters().stream().map(Node::toString).collect(Collectors.joining(", "))).append("> ");
            }
            builder.append(method.getTypeAsString()).append(' ');
        }
        builder.append(callable.getNameAsString()).append('(');
        builder.append(callable.getParameters().stream().map(Parameter::toString).collect(Collectors.joining(", ")));
        builder.append(')');
        if (!callable.getThrownExceptions().isEmpty()) {
            builder.append(" throws ").append(callable.getThrownExceptions().stream().map(Node::toString).collect(Collectors.joining(", ")));
        }
        return builder.toString();
    }

    // ── method_source / called_methods (RUNTIME, ASSERTION mức 2) ─────

    static Map<String, Object> methodSource(JsonObject request) {
        String source = text(request, "source");
        Set<String> names = new LinkedHashSet<>(strings(request, "names"));
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("methods", parse(source).map(u -> sourcesOf(source, u, names)).orElseGet(ArrayList::new));
        return result;
    }

    static List<String> sourcesOf(String source, CompilationUnit unit, Set<String> names) {
        LineIndex index = new LineIndex(source);
        List<String> methods = new ArrayList<>();
        for (CallableDeclaration<?> callable : unit.findAll(CallableDeclaration.class)) {
            boolean wanted = names.contains(callable.getNameAsString())
                    || callable instanceof ConstructorDeclaration && names.contains("<init>");
            if (wanted && callable.getRange().isPresent()) {
                methods.add(index.slice(callable.getRange().get()));
            }
        }
        return methods;
    }

    /** Methods of the CUT called from the given test methods, plus CUT constructors and field declarations. */
    static Map<String, Object> calledMethods(JsonObject request) {
        String testSource = text(request, "test_source");
        String cutSource = text(request, "cut_source");
        Set<String> testMethods = new HashSet<>(strings(request, "test_methods"));
        Map<String, Object> result = new LinkedHashMap<>();
        Optional<CompilationUnit> test = parse(testSource);
        Optional<CompilationUnit> cut = parse(cutSource);
        if (!test.isPresent() || !cut.isPresent()) {
            result.put("called", new ArrayList<>());
            result.put("methods", new ArrayList<>());
            result.put("constructors", new ArrayList<>());
            result.put("fields", new ArrayList<>());
            return result;
        }
        Set<String> cutMethodNames = cut.get().findAll(MethodDeclaration.class).stream()
                .map(MethodDeclaration::getNameAsString).collect(Collectors.toCollection(LinkedHashSet::new));
        Set<String> called = new LinkedHashSet<>();
        for (MethodDeclaration method : test.get().findAll(MethodDeclaration.class)) {
            if (testMethods.isEmpty() || testMethods.contains(method.getNameAsString())) {
                for (MethodCallExpr call : method.findAll(MethodCallExpr.class)) {
                    if (cutMethodNames.contains(call.getNameAsString())) {
                        called.add(call.getNameAsString());
                    }
                }
            }
        }
        LineIndex index = new LineIndex(cutSource);
        result.put("called", new ArrayList<>(called));
        result.put("methods", sourcesOf(cutSource, cut.get(), called));
        result.put("constructors", cut.get().findAll(ConstructorDeclaration.class).stream()
                .filter(c -> c.getRange().isPresent()).map(c -> index.slice(c.getRange().get())).collect(Collectors.toList()));
        result.put("fields", cut.get().findAll(FieldDeclaration.class).stream()
                .filter(f -> f.getRange().isPresent()).map(f -> index.slice(f.getRange().get())).collect(Collectors.toList()));
        return result;
    }

    // ── splice_methods (specs/05 Mục 5.3) ─────────────────────────────

    static Map<String, Object> spliceMethods(JsonObject request) {
        String checkpoint = text(request, "checkpoint");
        String response = text(request, "response");
        Set<String> failing = new LinkedHashSet<>(strings(request, "methods"));
        Optional<CompilationUnit> base = parse(checkpoint);
        Optional<CompilationUnit> incoming = parse(response);
        Map<String, Object> result = new LinkedHashMap<>();
        if (!base.isPresent() || !incoming.isPresent()) {
            result.put("error", !base.isPresent() ? "checkpoint does not parse" : "response does not parse");
            return result;
        }
        return Splicer.splice(checkpoint, base.get(), response, incoming.get(), failing);
    }

    /** Line/column → offset conversion for the text the AST was parsed from. */
    static final class LineIndex {
        private final String text;
        private final List<Integer> starts = new ArrayList<>();

        LineIndex(String text) {
            this.text = text;
            starts.add(0);
            for (int i = 0; i < text.length(); i++) {
                if (text.charAt(i) == '\n') {
                    starts.add(i + 1);
                }
            }
        }

        int offset(Position position) {
            int line = Math.max(1, Math.min(position.line, starts.size()));
            return Math.min(text.length(), starts.get(line - 1) + position.column - 1);
        }

        String slice(Range range) {
            return text.substring(offset(range.begin), Math.min(text.length(), offset(range.end) + 1));
        }

        int lineStart(int line) {
            return starts.get(Math.max(0, Math.min(line, starts.size()) - 1));
        }
    }

    static JsonArray toJson(List<String> values) {
        JsonArray array = new JsonArray();
        values.forEach(array::add);
        return array;
    }

    static List<ImportDeclaration> imports(CompilationUnit unit) {
        return unit.getImports();
    }
}
