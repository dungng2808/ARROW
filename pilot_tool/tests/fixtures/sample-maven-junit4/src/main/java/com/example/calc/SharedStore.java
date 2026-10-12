package com.example.calc;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

/** State shared between tests through a file in the build directory. */
public final class SharedStore {

    public static final File FILE = new File("target/shared-store.txt");

    private SharedStore() {
    }

    public static void write(String value) throws IOException {
        FILE.getParentFile().mkdirs();
        Files.write(FILE.toPath(), value.getBytes(StandardCharsets.UTF_8));
    }

    public static String read() throws IOException {
        return FILE.exists() ? new String(Files.readAllBytes(FILE.toPath()), StandardCharsets.UTF_8) : "ok";
    }
}
