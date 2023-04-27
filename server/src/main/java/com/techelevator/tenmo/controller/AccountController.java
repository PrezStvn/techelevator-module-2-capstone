package com.techelevator.tenmo.controller;

import com.techelevator.tenmo.dao.account.AccountDao;

import com.techelevator.tenmo.model.Account;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import java.math.BigDecimal;

@RestController
@RequestMapping("/accounts")
@PreAuthorize("isAuthenticated()")
public class AccountController {
    private AccountDao dao;

    public AccountController(AccountDao accountDao) {
        this.dao = accountDao;
    }



    @RequestMapping(path = "/{id}", method = RequestMethod.GET)
    public Account get(@PathVariable int id) {
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
