package com.techelevator.tenmo.dao.transaction;

import com.techelevator.tenmo.dao.account.AccountDao;
import com.techelevator.tenmo.exception.DaoException;
import com.techelevator.tenmo.model.Account;
import com.techelevator.tenmo.model.Transfer;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Component
public class JdbcTransferDao implements TransferDao {

    private JdbcTemplate jdbcTemplate;
    private AccountDao accountDao;

    public JdbcTransferDao(JdbcTemplate jdbcTemplate, AccountDao accountDao) {

        this.jdbcTemplate = jdbcTemplate;
        this.accountDao = accountDao;
    }

    // 6. As an authenticated user of the system, I need to be able to see transfers I have sent or received.

    @Override
    public List<Transfer> getAllTransfers() {
        List<Transfer> transfers = new ArrayList<>();
        String sql = "SELECT transfer_id, sender_id, receiver_id, transfer_amount FROM transfer";

        try {
            SqlRowSet results = jdbcTemplate.queryForRowSet(sql);
            while (results.next()){
                Transfer transfer = mapRowToTransfer(results);
                transfers.add(transfer);
            }
        } catch (CannotGetJdbcConnectionException e) {
            throw new DaoException("Unable to connect to server or database", e);
        } catch (BadSqlGrammarException e) {
            throw new DaoException("SQL syntax error", e);
        }

        return transfers;
    }
    // 7. 7. As an authenticated user of the system, I need to be able to retrieve the details of any transfer based upon the transfer ID.
    @Override
    public List<Transfer> getTransfersForUser(int userId) {
        List<Transfer> transfers = new ArrayList<>();
        String sql = "SELECT transfer_id, sender_id, receiver_id, transfer_amount FROM transfer WHERE userId=?";
        try {
            SqlRowSet results = jdbcTemplate.queryForRowSet(sql, userId);
            while (results.next()) {
                Transfer transfer = mapRowToTransfer(results);
                transfers.add(transfer);
            }
        } catch (CannotGetJdbcConnectionException e) {
            throw new DaoException("Unable to connect to server or database", e);
        } catch (BadSqlGrammarException e) {
            throw new DaoException("SQL syntax error", e);
        }
        return transfers;
    }

    @Override
    public Transfer getTransfer(int transferId) {
        Transfer transfer = null;
        String sql = "SELECT transfer_id, sender_id, receiver_id, transfer_amount FROM transfers WHERE transfer_id=?";
        try {
            SqlRowSet results = jdbcTemplate.queryForRowSet(sql, transferId);
            if (results.next()) {
                transfer = mapRowToTransfer(results);
            }
        } catch (CannotGetJdbcConnectionException e) {
            throw new DaoException("Unable to connect to server or database", e);
        } catch (BadSqlGrammarException e) {
            throw new DaoException("SQL syntax error", e);
        }
        return transfer;
    }
    // TODO see if receiver id exist
    @Override
    public Transfer createTransfer(int senderId, int receiverId, BigDecimal transferAmount) {
        if(isValidTransfer(senderId, receiverId, transferAmount));
// 5.8. A Sending Transfer has an initial status of *Approved*.
        Transfer newTransfer = null;
        String sql = "INSERT INTO transfers (sender_id, receiver_id, transfer_amount, transfer_status) " +
                "VALUES (?, ?, ?, 'APPROVED') " +
                "RETURNING transfer_id";

        // int receiverAccount = transfer.getReceiverId();
// step 5.2: I must not be allowed to send money to myself.

        Account senderAccount = accountDao.findByAccountId(senderId);
        Account receiverAccount = accountDao.findByAccountId(receiverId);



        // Update sender's account balance
        senderAccount.setBalance(senderAccount.getBalance().subtract(transferAmount));


        // Update receiver's account balance

        receiverAccount.setBalance(receiverAccount.getBalance().add(transferAmount));


        try {
            Integer transferId = jdbcTemplate.queryForObject(sql, Integer.class, senderId, receiverId, transferAmount);
            newTransfer = getTransfer(transferId);
            accountDao.update(senderAccount);
            accountDao.update(receiverAccount);
        } catch (CannotGetJdbcConnectionException e) {
            throw new DaoException("Unable to connect to server or database", e);
        } catch (BadSqlGrammarException e) {
            throw new DaoException("SQL syntax error", e);
        } catch (DataIntegrityViolationException e) {
            throw new DaoException("Data integrity violation", e);
        }

        return newTransfer;
    }

    @Override
    public Transfer updateTransfer(Transfer transfer, int transferId) {
        Transfer newTransfer = null;
        String sql = "UPDATE transfer SET sender_id=?, receiver_id=?, transfer_amount=? WHERE transfer_id=?";


        int senderId = transfer.getSenderId();
        int receiverId = transfer.getReceiverId();
        BigDecimal transferAmount = transfer.getTransferAmount();

        try {
            int rowsAffected = jdbcTemplate.update(sql, senderId, receiverId, transferAmount, transferId);
            if(rowsAffected == 0){
                throw new DaoException("ERROR updating reservation. Reservation not updated");
            } else {
                newTransfer = (Transfer) getTransfersForUser(transferId);
            }
        } catch (CannotGetJdbcConnectionException e) {
            throw new DaoException("Unable to connect to server or database", e);
        } catch (BadSqlGrammarException e) {
            throw new DaoException("SQL syntax error", e);
        } catch (DataIntegrityViolationException e) {
            throw new DaoException("Data integrity violation", e);
        }

        return newTransfer;
    }

    private boolean isValidTransfer(int senderId, int receiverId, BigDecimal transferAmount) {
        Account senderAccount = accountDao.findByAccountId(senderId);
        Account receiverAccount = accountDao.findByAccountId(receiverId);

        if (senderId == receiverId) {
            throw new IllegalArgumentException("Sender and receiver cannot be the same");
        }
        if(accountDao.findByAccountId(senderId) == null) {
            throw new IllegalArgumentException("Origin account does not exist");
        }
        if(accountDao.findByAccountId(receiverId) == null) {
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

    private Transfer mapRowToTransfer(SqlRowSet rs) {
        Transfer transfer = new Transfer();
        transfer.setTransferId(rs.getInt("transfer_id"));
        transfer.setSenderId(rs.getInt("sender_id"));
        transfer.setReceiverId(rs.getInt("receiver_id"));
        transfer.setTransferAmount(rs.getBigDecimal("transfer_amount"));
        return transfer;
    }
}
