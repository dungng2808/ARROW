package com.mastering.spring.springboot.controller;

import static org.assertj.core.api.Assertions.assertThat;
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
        // Using reflection to set the private field since we can't use Mockito's injectMocks
        // This is a workaround to avoid using reflection in the actual implementation
        // In a real test setup, we would use Spring's testing support
        setTodoServiceField(todoController, todoService);
    }

    @Test
    public void testRetrieveTodos() {
        String name = "testUser";
        List<Todo> expectedTodos = new ArrayList<>();
        expectedTodos.add(new Todo(1, name, "Test Description", new Date(), false));
        
        when(todoService.retrieveTodos(name)).thenReturn(expectedTodos);
        
        List<Todo> result = todoController.retrieveTodos(name);
        
        assertThat(result).isSameAs(expectedTodos);
    }

    @Test
    public void testRetrieveTodo() {
        int id = 1;
        String name = "testUser";
        Todo expectedTodo = new Todo(id, name, "Test Description", new Date(), false);
        
        when(todoService.retrieveTodo(id)).thenReturn(expectedTodo);
        
        Todo result = todoController.retrieveTodo(name, id);
        
        assertThat(result).isSameAs(expectedTodo);
    }

    @Test
    public void testAddTodo_Success() throws Exception {
        String name = "testUser";
        Todo todo = new Todo(0, name, "Test Description", new Date(), false);
        Todo createdTodo = new Todo(1, name, "Test Description", new Date(), false);
        
        when(todoService.addTodo(name, todo.getDesc(), todo.getTargetDate(), todo.isDone())).thenReturn(createdTodo);
        
        ResponseEntity<?> result = todoController.add(name, todo);
        
        assertThat(result.getStatusCode().value()).isEqualTo(201);
        assertThat(result.getHeaders().getLocation()).isNotNull();
    }

    @Test
    public void testAddTodo_NoContent() throws Exception {
        String name = "testUser";
        Todo todo = new Todo(0, name, "Test Description", new Date(), false);
        
        when(todoService.addTodo(name, todo.getDesc(), todo.getTargetDate(), todo.isDone())).thenReturn(null);
        
        ResponseEntity<?> result = todoController.add(name, todo);
        
        assertThat(result.getStatusCode().value()).isEqualTo(204);
    }
    
    // Helper method to set private field - this is needed because we can't use Mockito's injectMocks
    private void setTodoServiceField(TodoController controller, TodoService service) {
        try {
            java.lang.reflect.Field field = TodoController.class.getDeclaredField("todoService");
            field.setAccessible(true);
            field.set(controller, service);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
