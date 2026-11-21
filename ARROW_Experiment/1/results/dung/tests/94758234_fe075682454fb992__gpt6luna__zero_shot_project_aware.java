package com.mastering.spring.springboot.controller;

import com.mastering.spring.springboot.bean.Todo;
import com.mastering.spring.springboot.service.TodoService;
import org.junit.Test;
import org.springframework.http.ResponseEntity;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TodoController_ArrowTest {

    @Test
    public void retrieveTodosReturnsServiceResult() throws Exception {
        TodoController controller = new TodoController();
        TodoService service = mock(TodoService.class);
        setService(controller, service);
        List<Todo> todos = new ArrayList<Todo>(Arrays.asList((Todo) null));
        when(service.retrieveTodos("alice")).thenReturn(todos);

        assertSame(todos, controller.retrieveTodos("alice"));
    }

    @Test
    public void retrieveTodoReturnsServiceResult() throws Exception {
        TodoController controller = new TodoController();
        TodoService service = mock(TodoService.class);
        setService(controller, service);
        Todo todo = mock(Todo.class);
        when(service.retrieveTodo(7)).thenReturn(todo);

        assertSame(todo, controller.retrieveTodo("alice", 7));
    }

    @Test
    public void addReturnsNoContentWhenServiceReturnsNull() throws Exception {
        TodoController controller = new TodoController();
        TodoService service = mock(TodoService.class);
        setService(controller, service);
        Todo todo = mock(Todo.class);
        when(service.addTodo("alice", null, null, false)).thenReturn(null);

        ResponseEntity<?> response = controller.add("alice", todo);

        assertEquals(204, response.getStatusCodeValue());
        assertNull(response.getBody());
    }

    private static void setService(TodoController controller, TodoService service) throws Exception {
        Field field = TodoController.class.getDeclaredField("todoService");
        field.setAccessible(true);
        field.set(controller, service);
    }
}
