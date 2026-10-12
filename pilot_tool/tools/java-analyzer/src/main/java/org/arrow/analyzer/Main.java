package org.arrow.analyzer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Entry point: {@code java -jar java-analyzer.jar <request.json>}. The request is a JSON object with a
 * {@code command} field; the result is one JSON object on stdout. Errors are reported as
 * {@code {"error": "..."}} with exit code 0 so the caller always gets JSON; exit code 2 only for bad usage.
 *
 * <p>Commands (specs/03 Mục 7–8, specs/05 Mục 5.3, specs/04 Mục 6.2):
 * analyze_test, strip_comments, count_assertions, method_at_line, cut_api, method_source, called_methods, splice_methods.
 */
public final class Main {

    private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().serializeNulls().create();

    private Main() {
    }

    public static void main(String[] args) throws IOException {
        PrintStream out = new PrintStream(System.out, true, "UTF-8");
        if (args.length != 1) {
            System.err.println("usage: java -jar java-analyzer.jar <request.json>");
            System.exit(2);
        }
        String text = new String(Files.readAllBytes(Paths.get(args[0])), StandardCharsets.UTF_8);
        JsonObject request = GSON.fromJson(text, JsonObject.class);
        Object result;
        try {
            result = run(request);
        } catch (RuntimeException e) {
            Map<String, Object> error = new LinkedHashMap<>();
            error.put("error", e.getClass().getSimpleName() + ": " + e.getMessage());
            result = error;
        }
        out.println(GSON.toJson(result));
    }

    static Object run(JsonObject request) {
        String command = request.get("command").getAsString();
        switch (command) {
            case "analyze_test":
                return Commands.analyzeTest(request);
            case "strip_comments":
                return Commands.stripComments(request);
            case "count_assertions":
                return Commands.countAssertions(request);
            case "method_at_line":
                return Commands.methodAtLine(request);
            case "cut_api":
                return Commands.cutApi(request);
            case "method_source":
                return Commands.methodSource(request);
            case "called_methods":
                return Commands.calledMethods(request);
            case "splice_methods":
                return Commands.spliceMethods(request);
            default:
                throw new IllegalArgumentException("unknown command " + command);
        }
    }
}
