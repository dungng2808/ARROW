package com.devskiller.jpa2ddl;

import org.junit.Test;
import static org.junit.Assert.*;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class FileResolver_ArrowTest {

	@Test
	public void testResolveNextMigrationFile() throws IOException {
		File tempDir = Files.createTempDirectory("migration_test").toFile();
		try {
			File nextFile = FileResolver.resolveNextMigrationFile(tempDir);
			assertEquals("v1__jpa2ddl.sql", nextFile.getName());
		} finally {
			tempDir.delete();
		}
	}

	@Test
	public void testResolveNextMigrationFileWithExistingFile() throws IOException {
		File tempDir = Files.createTempDirectory("migration_test").toFile();
		try {
			File existingFile = new File(tempDir, "v1__test.sql");
			existingFile.createNewFile();
			
			File nextFile = FileResolver.resolveNextMigrationFile(tempDir);
			assertEquals("v2__jpa2ddl.sql", nextFile.getName());
		} finally {
			tempDir.delete();
		}
	}

	@Test
	public void testResolveExistingMigrationsEmptyDir() {
		File tempDir = new File("nonexistent_dir_12345");
		List<Path> migrations = FileResolver.resolveExistingMigrations(tempDir, false, false);
		assertTrue(migrations.isEmpty());
	}

	@Test
	public void testResolveExistingMigrationsWithFiles() throws IOException {
		File tempDir = Files.createTempDirectory("migration_test").toFile();
		try {
			File file1 = new File(tempDir, "v2__second.sql");
			File file2 = new File(tempDir, "v1__first.sql");
			file1.createNewFile();
			file2.createNewFile();
			
			List<Path> migrations = FileResolver.resolveExistingMigrations(tempDir, false, false);
			assertEquals(2, migrations.size());
			assertEquals("v1__first.sql", migrations.get(0).getFileName().toString());
			assertEquals("v2__second.sql", migrations.get(1).getFileName().toString());
		} finally {
			tempDir.delete();
		}
	}

	@Test
	public void testResolveExistingMigrationsReversed() throws IOException {
		File tempDir = Files.createTempDirectory("migration_test").toFile();
		try {
			File file1 = new File(tempDir, "v2__second.sql");
			File file2 = new File(tempDir, "v1__first.sql");
			file1.createNewFile();
			file2.createNewFile();
			
			List<Path> migrations = FileResolver.resolveExistingMigrations(tempDir, true, false);
			assertEquals(2, migrations.size());
			assertEquals("v2__second.sql", migrations.get(0).getFileName().toString());
			assertEquals("v1__first.sql", migrations.get(1).getFileName().toString());
		} finally {
			tempDir.delete();
		}
	}

	@Test
	public void testResolveExistingMigrationsOnlySchemaMigrations() throws IOException {
		File tempDir = Files.createTempDirectory("migration_test").toFile();
		try {
			File file1 = new File(tempDir, "v2__jpa2ddl_schema.sql");
			File file2 = new File(tempDir, "v1__normal.sql");
			file1.createNewFile();
			file2.createNewFile();
			
			List<Path> migrations = FileResolver.resolveExistingMigrations(tempDir, false, true);
			assertEquals(1, migrations.size());
			assertEquals("v2__jpa2ddl_schema.sql", migrations.get(0).getFileName().toString());
		} finally {
			tempDir.delete();
		}
	}

	@Test(expected = IllegalStateException.class)
	public void testListClassNamesInPackageNoPackage() throws Exception {
		FileResolver.listClassNamesInPackage("com.nonexistent.package");
	}
}
