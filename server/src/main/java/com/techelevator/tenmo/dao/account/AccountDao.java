package com.techelevator.tenmo.dao.account;

import com.techelevator.tenmo.model.Account;

import java.math.BigDecimal;
import java.util.List;

public interface AccountDao {
    List<Account> getAllAccounts();

    Account findByAccountId(int accountId);

    // TODO: step 4

    Account getBalance(int accountId);
    
    Account create(Account account);

    Account update(Account account, int userId);

    Account update(Account account);

    void delete(int accountId);
    
}
