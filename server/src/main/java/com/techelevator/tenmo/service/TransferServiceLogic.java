package com.techelevator.tenmo.service;

import com.techelevator.tenmo.dao.account.AccountDao;
import com.techelevator.tenmo.dao.transaction.TransferDao;
import com.techelevator.tenmo.dao.user.UserDao;
import com.techelevator.tenmo.model.Account;
import com.techelevator.tenmo.model.Status;
import com.techelevator.tenmo.model.Transfer;
import com.techelevator.tenmo.model.User;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.List;

public class TransferServiceLogic {
    private UserDao userDao;
    private AccountDao accountDao;
    private TransferDao transferDao;

    /*
    * TODO:
    *  Logic: intake Authorizer(Principal principal, int senderId, int receiverId, BigDecimal transferAmount)
    *       needs to run query to retrieve principal userId and associated accounts
    *       check against both fromAcc & toAcc -> validation logic -> send or receive request +->
    *       transfer_status PENDING || APPROVED based on if principal == sender
    *  Check 2: make sure transferAmount is > 0.
    *  Check 3: ensure sender is not sending to self.
    *  Check 4: maybe account balance checker should be in transfer jdbc and make it on update
    *           this way we do not check to see if balance is high enough until money would actually move.
    *           unkown to or from acc
    *
    *
     */
    public TransferServiceLogic( TransferDao transferDao, AccountDao accountDao, UserDao userDao) {
        this.userDao = userDao;
        this.accountDao = accountDao;
        this.transferDao = transferDao;


    }


    public Transfer onCreateChecks(Principal principal,  int fromAcc, int toAcc, BigDecimal transferAmount){
        Transfer transfer = new Transfer();
        transfer.setSenderId(fromAcc);
        transfer.setReceiverId(toAcc);
        transfer.setTransferAmount(transferAmount);
        isValidTransfer(transfer.getSenderId(), transfer.getReceiverId(), transfer.getTransferAmount());
        transfer.setStatus(isTransferSentOrRequested(principal, fromAcc, toAcc));
        if(transfer.getStatus() == Status.DENIED) throw new BizLogicException("You cannot request a transfer between these accounts.");
        transferAmountChecker(transfer.getTransferAmount());
        if(transfer.getStatus() == Status.APPROVED) {
            preCompletionCheck(transfer);
            transferDao.updateTransfer(transfer, transfer.getTransferId());

        }

        transfer = transferDao.createTransfer(transfer.getSenderId(), transfer.getReceiverId(), transfer.getTransferAmount());

        return transfer;
    }
    private boolean isValidTransfer(int senderId, int receiverId, BigDecimal transferAmount) {
        Account senderAccount = accountDao.findByAccountId(senderId);
        Account receiverAccount = accountDao.findByAccountId(receiverId);

        if (senderId == receiverId) {
            throw new IllegalArgumentException("Sender and receiver cannot be the same");
        }
        if(senderAccount == null) {
            throw new IllegalArgumentException("Origin account does not exist");
        }
        if(receiverAccount == null) {
            throw new IllegalArgumentException("Target account does not exist");
        }
        // 5.7. I can't send a zero or negative amount.
        if (transferAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Transfer amount must be greater than zero");
        }
//  5.6. I can't send more TE Bucks than I have in my account.
        if (senderAccount.getBalance().compareTo(transferAmount) < 0) {
            throw new IllegalArgumentException("Insufficient funds");
        }

        return true;
    }

    public int isTransferSentOrRequested(Principal principal, int senderId, int receiverId) {
        User principalUser = userDao.findByUsername(principal.getName());
        List<Account> principalAccounts = accountDao.getAllMyAccounts(principalUser.getId());

        for(Account acc : principalAccounts) {
            if(acc.getAccountId() == receiverId) {
                return 1;
            } else if(acc.getAccountId() == senderId)
            {
                return 2;
            } else {
                throw new BizLogicException("Transfer not created: Neither account belongs to principal, please enter a valid account");
            }
        }
        return 3;
    }


    public boolean transferAmountChecker(BigDecimal amount) {
        if(amount.compareTo(BigDecimal.valueOf(0)) <= 0) throw new BizLogicException("cannot send an amount equal or less than 0.");
        if(amount.compareTo(new BigDecimal(".01")) <= 0) throw new BizLogicException("cannot send an amount less than .001");
        return true;
    }

    public boolean preCompletionCheck(Transfer transfer){
        Account senderAccount = accountDao.findByAccountId(transfer.getSenderId());
        BigDecimal senderBalance = senderAccount.getBalance();
        if(transfer.getTransferAmount().compareTo(senderBalance) > 0) throw new BizLogicException("Not enough funds at present to complete transfer.");
        return true;
    }



}
