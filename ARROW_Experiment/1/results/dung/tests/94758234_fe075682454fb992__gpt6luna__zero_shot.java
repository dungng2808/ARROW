package com.mastering.spring.springboot.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.springframework.http.ResponseEntity;

import com.mastering.spring.springboot.bean.Todo;
import com.mastering.spring.springboot.service.TodoService;

public class TodoController_ArrowTest {

    private static class StubTodoService extends TodoService {
        private List<Todo> todos;
        private Todo todo;
        private Todo addedTodo;
        private String addedName;
        private String addedDescription;
        private java.util.Date addedTargetDate;
        private boolean addedDone;

        @Override
        public List<Todo> retrieveTodos(String name) {
            return todos;
        }

        @Override
        public Todo retrieveTodo(int id) {
            return todo;
        }

        @Override
        public Todo addTodo(String name, String description, java.util.Date targetDate, boolean done) {
            addedName = name;
            addedDescription = description;
            addedTargetDate = targetDate;
            addedDone = done;
            return addedTodo;
        }
    }

    private TodoController controllerWith(StubTodoService service) throws Exception {
        TodoController controller = new TodoController();
        java.lang.reflect.Field field = TodoController.class.getDeclaredField("todoService");
        field.setAccessible(true);
        field.set(controller, service);
        return controller;
    }

    @Test
    public void retrieveTodosReturnsServiceResult() throws Exception {
        StubTodoService service = new StubTodoService();
        service.todos = new ArrayList<Todo>();
        TodoController controller = controllerWith(service);

        assertEquals(service.todos, controller.retrieveTodos("alice"));
    }

    @Test
    public void retrieveTodoReturnsServiceResult() throws Exception {
        StubTodoService service = new StubTodoService();
        service.todo = null;
        TodoController controller = controllerWith(service);

        assertNull(controller.retrieveTodo("alice", 7));
    }

    @Test
    public void addReturnsNoContentWhenServiceReturnsNull() throws Exception {
        StubTodoService service = new StubTodoService();
        service.addedTodo = null;
        TodoController controller = controllerWith(service);
        Todo todo = new Todo();

        ResponseEntity<?> response = controller.add("alice", todo);

        assertEquals(204, response.getStatusCodeValue());
    }
}
