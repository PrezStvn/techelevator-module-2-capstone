package com.techelevator.tenmo.dao.account;

import com.techelevator.tenmo.model.Account;

import java.util.List;

public interface AccountDao {
    List<Account> findAll();
// this is method where user can see account balance
    int findByAccountId(String username);

    boolean create(int userId);
}
