package com.javatechie.jwt.api;

import com.javatechie.jwt.api.entity.User;
import com.javatechie.jwt.api.repository.UserRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class SpringSecurityJwtExampleApplication_ArrowTest {

    @Mock
    private UserRepository repository;

    @InjectMocks
    private SpringSecurityJwtExampleApplication application;

    @Test
    public void testInitUsers() {
        application.initUsers();
        verify(repository).saveAll(org.mockito.ArgumentMatchers.anyList());
    }
}
