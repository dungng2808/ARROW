package com.mastering.spring.springboot.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.springframework.http.ResponseEntity;

import com.mastering.spring.springboot.bean.Todo;
import com.mastering.spring.springboot.service.TodoService;

public class TodoController_ArrowTest {

	private TodoController todoController;
	private TodoService todoService;

	@Before
	public void setUp() {
		todoService = mock(TodoService.class);
		todoController = new TodoController();
		// Using reflection to set the private field since we can't use constructor or
		// setter
		try {
			java.lang.reflect.Field field = TodoController.class.getDeclaredField("todoService");
			field.setAccessible(true);
			field.set(todoController, todoService);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Test
	public void testRetrieveTodos() {
		String name = "testUser";
		List<Todo> expectedTodos = new ArrayList<>();
		expectedTodos.add(new Todo(1, name, "Test Description", new Date(), false));

		when(todoService.retrieveTodos(name)).thenReturn(expectedTodos);

		List<Todo> actualTodos = todoController.retrieveTodos(name);

		assertEquals(expectedTodos, actualTodos);
	}

	@Test
	public void testRetrieveTodo() {
		String name = "testUser";
		int id = 1;
		Todo expectedTodo = new Todo(id, name, "Test Description", new Date(), false);

		when(todoService.retrieveTodo(id)).thenReturn(expectedTodo);

		Todo actualTodo = todoController.retrieveTodo(name, id);

		assertEquals(expectedTodo, actualTodo);
	}

	@Test
	public void testAddTodo_Success() throws Exception {
		String name = "testUser";
		Todo todo = new Todo(0, name, "Test Description", new Date(), false);
		Todo createdTodo = new Todo(1, name, "Test Description", new Date(), false);

		when(todoService.addTodo(name, todo.getDesc(), todo.getTargetDate(), todo.isDone())).thenReturn(createdTodo);

		ResponseEntity<?> response = todoController.add(name, todo);

		assertNotNull(response);
		assertEquals(201, response.getStatusCodeValue());
	}

	@Test
	public void testAddTodo_NoContent() throws Exception {
		String name = "testUser";
		Todo todo = new Todo(0, name, "Test Description", new Date(), false);

		when(todoService.addTodo(name, todo.getDesc(), todo.getTargetDate(), todo.isDone())).thenReturn(null);

		ResponseEntity<?> response = todoController.add(name, todo);

		assertNotNull(response);
		assertEquals(204, response.getStatusCodeValue());
	}
}
