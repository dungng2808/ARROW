package com.devskiller.jpa2ddl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;

public class FileResolver_ArrowTest {

    @Test
    public void resolveNextMigrationFileUsesOneGreaterThanHighestVersion() throws Exception {
        File directory = new File(System.getProperty("java.io.tmpdir"),
                "file-resolver-next-migration-test");
        directory.mkdirs();
        File first = new File(directory, "v2__change.sql");
        File second = new File(directory, "v5__change.sql");
        first.createNewFile();
        second.createNewFile();

        File result = FileResolver.resolveNextMigrationFile(directory);

        assertEquals(new File(directory, "v6__jpa2ddl.sql"), result);
        first.delete();
        second.delete();
        directory.delete();
    }

    @Test
    public void resolveNextMigrationFileStartsAtOneForEmptyDirectory() throws Exception {
        File directory = new File(System.getProperty("java.io.tmpdir"),
                "file-resolver-empty-migration-test");
        directory.mkdirs();

        File result = FileResolver.resolveNextMigrationFile(directory);

        assertEquals(new File(directory, "v1__jpa2ddl.sql"), result);
        directory.delete();
    }

    @Test
    public void resolveExistingMigrationsSortsByVersionAscending() throws Exception {
        File directory = new File(System.getProperty("java.io.tmpdir"),
                "file-resolver-sort-migration-test");
        directory.mkdirs();
        File versionThree = new File(directory, "v3__change.sql");
        File versionOne = new File(directory, "v1__change.sql");
        File invalid = new File(directory, "notes.txt");
        versionThree.createNewFile();
        versionOne.createNewFile();
        invalid.createNewFile();

        List<Path> result = FileResolver.resolveExistingMigrations(directory, false, false);

        assertEquals(Arrays.asList(invalid.toPath(), versionOne.toPath(), versionThree.toPath()), result);
        versionThree.delete();
        versionOne.delete();
        invalid.delete();
        directory.delete();
    }

    @Test
    public void resolveExistingMigrationsCanSortInReverseOrder() throws Exception {
        File directory = new File(System.getProperty("java.io.tmpdir"),
                "file-resolver-reverse-migration-test");
        directory.mkdirs();
        File versionThree = new File(directory, "v3__change.sql");
        File versionOne = new File(directory, "v1__change.sql");
        versionThree.createNewFile();
        versionOne.createNewFile();

        List<Path> result = FileResolver.resolveExistingMigrations(directory, true, false);

        assertEquals(Arrays.asList(versionThree.toPath(), versionOne.toPath()), result);
        versionThree.delete();
        versionOne.delete();
        directory.delete();
    }

    @Test
    public void resolveExistingMigrationsCanFilterForSchemaMigrations() throws Exception {
        File directory = new File(System.getProperty("java.io.tmpdir"),
                "file-resolver-schema-migration-test");
        directory.mkdirs();
        File schema = new File(directory, "v2__jpa2ddl.sql");
        File regular = new File(directory, "v1__change.sql");
        schema.createNewFile();
        regular.createNewFile();

        List<Path> result = FileResolver.resolveExistingMigrations(directory, false, true);

        assertEquals(Arrays.asList(schema.toPath()), result);
        schema.delete();
        regular.delete();
        directory.delete();
    }

    @Test
    public void resolveExistingMigrationsCreatesMissingDirectory() {
        File directory = new File(System.getProperty("java.io.tmpdir"),
                "file-resolver-created-directory-test");
        directory.delete();

        List<Path> result = FileResolver.resolveExistingMigrations(directory, false, false);

        assertTrue(directory.exists());
        assertTrue(result.isEmpty());
        directory.delete();
    }

    @Test
    public void listClassNamesInPackageIncludesClassesFromPackage() throws Exception {
        List<String> result = FileResolver.listClassNamesInPackage("com.devskiller.jpa2ddl");

        assertFalse(result.isEmpty());
        assertTrue(result.contains("com.devskiller.jpa2ddl.FileResolver_ArrowTest"));
    }

    @Test(expected = IllegalStateException.class)
    public void listClassNamesInMissingPackageThrows() throws Exception {
        FileResolver.listClassNamesInPackage("com.devskiller.jpa2ddl.package.that.does.not.exist");
    }
}
