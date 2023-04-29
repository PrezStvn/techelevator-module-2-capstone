package com.techelevator.tenmo.service;

import com.techelevator.tenmo.dao.account.AccountDao;
import com.techelevator.tenmo.dao.transaction.TransferDao;
import com.techelevator.tenmo.dao.user.UserDao;
import com.techelevator.tenmo.model.Account;
import com.techelevator.tenmo.model.Transfer;
import com.techelevator.tenmo.model.User;

import java.math.BigDecimal;
import java.security.Principal;

public class AccountServiceLogic {
    private UserDao userDao;
    private AccountDao accountDao;
    private User principalUser;

    public AccountServiceLogic(){};

    public AccountServiceLogic(UserDao userDao, AccountDao accountDao) {
        this.userDao = userDao;
        this.accountDao = accountDao;

    }

    /* TODO: Attempting to view account not owned by principal
     *
     */
    public boolean canIGet(Principal principal, int id) {
        Account account = accountDao.findByAccountId(id);
        User ownerOfTagetAccount = userDao.findByUserId(account.getUserId());
        User principalUser = userDao.findByUsername(principal.getName());
        return (principalUser.getId() == ownerOfTagetAccount.getId());
    }
}
