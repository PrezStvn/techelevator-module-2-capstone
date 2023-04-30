package com.techelevator.tenmo.controller;

import com.techelevator.tenmo.dao.account.AccountDao;
import com.techelevator.tenmo.dao.transaction.TransferDao;
import com.techelevator.tenmo.dao.user.UserDao;
import com.techelevator.tenmo.model.Transfer;
import com.techelevator.tenmo.service.TransferServiceLogic;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.security.Principal;


@RestController
@RequestMapping("/transfers")
@PreAuthorize("isAuthenticated()")
public class TransferController {
    private TransferDao dao;
    private AccountDao accountDao;
    private UserDao userDao;
    private TransferServiceLogic transferLogic;

    public TransferController(TransferDao transferDao, AccountDao accountDao, UserDao userDao) {
        this.dao = transferDao;
        this.accountDao = accountDao;
        this.userDao = userDao;
    }
// TODO need to implement transfer for user
    @RequestMapping(path = "/{id}", method = RequestMethod.GET)
    public Transfer get(@PathVariable int id) {
        Transfer transfer = dao.getTransfer(id);
        if (transfer == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Transfer Not Found");
        } else return transfer;
    }

    @ResponseStatus(HttpStatus.CREATED)
    @RequestMapping(path = "", method = RequestMethod.POST)
    public Transfer create(@Valid @RequestParam int senderId, @Valid @RequestParam("receiverId") int receiverId, @Valid @RequestParam BigDecimal transferAmount, Principal principal) {
        transferLogic = new TransferServiceLogic(dao, accountDao, userDao);
        return transferLogic.onCreateChecks(principal, senderId, receiverId, transferAmount);
    }

    @RequestMapping(path = "/{id}", method = RequestMethod.PUT)
    public Transfer update(@Valid @RequestBody Transfer transfer, @PathVariable int transferId) {
        Transfer updatedTransfer = dao.updateTransfer(transfer, transferId);
        if (updatedTransfer == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Transfer Not Found");
        } else if(transfer.getTransferId() != transferId) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "path does not match transfer id");
        } else return updatedTransfer;
    }

}
