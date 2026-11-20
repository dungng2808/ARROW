package com.github.ricksbrown.cowsay.ant;

import org.apache.tools.ant.Project;
import org.junit.Test;
import static org.junit.Assert.*;

public class CowsayTask_ArrowTest {

	@Test
	public void testSetProperty() {
		CowsayTask task = new CowsayTask();
		task.setProperty("testProperty");
		// No direct way to verify, but we can ensure no exception is thrown
		assertNotNull(task);
	}

	@Test
	public void testSetCowfile() {
		CowsayTask task = new CowsayTask();
		task.setCowfile("custom.cow");
		// No direct way to verify, but we can ensure no exception is thrown
		assertNotNull(task);
	}

	@Test
	public void testSetThink() {
		CowsayTask task = new CowsayTask();
		task.setThink(true);
		// No direct way to verify, but we can ensure no exception is thrown
		assertNotNull(task);
	}

	@Test
	public void testSetLang() {
		CowsayTask task = new CowsayTask();
		task.setLang("fr");
		// No direct way to verify, but we can ensure no exception is thrown
		assertNotNull(task);
	}

	@Test
	public void testSetAlt() {
		CowsayTask task = new CowsayTask();
		task.setAlt("Alternative text");
		// No direct way to verify, but we can ensure no exception is thrown
		assertNotNull(task);
	}

	@Test
	public void testSetHtml() {
		CowsayTask task = new CowsayTask();
		task.setHtml(true);
		// No direct way to verify, but we can ensure no exception is thrown
		assertNotNull(task);
	}

	@Test
	public void testSetTongue() {
		CowsayTask task = new CowsayTask();
		task.setTongue("tongue");
		// No direct way to verify, but we can ensure no exception is thrown
		assertNotNull(task);
	}

	@Test
	public void testSetWrap() {
		CowsayTask task = new CowsayTask();
		task.setWrap("50");
		// No direct way to verify, but we can ensure no exception is thrown
		assertNotNull(task);
	}

	@Test
	public void testSetMessage() {
		CowsayTask task = new CowsayTask();
		task.setMessage("Hello World");
		// No direct way to verify, but we can ensure no exception is thrown
		assertNotNull(task);
	}

	@Test
	public void testAddText() {
		CowsayTask task = new CowsayTask();
		Project project = new Project();
		task.setProject(project);
		task.addText("Hello World");
		// No direct way to verify, but we can ensure no exception is thrown
		assertNotNull(task);
	}

	@Test
	public void testSetMode() {
		CowsayTask task = new CowsayTask();
		task.setMode("b");
		// No direct way to verify, but we can ensure no exception is thrown
		assertNotNull(task);
	}

	@Test
	public void testExecuteWithProperty() throws Exception {
		CowsayTask task = new CowsayTask();
		Project project = new Project();
		task.setProject(project);
		task.setProperty("output");
		task.setMessage("Hello");
		// This should not throw an exception
		task.execute();
		// Verify property was set
		assertEquals("Hello", project.getProperty("output"));
	}

	@Test
	public void testExecuteWithoutProperty() throws Exception {
		CowsayTask task = new CowsayTask();
		Project project = new Project();
		task.setProject(project);
		task.setMessage("Hello");
		// This should not throw an exception
		task.execute();
		// Cannot verify output, but should not crash
		assertNotNull(task);
	}
}
