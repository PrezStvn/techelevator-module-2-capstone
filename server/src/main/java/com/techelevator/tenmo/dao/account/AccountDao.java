package com.techelevator.tenmo.dao.account;

import com.techelevator.tenmo.model.Account;

import java.math.BigDecimal;
import java.util.List;

public interface AccountDao {
    List<Account> getAllAccounts();

    int findByAccountId(String username);



    // TODO: step 4

    Account get(int accountId);

    boolean create(int userId, BigDecimal balance);

    Account update(Account account, int userId);

    Void delete(int accountId);

}
