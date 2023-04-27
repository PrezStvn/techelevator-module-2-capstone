package com.techelevator.tenmo.controller;

import com.techelevator.tenmo.dao.account.AccountDao;
import com.techelevator.tenmo.dao.account.JdbcAccountDao;
import com.techelevator.tenmo.model.Account;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/accounts")
@PreAuthorize("isAuthenticated()")
public class AccountController {
    private AccountDao dao;
    public AccountController(AccountDao accountDao) { this.dao = accountDao;}

    //CRUD
    @PreAuthorize("permitAll")
    @RequestMapping(path = "/{id}", method =  RequestMethod.GET)
    public Account get(@PathVariable int id) {
        Account account = dao.get(id);
        if(account == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Account Not Found");
        } else return account;
    }


}
