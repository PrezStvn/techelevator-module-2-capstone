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
    *  really flesh out exceptions thrown with ppoper messagin and and actual exception passes.
    *   some potentially redundant methods currently present.
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
        }

        transfer = transferDao.createTransfer(transfer.getSenderId(), transfer.getReceiverId(), transfer.getTransferAmount(), transfer.getStatus());

        return transfer;
    }

    public Transfer onGetChecks(Principal principal, int transferId) {
        Transfer transfer = transferDao.getTransfer(transferId);
        if(isSenderOrReciever(principal, transfer)) {
            return transfer;
        } else throw new BizLogicException("You have no business viewing transactions that do not involve you");
    }

    //TODO: error on sender changing transaction status if not PENDING
    public boolean onUpdateChecks(Principal principal, Transfer transfer) {
        Transfer currentTransferFromDB = transferDao.getTransfer(transfer.getTransferId());
        if(currentTransferFromDB.getStatus() == Status.PENDING) {
            if(isSender(principal, transfer)) {
                transferDao.updateTransfer(transfer);
                return true;
            }
        } else throw new BizLogicException("The selected transaction is already completed");

        return false;
    }

    public boolean isSender(Principal principal, Transfer transfer) {
        User principalUser = userDao.findByUsername(principal.getName());
        List<Account> principalAccounts = accountDao.getAllMyAccounts(principalUser.getId());
        for(Account acc : principalAccounts) {
            if(acc.getAccountId() == transfer.getSenderId()) return true;
        }
        throw new BizLogicException("You cannot approve or deny this transfer, you are not the owner of the from account");
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

    public Status isTransferSentOrRequested(Principal principal, int senderId, int receiverId) {
        User principalUser = userDao.findByUsername(principal.getName());
        List<Account> principalAccounts = accountDao.getAllMyAccounts(principalUser.getId());

        for(Account acc : principalAccounts) {
            if(acc.getAccountId() == receiverId) {
                return Status.PENDING;
            } else if(acc.getAccountId() == senderId)
            {
                return Status.APPROVED;
            }
        }
        return Status.DENIED;
    }

    public boolean isSenderOrReciever(Principal principal, Transfer transfer) {
        User principalUser = userDao.findByUsername(principal.getName());
        List<Account> principalAccounts = accountDao.getAllMyAccounts(principalUser.getId());
        for(Account acc : principalAccounts)     {
            if(acc.getAccountId() == transfer.getReceiverId() || acc.getAccountId() == transfer.getSenderId()) return true;
        }
        return false;
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
