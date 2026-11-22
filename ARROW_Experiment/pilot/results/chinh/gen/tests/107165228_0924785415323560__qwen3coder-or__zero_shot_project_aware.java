package com.devskiller.jpa2ddl;

import org.junit.Test;
import org.junit.Before;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class FileResolver_ArrowTest {

    private File tempDir;

    @Before
    public void setUp() throws IOException {
        tempDir = Files.createTempDirectory("migration_test").toFile();
    }

    @Test
    public void shouldResolveNextMigrationFileWhenNoFilesExist() {
        File nextFile = FileResolver.resolveNextMigrationFile(tempDir);
        assertThat(nextFile.getName()).isEqualTo("v1__jpa2ddl.sql");
    }

    @Test
    public void shouldResolveNextMigrationFileWhenOneFileExists() {
        File existingFile = new File(tempDir, "v1__test.sql");
        try {
            existingFile.createNewFile();
        } catch (IOException e) {
            // ignore
        }
        
        File nextFile = FileResolver.resolveNextMigrationFile(tempDir);
        assertThat(nextFile.getName()).isEqualTo("v2__jpa2ddl.sql");
    }

    @Test
    public void shouldResolveNextMigrationFileWhenMultipleFilesExist() {
        File file1 = new File(tempDir, "v1__test.sql");
        File file2 = new File(tempDir, "v3__test.sql");
        File file3 = new File(tempDir, "v2__test.sql");
        try {
            file1.createNewFile();
            file2.createNewFile();
            file3.createNewFile();
        } catch (IOException e) {
            // ignore
        }
        
        File nextFile = FileResolver.resolveNextMigrationFile(tempDir);
        assertThat(nextFile.getName()).isEqualTo("v4__jpa2ddl.sql");
    }

    @Test
    public void shouldResolveNextMigrationFileWhenNonVersionedFileExists() {
        File nonVersionedFile = new File(tempDir, "test.sql");
        try {
            nonVersionedFile.createNewFile();
        } catch (IOException e) {
            // ignore
        }
        
        File nextFile = FileResolver.resolveNextMigrationFile(tempDir);
        assertThat(nextFile.getName()).isEqualTo("v1__jpa2ddl.sql");
    }

    @Test
    public void shouldResolveExistingMigrationsWhenDirectoryDoesNotExist() {
        File nonExistentDir = new File(tempDir, "non_existent");
        List<Path> migrations = FileResolver.resolveExistingMigrations(nonExistentDir, false, false);
        assertThat(migrations).isEmpty();
    }

    @Test
    public void shouldResolveExistingMigrationsWhenDirectoryIsEmpty() {
        List<Path> migrations = FileResolver.resolveExistingMigrations(tempDir, false, false);
        assertThat(migrations).isEmpty();
    }

    @Test
    public void shouldResolveExistingMigrationsWithVersionedFiles() {
        File file1 = new File(tempDir, "v1__test.sql");
        File file2 = new File(tempDir, "v3__test.sql");
        File file3 = new File(tempDir, "v2__test.sql");
        try {
            file1.createNewFile();
            file2.createNewFile();
            file3.createNewFile();
        } catch (IOException e) {
            // ignore
        }
        
        List<Path> migrations = FileResolver.resolveExistingMigrations(tempDir, false, false);
        assertThat(migrations).hasSize(3);
        assertThat(migrations.get(0).getFileName().toString()).isEqualTo("v1__test.sql");
        assertThat(migrations.get(1).getFileName().toString()).isEqualTo("v2__test.sql");
        assertThat(migrations.get(2).getFileName().toString()).isEqualTo("v3__test.sql");
    }

    @Test
    public void shouldResolveExistingMigrationsReversed() {
        File file1 = new File(tempDir, "v1__test.sql");
        File file2 = new File(tempDir, "v3__test.sql");
        File file3 = new File(tempDir, "v2__test.sql");
        try {
            file1.createNewFile();
            file2.createNewFile();
            file3.createNewFile();
        } catch (IOException e) {
            // ignore
        }
        
        List<Path> migrations = FileResolver.resolveExistingMigrations(tempDir, true, false);
        assertThat(migrations).hasSize(3);
        assertThat(migrations.get(0).getFileName().toString()).isEqualTo("v3__test.sql");
        assertThat(migrations.get(1).getFileName().toString()).isEqualTo("v2__test.sql");
        assertThat(migrations.get(2).getFileName().toString()).isEqualTo("v1__test.sql");
    }

    @Test
    public void shouldResolveExistingMigrationsOnlySchemaMigrations() {
        File regularFile = new File(tempDir, "v1__test.sql");
        File schemaFile = new File(tempDir, "v1__jpa2ddl_schema.sql");
        try {
            regularFile.createNewFile();
            schemaFile.createNewFile();
        } catch (IOException e) {
            // ignore
        }
        
        List<Path> migrations = FileResolver.resolveExistingMigrations(tempDir, false, true);
        assertThat(migrations).hasSize(1);
        assertThat(migrations.get(0).getFileName().toString()).isEqualTo("v1__jpa2ddl_schema.sql");
    }

    @Test
    public void shouldListClassNamesInPackage() throws Exception {
        // This test assumes there are classes in the test package
        List<String> classNames = FileResolver.listClassNamesInPackage("com.devskiller.jpa2ddl");
        assertThat(classNames).isNotEmpty();
    }

    @Test
    public void shouldThrowExceptionWhenListingClassesInNonExistentPackage() throws Exception {
        assertThatThrownBy(() -> FileResolver.listClassNamesInPackage("non.existent.package"))
                .isInstanceOf(IllegalStateException.class);
    }
}
