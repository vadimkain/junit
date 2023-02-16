package com.kainv.junit.service;

import com.kainv.dto.User;
import com.kainv.service.UserService;
import org.junit.jupiter.api.*;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class UserServiceTest {

    private UserService userService;
    private static final User VADIM = User.of(1, "Vadim", "123");
    private static final User PETR = User.of(2, "Petr", "123");

    @BeforeAll
    void init() {
        System.out.println("Before all" + this);
    }

    @BeforeEach
    void prepare() {
        System.out.println("Before each: " + this);
        userService = new UserService();
    }

    @Test
    void usersEmptyIfNoUserAdded() {
        System.out.println("Test 1: " + this);
        List<User> users = userService.getAll();
        assertTrue(users.isEmpty(), () -> "User list should be empty");
    }

    @Test
    void usersSizeIfUserAdded() {
        System.out.println("Test 2: " + this);

        userService.add(VADIM);
        userService.add(PETR);

        // Делаем проверку на кол-во пользователей в приложении
        List<User> users = userService.getAll();

        assertEquals(2, users.size());
    }

    @Test
    void loginSuccessIfUserExists() {
        userService.add(VADIM);
        userService.add(PETR);

        Optional<User> maybeUser = userService.login(VADIM.getUsername(), VADIM.getPassword());

        // Проверяем, что такой пользователь существует
        assertTrue(maybeUser.isPresent());
        // Проверяем, действительно ли это тот пользователь (первый параметр - ожидаемый, второй - фактический)
        maybeUser.ifPresent(user -> assertEquals(VADIM, user));
    }

    @Test
    void loginFailIfPasswordIsNotCorrect() {
        userService.add(VADIM);

        Optional<User> maybeUser = userService.login(VADIM.getUsername(), "incorrect");

        assertTrue(maybeUser.isEmpty());
    }

    @Test
    void loginFailIfUserDoesNotExist() {
        userService.add(VADIM);

        Optional<User> maybeUser = userService.login("Dima", VADIM.getPassword());

        assertTrue(maybeUser.isEmpty());
    }

    @AfterEach
    void deleteDataFromDatabase() {
        System.out.println("After each: " + this);
    }

    @AfterAll
    void closeConnectionPool() {
        System.out.println("After all" + this);
    }
}
