package com.techelevator.tenmo.dao.transaction;

import com.techelevator.tenmo.dao.account.AccountDao;
import com.techelevator.tenmo.exception.DaoException;
import com.techelevator.tenmo.model.Account;
import com.techelevator.tenmo.model.Status;
import com.techelevator.tenmo.model.Transfer;
import com.techelevator.tenmo.service.TransferServiceLogic;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SqlParameterValue;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static java.sql.Types.VARCHAR;

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
        String sql = "SELECT transfer_id, sender_id, receiver_id, transfer_amount, transfer_status FROM transfers WHERE transfer_id=?";
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
    public Transfer createTransfer(int senderId, int receiverId, BigDecimal transferAmount, Status status)  {

        Transfer newTransfer = null;
        String sql = "INSERT INTO transfers (transfer_status, sender_id, receiver_id, transfer_amount) " +
                "VALUES ('" + status.toString() + "', ?, ?, ?) " +
                "RETURNING transfer_id";

        try {
            Integer transferId = jdbcTemplate.queryForObject(sql, Integer.class, senderId, receiverId, transferAmount);
            newTransfer = getTransfer(transferId);
            if(newTransfer.getStatus() == Status.APPROVED) {
                transferApproved(senderId, receiverId, transferAmount);
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

    public void transferApproved(int fromAcc, int toAcc, BigDecimal transferAmount) {
        Account senderAccount = accountDao.findByAccountId(fromAcc);
        Account receiverAccount = accountDao.findByAccountId(toAcc);
        // Update sender's account balance
        senderAccount.setBalance(senderAccount.getBalance().subtract(transferAmount));
        // Update receiver's account balance
        receiverAccount.setBalance(receiverAccount.getBalance().add(transferAmount));
        accountDao.update(senderAccount);
        accountDao.update(receiverAccount);
        System.out.println("accounts updated after transfer");
    }

    @Override
    public Transfer updateTransfer(Transfer transfer) {
        Transfer newTransfer = null;
        String sql = "UPDATE transfer SET transfer_status = ? WHERE transfer_id=?";


        int senderId = transfer.getSenderId();
        int receiverId = transfer.getReceiverId();
        BigDecimal transferAmount = transfer.getTransferAmount();

        try {
            int rowsAffected = jdbcTemplate.update(sql, transfer.getStatus(), transfer.getTransferId());
            if(rowsAffected == 0){
                throw new DaoException("ERROR updating reservation. Reservation not updated");
            } else {
                newTransfer = (Transfer) getTransfersForUser(transfer.getTransferId());
                if(newTransfer.getStatus() == Status.APPROVED) transferApproved(newTransfer.getSenderId(), newTransfer.getReceiverId(), newTransfer.getTransferAmount());
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
        transfer.setStatus(Status.valueOf(rs.getString("transfer_status")));
        return transfer;
    }
}
