package com.techelevator.tenmo.dao.user;

import com.techelevator.tenmo.model.User;

import java.util.List;

public interface UserDao {

    List<User> findAll();

    User findByUsername(String username);

    User findByUserId(int id);

    int findIdByUsername(String username);

    boolean create(String username, String password);
}

