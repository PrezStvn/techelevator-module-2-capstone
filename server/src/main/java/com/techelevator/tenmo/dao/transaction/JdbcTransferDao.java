package com.techelevator.tenmo.dao.transaction;

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

    public JdbcTransferDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
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
// tODO may need to implement 3. A transfer includes the usernames of the from and to users and the amount of TE Bucks.
    // TODO 4. The receiver's account balance is increased by the amount of the transfer.
//   TODO 5. The sender's account balance is decreased by the amount of the transfer.
    @Override
    public Transfer createTransfer(Transfer transfer, int senderId, int receiverId, BigDecimal transferAmount) {
// 5.8. A Sending Transfer has an initial status of *Approved*.
        Transfer newTransfer = null;
        String sql = "INSERT INTO transfers (sender_id, receiver_id, amount, status) " +
                "VALUES (?, ?, ?, 'Approved') " +
                "RETURNING transfer_id";
        Account account = new Account();
        int senderAccount = transfer.getSenderId();
       // int receiverAccount = transfer.getReceiverId();
// step 5.2: I must not be allowed to send money to myself.
        if (senderId == receiverId) {
            throw new IllegalArgumentException("Sender and receiver cannot be the same");
        }
        // 5.7. I can't send a zero or negative amount.
        if (transferAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Transfer amount must be greater than zero");
        }
//  5.6. I can't send more TE Bucks than I have in my account.
        if (account.getBalance().compareTo(transferAmount) < 0) {
            throw new IllegalArgumentException("Insufficient funds");
        }

        // Update sender's account balance
        BigDecimal newSenderBalance = account.getBalance().subtract(transferAmount);
        account.setBalance(newSenderBalance);
        // TODO  // save updated account balance to the database

        try {
            Integer transferId = jdbcTemplate.queryForObject(sql, Integer.class,
                    transfer.getSenderId(), transfer.getReceiverId(), transferAmount);
            newTransfer = new Transfer(transferId, transfer.getSenderId(), transfer.getReceiverId(), transferAmount);
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


    private Transfer mapRowToTransfer(SqlRowSet rs) {
        Transfer transfer = new Transfer();
        transfer.setTransferId(rs.getInt("transfer_id"));
        transfer.setSenderId(rs.getInt("sender_id"));
        transfer.setReceiverId(rs.getInt("receiver_id"));
        transfer.setTransferAmount(rs.getBigDecimal("transfer_amount"));
        return transfer;
    }
}
