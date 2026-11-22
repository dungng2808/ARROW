package com.devskiller.jpa2ddl;

import org.junit.Test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class FileResolver_ArrowTest {

    @Test
    public void resolveNextMigrationFileStartsAtVersionOneWhenDirectoryIsEmpty() throws Exception {
        Path directory = Files.createTempDirectory("file-resolver");
        try {
            File result = FileResolver.resolveNextMigrationFile(directory.toFile());

            assertThat(result).isEqualTo(directory.resolve("v1__jpa2ddl.sql").toFile());
        } finally {
            Files.deleteIfExists(directory);
        }
    }

    @Test
    public void resolveNextMigrationFileUsesHighestVersion() throws Exception {
        Path directory = Files.createTempDirectory("file-resolver");
        try {
            Files.createFile(directory.resolve("v2__first.sql"));
            Files.createFile(directory.resolve("v10__second.sql"));
            Files.createFile(directory.resolve("v3__jpa2ddl.sql"));

            File result = FileResolver.resolveNextMigrationFile(directory.toFile());

            assertThat(result).isEqualTo(directory.resolve("v11__jpa2ddl.sql").toFile());
        } finally {
            Files.deleteIfExists(directory.resolve("v2__first.sql"));
            Files.deleteIfExists(directory.resolve("v10__second.sql"));
            Files.deleteIfExists(directory.resolve("v3__jpa2ddl.sql"));
            Files.deleteIfExists(directory);
        }
    }

    @Test
    public void resolveExistingMigrationsSortsByVersionAscending() throws Exception {
        Path directory = Files.createTempDirectory("file-resolver");
        try {
            Path versionTwo = Files.createFile(directory.resolve("v2__second.sql"));
            Path versionOne = Files.createFile(directory.resolve("v1__first.sql"));
            Path invalid = Files.createFile(directory.resolve("notes.txt"));

            List<Path> result = FileResolver.resolveExistingMigrations(directory.toFile(), false, false);

            assertThat(result).containsExactly(invalid, versionOne, versionTwo);
        } finally {
            Files.deleteIfExists(directory.resolve("v2__second.sql"));
            Files.deleteIfExists(directory.resolve("v1__first.sql"));
            Files.deleteIfExists(directory.resolve("notes.txt"));
            Files.deleteIfExists(directory);
        }
    }

    @Test
    public void resolveExistingMigrationsSortsByVersionDescending() throws Exception {
        Path directory = Files.createTempDirectory("file-resolver");
        try {
            Path versionTwo = Files.createFile(directory.resolve("v2__second.sql"));
            Path versionOne = Files.createFile(directory.resolve("v1__first.sql"));

            List<Path> result = FileResolver.resolveExistingMigrations(directory.toFile(), true, false);

            assertThat(result).containsExactly(versionTwo, versionOne);
        } finally {
            Files.deleteIfExists(directory.resolve("v2__second.sql"));
            Files.deleteIfExists(directory.resolve("v1__first.sql"));
            Files.deleteIfExists(directory);
        }
    }

    @Test
    public void resolveExistingMigrationsCanFilterSchemaMigrations() throws Exception {
        Path directory = Files.createTempDirectory("file-resolver");
        try {
            Path schemaMigration = Files.createFile(directory.resolve("v2__jpa2ddl.sql"));
            Files.createFile(directory.resolve("v1__application.sql"));
            Files.createFile(directory.resolve("v3__jpa2ddl-custom.sql"));

            List<Path> result = FileResolver.resolveExistingMigrations(directory.toFile(), false, true);

            assertThat(result).containsExactly(schemaMigration);
        } finally {
            Files.deleteIfExists(directory.resolve("v2__jpa2ddl.sql"));
            Files.deleteIfExists(directory.resolve("v1__application.sql"));
            Files.deleteIfExists(directory.resolve("v3__jpa2ddl-custom.sql"));
            Files.deleteIfExists(directory);
        }
    }

    @Test
    public void resolveExistingMigrationsCreatesMissingDirectory() throws Exception {
        Path parent = Files.createTempDirectory("file-resolver");
        Path directory = parent.resolve("missing");
        try {
            List<Path> result = FileResolver.resolveExistingMigrations(directory.toFile(), false, false);

            assertThat(result).isEmpty();
            assertThat(Files.isDirectory(directory)).isTrue();
        } finally {
            Files.deleteIfExists(directory);
            Files.deleteIfExists(parent);
        }
    }

    @Test
    public void listClassNamesInPackageFindsClasses() throws Exception {
        List<String> result = FileResolver.listClassNamesInPackage("com.devskiller.jpa2ddl");

        assertThat(result).contains("com.devskiller.jpa2ddl.FileResolver");
    }

    @Test(expected = IllegalStateException.class)
    public void listClassNamesInPackageThrowsWhenPackageIsMissing() throws Exception {
        FileResolver.listClassNamesInPackage("package.that.does.not.exist");
    }
}
