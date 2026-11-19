package com.mastering.spring.springboot.controller;

import com.mastering.spring.springboot.bean.Todo;
import com.mastering.spring.springboot.service.TodoService;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import org.springframework.http.ResponseEntity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

public class TodoController_ArrowTest {

    private static class TestableTodoController extends TodoController {
        private final TodoService service;

        TestableTodoController(TodoService service) {
            this.service = service;
            try {
                java.lang.reflect.Field field = TodoController.class.getDeclaredField("todoService");
                field.setAccessible(true);
                field.set(this, service);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Test
    public void retrieveTodosReturnsServiceResult() {
        final List<Todo> expected = new ArrayList<Todo>();
        TodoService service = new TodoService() {
            @Override
            public List<Todo> retrieveTodos(String name) {
                return expected;
            }

            @Override
            public Todo retrieveTodo(int id) {
                return null;
            }

            @Override
            public Todo addTodo(String name, String desc, java.util.Date targetDate, boolean done) {
                return null;
            }
        };

        TodoController controller = new TestableTodoController(service);
        assertSame(expected, controller.retrieveTodos("alice"));
    }

    @Test
    public void retrieveTodoReturnsServiceResult() {
        final Todo expected = null;
        TodoService service = new TodoService() {
            @Override
            public List<Todo> retrieveTodos(String name) {
                return null;
            }

            @Override
            public Todo retrieveTodo(int id) {
                return expected;
            }

            @Override
            public Todo addTodo(String name, String desc, java.util.Date targetDate, boolean done) {
                return null;
            }
        };

        TodoController controller = new TestableTodoController(service);
        assertNull(controller.retrieveTodo("alice", 7));
    }

    @Test
    public void addReturnsNoContentWhenServiceReturnsNull() {
        TodoService service = new TodoService() {
            @Override
            public List<Todo> retrieveTodos(String name) {
                return null;
            }

            @Override
            public Todo retrieveTodo(int id) {
                return null;
            }

            @Override
            public Todo addTodo(String name, String desc, java.util.Date targetDate, boolean done) {
                return null;
            }
        };

        TodoController controller = new TestableTodoController(service);
        ResponseEntity<?> response = controller.add("alice", null);
        assertEquals(204, response.getStatusCodeValue());
    }
}
