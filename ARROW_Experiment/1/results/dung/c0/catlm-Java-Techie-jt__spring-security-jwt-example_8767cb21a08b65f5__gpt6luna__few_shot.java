package com.javatechie.jwt.api;

import com.javatechie.jwt.api.entity.User;
import com.javatechie.jwt.api.repository.UserRepository;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

public class SpringSecurityJwtExampleApplication_ArrowTest {

    private static class RecordingUserRepository extends UserRepository {
        private List<User> savedUsers;

        @Override
        public <S extends User> List<S> saveAll(Iterable<S> entities) {
            savedUsers = new ArrayList<>();
            for (S entity : entities) {
                savedUsers.add(entity);
            }
            return savedUsers;
        }
    }

    @Test
    public void initUsersSavesFourExpectedUsers() throws Exception {
        SpringSecurityJwtExampleApplication application = new SpringSecurityJwtExampleApplication();
        RecordingUserRepository repository = new RecordingUserRepository();

        Field field = SpringSecurityJwtExampleApplication.class.getDeclaredField("repository");
        field.setAccessible(true);
        field.set(application, repository);

        application.initUsers();

        assertNotNull(repository.savedUsers);
        assertEquals(4, repository.savedUsers.size());
        assertEquals(Arrays.asList(101, 102, 103, 104), Arrays.asList(
                repository.savedUsers.get(0).getId(),
                repository.savedUsers.get(1).getId(),
                repository.savedUsers.get(2).getId(),
                repository.savedUsers.get(3).getId()));
        assertEquals(Arrays.asList("javatechie", "user1", "user2", "user3"), Arrays.asList(
                repository.savedUsers.get(0).getUsername(),
                repository.savedUsers.get(1).getUsername(),
                repository.savedUsers.get(2).getUsername(),
                repository.savedUsers.get(3).getUsername()));
        assertEquals(Arrays.asList("password", "pwd1", "pwd2", "pwd3"), Arrays.asList(
                repository.savedUsers.get(0).getPassword(),
                repository.savedUsers.get(1).getPassword(),
                repository.savedUsers.get(2).getPassword(),
                repository.savedUsers.get(3).getPassword()));
        assertEquals(Arrays.asList("javatechie@gmail.com", "user1@gmail.com", "user2@gmail.com", "user3@gmail.com"),
                Arrays.asList(
                        repository.savedUsers.get(0).getEmail(),
                        repository.savedUsers.get(1).getEmail(),
                        repository.savedUsers.get(2).getEmail(),
                        repository.savedUsers.get(3).getEmail()));
    }
}
