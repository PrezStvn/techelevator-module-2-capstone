package com.techelevator.tenmo.controller;

import com.techelevator.tenmo.dao.account.AccountDao;

import com.techelevator.tenmo.dao.user.UserDao;
import com.techelevator.tenmo.exception.DaoException;
import com.techelevator.tenmo.model.Account;
import com.techelevator.tenmo.model.User;
import com.techelevator.tenmo.service.AccountServiceLogic;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/account")
@PreAuthorize("isAuthenticated()")
public class AccountController {
    private AccountDao dao;
    private UserDao userDao;
    private AccountServiceLogic logic ;


    public AccountController(AccountDao accountDao, UserDao userDao) {
        this.dao = accountDao;
        this.userDao = userDao;
        logic = new AccountServiceLogic(userDao, accountDao);
    }

    @RequestMapping(path = "s", method = RequestMethod.GET)
    public List<String> listOfUsers() {
        List<String> users = new ArrayList<>();
        for(User user : userDao.findAll()) {
            users.add(user.getUsername());
        }
        return users;
    }
    //TODO: once again could rewrite to obfuscate any data being sent between client and server;
    @RequestMapping(path = "/{id}", method = RequestMethod.GET)
    public Account get(@PathVariable int id, Principal principal) {
        if(!(logic.canIGet(principal, id))) {
            throw new DaoException("You do not have access to this account");
        }
            Account account = dao.findByAccountId(id);

        if (account == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Account Not Found");
        } else return account;
    }

    @ResponseStatus(HttpStatus.CREATED)
    @RequestMapping(path = "", method = RequestMethod.POST)
    public Account create(@Valid @RequestParam int userId, @Valid @RequestParam BigDecimal balance) {
        return dao.create(userId, balance);
    }

    @RequestMapping(path = "/{id}", method = RequestMethod.PUT)
    //@PreAuthorize("hasAnyRole('ADMIN', 'CREATOR')")
    public Account update(@Valid @RequestBody Account account, @PathVariable int id) {
        Account updatedAccount = dao.update(account);
        if (updatedAccount == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Account Not Found");
        } else if(account.getAccountId() != id) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "path does not match accountId");
        } else return updatedAccount;
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @RequestMapping(path = "/{id}", method = RequestMethod.DELETE)
    @PreAuthorize("hasAnyRole('ADMIN')")
    public void delete(@PathVariable int id) {
        dao.delete(id);
    }
}
