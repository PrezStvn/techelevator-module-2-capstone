package com.techelevator.tenmo.authorizer;

import com.techelevator.tenmo.dao.account.AccountDao;
import com.techelevator.tenmo.dao.transaction.TransferDao;
import com.techelevator.tenmo.dao.user.UserDao;
import com.techelevator.tenmo.model.Account;
import com.techelevator.tenmo.model.Transfer;
import com.techelevator.tenmo.model.User;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.List;

public class Authorizer {
    private UserDao userDao;
    private AccountDao accountDao;
    private TransferDao transferDao;
    private Transfer transfer;
    private User transferSubmitter;
    /*
    * TODO:
    *  Logic: intake Authorizer(Principal principal, int senderId, int receiverId, BigDecimal transferAmount)
    *       needs to run query to retrieve principal userId and associated accounts
    *       check against both fromAcc & toAcc -> validation logic -> send or receive request +->
    *       transfer_status PENDING || APPROVED based on if principal == sender
    *  Check 2: make sure transferAmount is > 0.
    *  Check 3: ensure sendr is not sending to self.
    *  Check 4: maybe account balance checker should be in transfer jdbc and make it on update
    *           this way we do not check to see if balance is high enough until money would actually move.
    *
     */
    public Authorizer(UserDao userDao, AccountDao accountDao, TransferDao transferDao, Principal principal, int fromAcc, int toAcc, BigDecimal transferAmount) {
        this.userDao = userDao;
        this.accountDao = accountDao;
        this.transferDao = transferDao;
        this.transferSubmitter = userDao.findByUsername(principal.getName());
        transfer.setSenderId(fromAcc);
        transfer.setReceiverId(toAcc);
        transfer.setTransferAmount(transferAmount);
    }

    public void isTransferSentOrRequested() {
        List<Account> principalAccounts = accountDao.getAllMyAccounts(transferSubmitter.getId());

        for(Account acc : principalAccounts) {
            if(acc.getAccountId() == transfer.getSenderId()) {
                transfer.setStatus(2);
            } else if(acc.getAccountId() == transfer.getReceiverId())
            {
                transfer.setStatus(1);
            }
        }
    }



}
