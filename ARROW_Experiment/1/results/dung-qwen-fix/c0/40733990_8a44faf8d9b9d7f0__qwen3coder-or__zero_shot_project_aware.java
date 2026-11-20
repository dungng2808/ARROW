package com.github.ricksbrown.cowsay.ant;

import com.github.ricksbrown.cowsay.plugin.CowExecutor;
import org.apache.tools.ant.Project;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class CowsayTask_ArrowTest {

	private CowsayTask task;
	private Project project;

	@Before
	public void setUp() {
		task = new CowsayTask();
		project = new Project();
		task.setProject(project);
	}

	@Test
	public void testSetProperty() {
		String propertyName = "testProperty";
		task.setProperty(propertyName);
		assertEquals(propertyName, task.getClass().getDeclaredField("property").get(task));
	}

	@Test
	public void testSetCowfile() {
		String cowfile = "test.cow";
		task.setCowfile(cowfile);
		// Verify that the cowfile was set in the executor
		CowExecutor executor = getPrivateExecutor(task);
		assertEquals(cowfile, executor.getClass().getDeclaredField("cowfile").get(executor));
	}

	@Test
	public void testSetThink() {
		task.setThink(true);
		CowExecutor executor = getPrivateExecutor(task);
		assertEquals(true, executor.getClass().getDeclaredField("think").get(executor));
	}

	@Test
	public void testSetLang() {
		String lang = "fr";
		task.setLang(lang);
		CowExecutor executor = getPrivateExecutor(task);
		assertEquals(lang, executor.getClass().getDeclaredField("lang").get(executor));
	}

	@Test
	public void testSetAlt() {
		String alt = "test alt text";
		task.setAlt(alt);
		CowExecutor executor = getPrivateExecutor(task);
		assertEquals(alt, executor.getClass().getDeclaredField("alt").get(executor));
	}

	@Test
	public void testSetHtml() {
		task.setHtml(true);
		CowExecutor executor = getPrivateExecutor(task);
		assertEquals(true, executor.getClass().getDeclaredField("html").get(executor));
	}

	@Test
	public void testSetTongue() {
		String tongue = "wide";
		task.setTongue(tongue);
		CowExecutor executor = getPrivateExecutor(task);
		assertEquals(tongue, executor.getClass().getDeclaredField("tongue").get(executor));
	}

	@Test
	public void testSetWrap() {
		String wrap = "50";
		task.setWrap(wrap);
		CowExecutor executor = getPrivateExecutor(task);
		assertEquals(wrap, executor.getClass().getDeclaredField("wrap").get(executor));
	}

	@Test
	public void testSetMessage() {
		String message = "Hello World";
		task.setMessage(message);
		CowExecutor executor = getPrivateExecutor(task);
		assertEquals(message, executor.getClass().getDeclaredField("message").get(executor));
	}

	@Test
	public void testAddText() {
		String text = "Hello World";
		task.addText(text);
		CowExecutor executor = getPrivateExecutor(task);
		assertEquals(text, executor.getClass().getDeclaredField("message").get(executor));
	}

	@Test
	public void testSetMode() {
		String mode = "b";
		task.setMode(mode);
		CowExecutor executor = getPrivateExecutor(task);
		assertEquals(mode, executor.getClass().getDeclaredField("mode").get(executor));
	}

	@Test
	public void testExecuteWithProperty() throws Exception {
		String propertyName = "outputProperty";
		String message = "Hello World";
		task.setProperty(propertyName);
		task.setMessage(message);
		
		// Mock the project to capture property setting
		task.execute();
		
		// Verify that the property was set
		assertEquals(message, project.getProperty(propertyName));
	}

	@Test
	public void testExecuteWithoutProperty() throws Exception {
		// This would normally print to stdout, but we're testing the logic flow
		String message = "Hello World";
		task.setMessage(message);
		task.execute(); // Should not throw any exception
	}

	private CowExecutor getPrivateExecutor(CowsayTask task) {
		try {
			java.lang.reflect.Field field = CowsayTask.class.getDeclaredField("executor");
			field.setAccessible(true);
			return (CowExecutor) field.get(task);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
