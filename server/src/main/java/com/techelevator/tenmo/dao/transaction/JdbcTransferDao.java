package com.techelevator.tenmo.dao.transaction;

import com.techelevator.tenmo.exception.DaoException;
import com.techelevator.tenmo.model.Account;
import com.techelevator.tenmo.model.Transfer;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class JdbcTransferDao implements TransferDao {

    private JdbcTemplate jdbcTemplate;

    public JdbcTransferDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // TODO: needs to be filled out (step 5)

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
    public Transfer createTransfer(Transfer transfer, int senderId, int receiverId, BigDecimal transferAmount) {

        Transfer newTransfer = null;
        String sql = "INSERT INTO transfers (sender_id, receiver_id, amount, status) " +
                "VALUES (?, ?, ?, 'Approved') " +
                "RETURNING transfer_id";

        if (senderId == receiverId) {
            throw new IllegalArgumentException("Sender and receiver cannot be the same");
        }
        if (transferAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Transfer amount must be greater than zero");
        }
        if (Account.getBalance().compareTo(transferAmount) < 0) {
            throw new IllegalArgumentException("Insufficient funds");
        }

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
