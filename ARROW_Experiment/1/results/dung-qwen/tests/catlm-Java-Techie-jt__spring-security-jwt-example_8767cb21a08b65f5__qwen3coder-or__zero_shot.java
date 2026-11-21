package com.javatechie.jwt.api;

import com.javatechie.jwt.api.entity.User;
import com.javatechie.jwt.api.repository.UserRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class SpringSecurityJwtExampleApplication_ArrowTest {

    @Mock
    private UserRepository repository;

    @Test
    public void testInitUsers() {
        SpringSecurityJwtExampleApplication application = new SpringSecurityJwtExampleApplication();
        application.repository = repository;

        application.initUsers();

        verify(repository, times(1)).saveAll(Mockito.anyList());
    }
}
