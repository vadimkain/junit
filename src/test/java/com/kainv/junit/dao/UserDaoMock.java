package com.kainv.junit.dao;

import com.kainv.dao.UserDao;
import org.mockito.stubbing.Answer1;

import java.util.HashMap;
import java.util.Map;

public class UserDaoMock extends UserDao {

    // Ключ Integer потому что аргумент типа Integer и значение Boolean потому что метод возвращает boolean
    private Map<Integer, Boolean> answers = new HashMap<>();
    private Answer1<Integer, Boolean> answer1;
    @Override
    public boolean delete(Integer userId) {
        return answers.getOrDefault(userId, false);
    }
}
