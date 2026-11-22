package com.devskiller.jpa2ddl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.Test;

public class FileResolver_ArrowTest {

    @Test
    public void resolvesNextMigrationFileWhenDirectoryIsEmpty() throws Exception {
        Path directory = Files.createTempDirectory("file-resolver-empty");
        try {
            File expected = directory.resolve("v1__jpa2ddl.sql").toFile();
            assertEquals(expected, FileResolver.resolveNextMigrationFile(directory.toFile()));
        } finally {
            Files.deleteIfExists(directory);
        }
    }

    @Test
    public void resolvesNextMigrationFileUsingHighestVersion() throws Exception {
        Path directory = Files.createTempDirectory("file-resolver-version");
        try {
            Files.createFile(directory.resolve("v2__first.sql"));
            Files.createFile(directory.resolve("v10__latest.sql"));
            Files.createFile(directory.resolve("v3__jpa2ddl.sql"));

            File expected = directory.resolve("v11__jpa2ddl.sql").toFile();
            assertEquals(expected, FileResolver.resolveNextMigrationFile(directory.toFile()));
        } finally {
            try (java.util.stream.Stream<Path> paths = Files.list(directory)) {
                paths.forEach(path -> {
                    try {
                        Files.deleteIfExists(path);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
            }
            Files.deleteIfExists(directory);
        }
    }

    @Test
    public void sortsMigrationsInDescendingVersionOrder() throws Exception {
        Path directory = Files.createTempDirectory("file-resolver-sort");
        try {
            Files.createFile(directory.resolve("v2__two.sql"));
            Files.createFile(directory.resolve("v10__ten.sql"));
            Files.createFile(directory.resolve("notes.txt"));

            List<Path> result = FileResolver.resolveExistingMigrations(directory.toFile(), true, false);

            assertEquals(3, result.size());
            assertEquals("v10__ten.sql", result.get(0).getFileName().toString());
            assertEquals("v2__two.sql", result.get(1).getFileName().toString());
            assertEquals("notes.txt", result.get(2).getFileName().toString());
        } finally {
            try (java.util.stream.Stream<Path> paths = Files.list(directory)) {
                paths.forEach(path -> {
                    try {
                        Files.deleteIfExists(path);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
            }
            Files.deleteIfExists(directory);
        }
    }

    @Test
    public void filtersSchemaMigrations() throws Exception {
        Path directory = Files.createTempDirectory("file-resolver-schema");
        try {
            Files.createFile(directory.resolve("v1__jpa2ddl.sql"));
            Files.createFile(directory.resolve("V2__JPA2DDL-extra.SQL"));
            Files.createFile(directory.resolve("v3__ordinary.sql"));

            List<Path> result = FileResolver.resolveExistingMigrations(directory.toFile(), false, true);

            assertEquals(2, result.size());
            assertEquals("v1__jpa2ddl.sql", result.get(0).getFileName().toString());
            assertEquals("V2__JPA2DDL-extra.SQL", result.get(1).getFileName().toString());
        } finally {
            try (java.util.stream.Stream<Path> paths = Files.list(directory)) {
                paths.forEach(path -> {
                    try {
                        Files.deleteIfExists(path);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
            }
            Files.deleteIfExists(directory);
        }
    }

    @Test
    public void createsMissingMigrationDirectory() throws Exception {
        Path parent = Files.createTempDirectory("file-resolver-parent");
        Path directory = parent.resolve("missing");

        try {
            List<Path> result = FileResolver.resolveExistingMigrations(directory.toFile(), true, false);

            assertTrue(Files.isDirectory(directory));
            assertTrue(result.isEmpty());
        } finally {
            Files.deleteIfExists(directory);
            Files.deleteIfExists(parent);
        }
    }

    @Test
    public void returnsEmptyListForSchemaFilterWithNoMatches() throws Exception {
        Path directory = Files.createTempDirectory("file-resolver-no-schema");
        try {
            Files.createFile(directory.resolve("v1__ordinary.sql"));

            List<Path> result = FileResolver.resolveExistingMigrations(directory.toFile(), true, true);

            assertTrue(result.isEmpty());
        } finally {
            Files.deleteIfExists(directory.resolve("v1__ordinary.sql"));
            Files.deleteIfExists(directory);
        }
    }

    @Test
    public void listsClassNamesInExistingPackage() throws Exception {
        List<String> result = FileResolver.listClassNamesInPackage("com.devskiller.jpa2ddl");

        assertFalse(result.isEmpty());
        assertTrue(result.contains("com.devskiller.jpa2ddl.FileResolver"));
    }

    @Test(expected = IllegalStateException.class)
    public void throwsWhenPackageDoesNotExist() throws Exception {
        FileResolver.listClassNamesInPackage("package.that.does.not.exist");
    }
}
