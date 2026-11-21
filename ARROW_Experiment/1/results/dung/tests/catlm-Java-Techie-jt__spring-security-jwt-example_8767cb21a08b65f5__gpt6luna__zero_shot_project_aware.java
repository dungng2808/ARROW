package com.javatechie.jwt.api;

import com.javatechie.jwt.api.entity.User;
import com.javatechie.jwt.api.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class SpringSecurityJwtExampleApplication_ArrowTest {

    @Test
    public void initUsersSavesExpectedUsers() throws Exception {
        SpringSecurityJwtExampleApplication application = new SpringSecurityJwtExampleApplication();
        UserRepository repository = mock(UserRepository.class);

        Field repositoryField = SpringSecurityJwtExampleApplication.class.getDeclaredField("repository");
        repositoryField.setAccessible(true);
        repositoryField.set(application, repository);

        application.initUsers();

        org.mockito.ArgumentCaptor<List<User>> captor =
                org.mockito.ArgumentCaptor.forClass((Class) List.class);
        verify(repository).saveAll(captor.capture());

        List<User> users = captor.getValue();
        assertEquals(4, users.size());
        assertEquals(new User(101, "javatechie", "password", "javatechie@gmail.com"), users.get(0));
        assertEquals(new User(102, "user1", "pwd1", "user1@gmail.com"), users.get(1));
        assertEquals(new User(103, "user2", "pwd2", "user2@gmail.com"), users.get(2));
        assertEquals(new User(104, "user3", "pwd3", "user3@gmail.com"), users.get(3));
    }
}
