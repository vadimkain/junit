package com.kainv.junit.service;

import com.kainv.dto.User;
import com.kainv.junit.paramresolver.UserServiceParamResolver;
import com.kainv.service.UserService;
import org.hamcrest.MatcherAssert;
import org.hamcrest.collection.IsEmptyCollection;
import org.hamcrest.collection.IsMapContaining;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@Tag("fast")
@Tag("user")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.DisplayName.class)
@ExtendWith({
        UserServiceParamResolver.class
})
public class UserServiceTest {

    private UserService userService;
    private static final User VADIM = User.of(1, "Vadim", "123");
    private static final User PETR = User.of(2, "Petr", "123");

    UserServiceTest(TestInfo testInfo) {
        System.out.println();
    }

    @BeforeAll
    void init() {
        System.out.println("Before all" + this);
    }

    @BeforeEach
    void prepare(UserService userService) {
        System.out.println("Before each: " + this);
        this.userService = userService;
    }

    @Test
    @Order(1)
    @DisplayName("users will be empty if no user added")
    void usersEmptyIfNoUserAdded() {
        System.out.println("Test 1: " + this);

        List<User> users = userService.getAll();

        MatcherAssert.assertThat(users, IsEmptyCollection.empty());
        assertTrue(users.isEmpty(), () -> "User list should be empty");
    }

    @Test
    @Order(2)
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

    @AfterEach
    void deleteDataFromDatabase() {
        System.out.println("After each: " + this);
    }

    @AfterAll
    void closeConnectionPool() {
        System.out.println("After all" + this);
    }

    @Nested
    @DisplayName("test user login functionality")
    @Tag("login")
    @Timeout(value = 200, unit = TimeUnit.MILLISECONDS)
    class LoginTest {
        @Test
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
//        @org.junit.Test(expected = IllegalArgumentException.class)
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
        @Disabled("flaky, need to see")
        void loginFailIfPasswordIsNotCorrect() {
            userService.add(VADIM);

            Optional<User> maybeUser = userService.login(VADIM.getUsername(), "incorrect");

            assertTrue(maybeUser.isEmpty());
        }

        //        @Test
        @RepeatedTest(value = 5, name = RepeatedTest.LONG_DISPLAY_NAME)
        void loginFailIfUserDoesNotExist(RepetitionInfo repetitionInfo) {
            userService.add(VADIM);

            Optional<User> maybeUser = userService.login("Dima", VADIM.getPassword());

            assertTrue(maybeUser.isEmpty());
        }

        @Test
        void checkLoginFunctionalityPerfomance() {
            System.out.println(Thread.currentThread().getName());
//            Тестируем время выполнения нашего теста
            Optional<User> result = assertTimeoutPreemptively(
                    Duration.ofMillis(200L),
                    () -> {
                        System.out.println(Thread.currentThread().getName());
                        Thread.sleep(300L);
                        return userService.login("Dima", VADIM.getPassword());
                    }
            );

        }

        //        Для кастомных провайдеров
//        @ArgumentsSource()
//        Реализовывает NullArgumentsProvider
//        @NullSource
//        Реализовывает EmptyArgumentsProvider
//        @EmptySource
//        @NullAndEmptySource
//        Реализовывает ValueArgumentsProvider
//        @ValueSource(strings = {
//                "Vadim", "Petr"
//        })
//        @EnumSource
//        @MethodSource("com.kainv.junit.service.UserServiceTest#getArgumentsForLoginTest")
//        @CsvFileSource(resources = "/login-test-data.csv", delimiter = ',', numLinesToSkip = 1)
//        @CsvSource({
//                "Vadim,123",
//                "Petr,123"
//        })
        @ParameterizedTest(name = "{arguments} test")
        @MethodSource("com.kainv.junit.service.UserServiceTest#getArgumentsForLoginTest")
        @DisplayName("login param test")
        void loginParameterizedTest(String username, String password, Optional<User> user) {
            userService.add(VADIM, PETR);

            Optional<User> maybeUser = userService.login(username, password);

            assertThat(maybeUser).isEqualTo(user);
        }
    }

    static Stream<Arguments> getArgumentsForLoginTest() {
        return Stream.of(
                Arguments.of("Vadim", "123", Optional.of(VADIM)),
                Arguments.of("Petr", "123", Optional.of(PETR)),
//                    Существующий пользователь, но неправльный пароль
                Arguments.of("Petr", "dummy", Optional.empty()),
//                    Пользователя не существует, а пароль существует
                Arguments.of("dummy", "123", Optional.empty())
        );
    }
}
