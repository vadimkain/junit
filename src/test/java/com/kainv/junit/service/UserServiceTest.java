package com.kainv.junit.service;

import com.kainv.dto.User;
import com.kainv.service.UserService;
import org.hamcrest.MatcherAssert;
import org.hamcrest.collection.IsEmptyCollection;
import org.hamcrest.collection.IsMapContaining;
import org.junit.jupiter.api.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@Tag("fast")
@Tag("user")
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

        MatcherAssert.assertThat(users, IsEmptyCollection.empty());
        assertTrue(users.isEmpty(), () -> "User list should be empty");
    }

    @Test
    void usersSizeIfUserAdded() {
        System.out.println("Test 2: " + this);

        userService.add(VADIM);
        userService.add(PETR);

        // Делаем проверку на кол-во пользователей в приложении
        List<User> users = userService.getAll();

        assertThat(users).hasSize(2);
//        assertEquals(2, users.size());
    }

    @Test
    @Tag("login")
    void loginSuccessIfUserExists() {
        userService.add(VADIM);
        userService.add(PETR);

        Optional<User> maybeUser = userService.login(VADIM.getUsername(), VADIM.getPassword());

        // Проверяем, что такой пользователь существует
        assertThat(maybeUser).isPresent();
//        assertTrue(maybeUser.isPresent());
        // Проверяем, действительно ли это тот пользователь (первый параметр - ожидаемый, второй - фактический)
        maybeUser.ifPresent(user -> assertThat(user).isEqualTo(VADIM));
//        maybeUser.ifPresent(user -> assertEquals(VADIM, user));
    }

    @Test
    @Tag("login")
    void throwExceptionIfUsernameOrPasswordIsNull() {
        assertAll(
                () -> {
                    IllegalArgumentException argumentException = assertThrows(IllegalArgumentException.class, () -> userService.login(null, "some password"));
                    assertThat(argumentException.getMessage()).isEqualTo("username or password is null");
                },
                () -> assertThrows(IllegalArgumentException.class, () -> userService.login("some username", null))
        );
    }

    @Test
    @Tag("login")
    void loginFailIfPasswordIsNotCorrect() {
        userService.add(VADIM);

        Optional<User> maybeUser = userService.login(VADIM.getUsername(), "incorrect");

        assertTrue(maybeUser.isEmpty());
    }

    @Test
    void usersConvertedToMapById() {
        userService.add(VADIM, PETR);

        Map<Integer, User> users = userService.getAllConvertedById();

        MatcherAssert.assertThat(users, IsMapContaining.hasKey(VADIM.getId()));

        assertAll(
                () ->
                        // В результирующей коллекции проверяем на содержание ID для Вадима и Петра
                        assertThat(users).containsKeys(VADIM.getId(), PETR.getId()),
                () ->
                        // Проверяем Map не только на содержание, но и значений
                        assertThat(users).containsValues(VADIM, PETR)
        );
    }

    @Test
    @Tag("login")
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
