package com.javatechie.jwt.api;

import com.javatechie.jwt.api.entity.User;
import com.javatechie.jwt.api.repository.UserRepository;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class SpringSecurityJwtExampleApplication_ArrowTest {

    @Test
    public void initUsersSavesFourUsers() throws Exception {
        SpringSecurityJwtExampleApplication application = new SpringSecurityJwtExampleApplication();
        RecordingUserRepository repository = new RecordingUserRepository();

        Field field = SpringSecurityJwtExampleApplication.class.getDeclaredField("repository");
        field.setAccessible(true);
        field.set(application, repository);

        application.initUsers();

        assertNotNull(repository.savedUsers);
        assertEquals(4, repository.savedUsers.size());
    }

    private static class RecordingUserRepository implements UserRepository {
        private List<User> savedUsers;

        @Override
        public <S extends User> List<S> saveAll(Iterable<S> entities) {
            savedUsers = new ArrayList<S>();
            for (S entity : entities) {
                savedUsers.add(entity);
            }
            return savedUsers;
        }
    }
}
