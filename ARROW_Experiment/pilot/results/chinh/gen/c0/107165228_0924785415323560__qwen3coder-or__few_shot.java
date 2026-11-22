package com.devskiller.jpa2ddl;

import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.Assert.*;

public class FileResolver_ArrowTest {

	@Test
	public void testResolveNextMigrationFile() throws IOException {
		File tempDir = Files.createTempDirectory("migration_test").toFile();
		try {
			File nextFile = FileResolver.resolveNextMigrationFile(tempDir);
			assertEquals("v1__jpa2ddl.sql", nextFile.getName());
			
			// Create a file to test incrementing
			File existingFile = new File(tempDir, "v1__test.sql");
			existingFile.createNewFile();
			
			File nextFile2 = FileResolver.resolveNextMigrationFile(tempDir);
			assertEquals("v2__jpa2ddl.sql", nextFile2.getName());
		} finally {
			tempDir.delete();
		}
	}

	@Test
	public void testResolveExistingMigrationsEmptyDir() {
		File tempDir = new File("nonexistent_dir_12345");
		List<Path> migrations = FileResolver.resolveExistingMigrations(tempDir, false, false);
		assertNotNull(migrations);
		assertEquals(0, migrations.size());
	}

	@Test
	public void testResolveExistingMigrationsWithFiles() throws IOException {
		File tempDir = Files.createTempDirectory("migration_test2").toFile();
		try {
			// Create some test files
			new File(tempDir, "v1__first.sql").createNewFile();
			new File(tempDir, "v3__third.sql").createNewFile();
			new File(tempDir, "v2__second.sql").createNewFile();
			new File(tempDir, "invalid_file.txt").createNewFile();
			
			List<Path> migrations = FileResolver.resolveExistingMigrations(tempDir, false, false);
			assertEquals(3, migrations.size());
			assertEquals("v1__first.sql", migrations.get(0).getFileName().toString());
			assertEquals("v2__second.sql", migrations.get(1).getFileName().toString());
			assertEquals("v3__third.sql", migrations.get(2).getFileName().toString());
		} finally {
			tempDir.delete();
		}
	}

	@Test
	public void testResolveExistingMigrationsReversed() throws IOException {
		File tempDir = Files.createTempDirectory("migration_test3").toFile();
		try {
			new File(tempDir, "v1__first.sql").createNewFile();
			new File(tempDir, "v3__third.sql").createNewFile();
			new File(tempDir, "v2__second.sql").createNewFile();
			
			List<Path> migrations = FileResolver.resolveExistingMigrations(tempDir, true, false);
			assertEquals(3, migrations.size());
			assertEquals("v3__third.sql", migrations.get(0).getFileName().toString());
			assertEquals("v2__second.sql", migrations.get(1).getFileName().toString());
			assertEquals("v1__first.sql", migrations.get(2).getFileName().toString());
		} finally {
			tempDir.delete();
		}
	}

	@Test
	public void testResolveExistingMigrationsOnlySchema() throws IOException {
		File tempDir = Files.createTempDirectory("migration_test4").toFile();
		try {
			new File(tempDir, "v1__first.sql").createNewFile();
			new File(tempDir, "v2__jpa2ddl_schema.sql").createNewFile();
			new File(tempDir, "v3__third.sql").createNewFile();
			new File(tempDir, "v4__jpa2ddl_other.sql").createNewFile();
			
			List<Path> migrations = FileResolver.resolveExistingMigrations(tempDir, false, true);
			assertEquals(2, migrations.size());
			assertEquals("v2__jpa2ddl_schema.sql", migrations.get(0).getFileName().toString());
			assertEquals("v4__jpa2ddl_other.sql", migrations.get(1).getFileName().toString());
		} finally {
			tempDir.delete();
		}
	}

	@Test
	public void testCompareVersionedMigrations() {
		Path path1 = Paths.get("v1__test.sql");
		Path path2 = Paths.get("v10__test.sql");
		Path path3 = Paths.get("invalid_file.txt");
		
		assertEquals(Long.valueOf(1), FileResolver.compareVersionedMigrations(path1));
		assertEquals(Long.valueOf(10), FileResolver.compareVersionedMigrations(path2));
		assertEquals(Long.valueOf(Long.MIN_VALUE), FileResolver.compareVersionedMigrations(path3));
	}
}
