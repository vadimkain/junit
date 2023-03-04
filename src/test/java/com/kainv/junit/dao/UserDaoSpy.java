package com.kainv.junit.dao;

import com.kainv.dao.UserDao;
import org.mockito.stubbing.Answer1;

import java.util.HashMap;
import java.util.Map;

public class UserDaoSpy extends UserDao {
    private final UserDao userDao;
    private Map<Integer, Boolean> answers = new HashMap<>();
    private Answer1<Integer, Boolean> answer1;

    public UserDaoSpy(UserDao userDao) {
        this.userDao = userDao;
    }

    @Override
    public boolean delete(Integer userId) {
//        invokation++
        return answers.getOrDefault(userId, userDao.delete(userId));
    }
}
