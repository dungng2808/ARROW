package org.arrow.analyzer;

import com.github.javaparser.Range;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Level-3 splice (specs/05 Mục 5.3): failing methods of the checkpoint are replaced by their version in the
 * response, failing methods missing from the response are dropped, new imports, fields and helper methods of the
 * response are added. Every other character of the checkpoint is kept: edits are applied on the text by source range.
 */
final class Splicer {

    private Splicer() {
    }

    private static final class Edit {
        final int start;
        final int end;
        final String text;

        Edit(int start, int end, String text) {
            this.start = start;
            this.end = end;
            this.text = text;
        }
    }

    static Map<String, Object> splice(String checkpoint, CompilationUnit base, String response, CompilationUnit incoming, Set<String> failing) {
        Commands.LineIndex baseIndex = new Commands.LineIndex(checkpoint);
        Commands.LineIndex responseIndex = new Commands.LineIndex(response);
        TypeDeclaration<?> baseType = primaryType(base, null);
        TypeDeclaration<?> responseType = primaryType(incoming, baseType.getNameAsString());
        List<Edit> edits = new ArrayList<>();
        List<String> replaced = new ArrayList<>();
        List<String> dropped = new ArrayList<>();

        for (String name : failing) {
            List<MethodDeclaration> current = directMethods(baseType, name);
            if (current.isEmpty()) {
                continue;
            }
            List<MethodDeclaration> fresh = directMethods(responseType, name);
            for (int i = 0; i < current.size(); i++) {
                MethodDeclaration method = current.get(i);
                if (i == 0 && !fresh.isEmpty()) {
                    int start = baseIndex.offset(withComment(method).begin);
                    int end = baseIndex.offset(method.getRange().get().end) + 1;
                    String text = fresh.stream().map(m -> responseIndex.slice(withComment(m))).collect(Collectors.joining("\n\n" + indentOf(checkpoint, start)));
                    edits.add(new Edit(start, end, text));
                } else {
                    edits.add(removal(checkpoint, baseIndex, method));
                }
            }
            (fresh.isEmpty() ? dropped : replaced).add(name);
        }

        // New imports after the last import (or after the package declaration).
        Set<String> baseImports = base.getImports().stream().map(Splicer::importKey).collect(Collectors.toSet());
        List<String> addedImports = new ArrayList<>();
        for (ImportDeclaration declaration : incoming.getImports()) {
            if (!baseImports.contains(importKey(declaration))) {
                addedImports.add(responseIndex.slice(declaration.getRange().get()).trim());
            }
        }
        if (!addedImports.isEmpty()) {
            int at;
            if (!base.getImports().isEmpty()) {
                at = baseIndex.offset(base.getImports().get(base.getImports().size() - 1).getRange().get().end) + 1;
            } else if (base.getPackageDeclaration().isPresent()) {
                at = baseIndex.offset(base.getPackageDeclaration().get().getRange().get().end) + 1;
            } else {
                at = 0;
            }
            String block = addedImports.stream().map(i -> "\n" + i).collect(Collectors.joining());
            edits.add(new Edit(at, at, at == 0 ? block.substring(1) + "\n" : block));
        }

        // New fields and helper methods before the closing brace of the class.
        Set<String> baseFields = new HashSet<>();
        for (FieldDeclaration field : directMembers(baseType, FieldDeclaration.class)) {
            field.getVariables().forEach(v -> baseFields.add(v.getNameAsString()));
        }
        Set<String> baseSignatures = new HashSet<>();
        for (MethodDeclaration method : directMembers(baseType, MethodDeclaration.class)) {
            baseSignatures.add(method.getSignature().asString());
        }
        List<String> addedFields = new ArrayList<>();
        List<String> addedMethods = new ArrayList<>();
        List<String> additions = new ArrayList<>();
        for (BodyDeclaration<?> member : responseType.getMembers()) {
            if (member instanceof FieldDeclaration) {
                FieldDeclaration field = (FieldDeclaration) member;
                List<String> names = field.getVariables().stream().map(VariableDeclarator::getNameAsString).collect(Collectors.toList());
                if (names.stream().noneMatch(baseFields::contains)) {
                    addedFields.addAll(names);
                    additions.add(responseIndex.slice(withComment(field)));
                }
            } else if (member instanceof MethodDeclaration) {
                MethodDeclaration method = (MethodDeclaration) member;
                if (!Commands.isTest(method) && !failing.contains(method.getNameAsString())
                        && !baseSignatures.contains(method.getSignature().asString())) {
                    addedMethods.add(method.getNameAsString());
                    additions.add(responseIndex.slice(withComment(method)));
                }
            }
        }
        if (!additions.isEmpty()) {
            int close = baseIndex.offset(baseType.getRange().get().end);
            String indent = baseType.getMembers().isEmpty() ? "    " : indentOf(checkpoint, baseIndex.offset(baseType.getMember(0).getRange().get().begin));
            StringBuilder block = new StringBuilder();
            for (String addition : additions) {
                block.append('\n').append(indent).append(addition).append('\n');
            }
            int lineStart = checkpoint.lastIndexOf('\n', close - 1) + 1;
            boolean braceAlone = checkpoint.substring(lineStart, close).trim().isEmpty();
            edits.add(braceAlone ? new Edit(lineStart, lineStart, block.substring(1)) : new Edit(close, close, block.toString()));
        }

        edits.sort(Comparator.comparingInt((Edit e) -> e.start).reversed());
        StringBuilder code = new StringBuilder(checkpoint);
        int limit = Integer.MAX_VALUE;
        for (Edit edit : edits) {
            if (edit.end > limit) {
                continue;
            }
            code.replace(edit.start, edit.end, edit.text);
            limit = edit.start;
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("code", code.toString());
        result.put("replaced", replaced);
        result.put("dropped", dropped);
        result.put("added_imports", addedImports);
        result.put("added_fields", addedFields);
        result.put("added_methods", addedMethods);
        return result;
    }

    private static TypeDeclaration<?> primaryType(CompilationUnit unit, String preferred) {
        if (preferred != null) {
            for (TypeDeclaration<?> type : unit.getTypes()) {
                if (type.getNameAsString().equals(preferred)) {
                    return type;
                }
            }
        }
        for (TypeDeclaration<?> type : unit.getTypes()) {
            if (type.isPublic()) {
                return type;
            }
        }
        return unit.getType(0);
    }

    private static List<MethodDeclaration> directMethods(TypeDeclaration<?> type, String name) {
        return directMembers(type, MethodDeclaration.class).stream().filter(m -> m.getNameAsString().equals(name)).collect(Collectors.toList());
    }

    private static <T extends BodyDeclaration<?>> List<T> directMembers(TypeDeclaration<?> type, Class<T> kind) {
        return type.getMembers().stream().filter(kind::isInstance).map(kind::cast).collect(Collectors.toList());
    }

    private static String importKey(ImportDeclaration declaration) {
        return (declaration.isStatic() ? "static " : "") + declaration.getNameAsString() + (declaration.isAsterisk() ? ".*" : "");
    }

    /** Range of a declaration including its attached comment (Javadoc). */
    private static Range withComment(Node node) {
        Range range = node.getRange().get();
        Optional<Range> comment = node.getComment().flatMap(Node::getRange);
        return comment.isPresent() && comment.get().begin.isBefore(range.begin) ? new Range(comment.get().begin, range.end) : range;
    }

    private static Edit removal(String text, Commands.LineIndex index, Node node) {
        Range range = withComment(node);
        int start = index.offset(range.begin);
        int end = index.offset(range.end) + 1;
        int lineStart = text.lastIndexOf('\n', start - 1) + 1;
        if (text.substring(lineStart, start).trim().isEmpty()) {
            start = lineStart;
        }
        if (end < text.length() && text.charAt(end) == '\n') {
            end++;
        }
        // Collapse the blank line left above the removed method.
        if (start >= 2 && text.charAt(start - 1) == '\n' && text.substring(text.lastIndexOf('\n', start - 2) + 1, start - 1).trim().isEmpty()) {
            start = text.lastIndexOf('\n', start - 2) + 1;
        }
        return new Edit(start, end, "");
    }

    private static String indentOf(String text, int offset) {
        int lineStart = text.lastIndexOf('\n', offset - 1) + 1;
        String prefix = text.substring(lineStart, offset);
        return prefix.trim().isEmpty() ? prefix : "    ";
    }
}
