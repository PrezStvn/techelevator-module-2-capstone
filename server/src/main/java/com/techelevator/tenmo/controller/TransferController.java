package com.techelevator.tenmo.controller;

import com.techelevator.tenmo.dao.transaction.TransferDao;
import com.techelevator.tenmo.model.Transfer;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import java.math.BigDecimal;


@RestController
@RequestMapping("/transfer")
@PreAuthorize("isAuthenticated()")
public class TransferController {
    private TransferDao dao;

    public TransferController(TransferDao transferDao) {
        this.dao = transferDao;
    }
// TODO need to implement transfer for user
    @RequestMapping(path = "/{id}", method = RequestMethod.GET)
    public Transfer get(@PathVariable int transferId) {
        Transfer transfer = dao.getTransfer(transferId);
        if (transfer == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Transfer Not Found");
        } else return transfer;
    }

    @ResponseStatus(HttpStatus.CREATED)
    @RequestMapping(path = "", method = RequestMethod.POST)
    public Transfer create(@Valid @RequestParam int senderId, @Valid @RequestParam int receiverId, @Valid @RequestParam BigDecimal transferAmount) {
        return dao.createTransfer(senderId, receiverId, transferAmount);
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
