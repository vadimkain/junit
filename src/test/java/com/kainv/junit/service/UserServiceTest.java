package com.kainv.junit.service;

import com.kainv.dto.User;
import com.kainv.service.UserService;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UserServiceTest {
    @Test
    void usersEmptyIfNoUserAdded() {
        UserService userService = new UserService();
        List<User> users = userService.getAll();
        assertFalse(users.isEmpty(), () -> "User list should be empty");
    }
}
