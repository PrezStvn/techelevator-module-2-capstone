package com.techelevator.tenmo.dao.account;

import com.techelevator.tenmo.exception.DaoException;
import com.techelevator.tenmo.model.Account;
import org.springframework.dao.DataAccessException;
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
public class JdbcAccountDao implements AccountDao{

<<<<<<< HEAD
    private static final Logger logger = LogManager.getLogger(TenmoApplication.class);

    private final JdbcTemplate jdbcTemplate;
=======
    private JdbcTemplate jdbcTemplate;
>>>>>>> main

    public JdbcAccountDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Account> getAllAccounts() {
        List<Account> accounts = new ArrayList<>();
        String sql = "SELECT account_id, user_id, balance FROM account";

        try {
            SqlRowSet results = jdbcTemplate.queryForRowSet(sql);
            while (results.next()){
                Account account = mapRowToAccount(results);
                accounts.add(account);
            }
        } catch (CannotGetJdbcConnectionException e) {
            throw new DaoException("Unable to connect to server or database", e);
        } catch (BadSqlGrammarException e) {
            throw new DaoException("SQL syntax error", e);
        }

        return accounts;
    }


    public Account findByAccountId(int accountId){
        String sql = "SELECT user_id, balance FROM account WHERE account_id=?;";
        SqlRowSet rowSet = jdbcTemplate.queryForRowSet(sql, accountId);
        if (rowSet.next()){
            return mapRowToAccount(rowSet);
        }
        throw new DaoException("Id not found");
    }

    @Override
    public Account get(int accountId) {
        Account account = null;
        String sql = "SELECT account_id, balance FROM account WHERE account_id = ?";

        try {
            SqlRowSet results = jdbcTemplate.queryForRowSet(sql, accountId);
            if (results.next()) {
                account = mapRowToAccount(results);
            }
        } catch (CannotGetJdbcConnectionException e) {
            throw new DaoException("Unable to connect to server or database", e);
        } catch (BadSqlGrammarException e) {
            throw new DaoException("SQL syntax error", e);
        }
        return account;
    }

    // TODO fix
    @Override
    public Integer create(int userId, BigDecimal balance) {
        String sql = "INSERT INTO account (user_id, balance) VALUES (?, ?) RETURNING account_id";
        Integer newAccountId = null;
        try {
            newAccountId = jdbcTemplate.queryForObject(sql, Integer.class, userId, balance);
        } catch (DataAccessException e) {
            throw new DaoException("Failed to create account for user " + userId, e);
        }
        if (newAccountId == null) {
            throw new DaoException("Failed to create account for user " + userId + ": no account ID returned");
        }
        return newAccountId;
    }

    @Override
    public Account update(Account account, int userId) {
        Account newAccount = null;
        String sql = "UPDATE account SET balance = ? WHERE account_id = ? AND user_id = ?";

        int accountId = account.getAccountId();
        BigDecimal balance = account.getBalance();

        try {
            int rowsAffected = jdbcTemplate.update(sql, balance, accountId, userId);
            if(rowsAffected == 0){
                throw new DaoException("ERROR updating account. Account not updated");
            } else {
                newAccount = findByAccountId(accountId);
            }
        } catch (CannotGetJdbcConnectionException e) {
            throw new DaoException("Unable to connect to server or database", e);
        } catch (BadSqlGrammarException e) {
            throw new DaoException("SQL syntax error", e);
        } catch (DataIntegrityViolationException e) {
            throw new DaoException("Data integrity violation", e);
        }

        return newAccount;
    }


    @Override
    public void delete(int accountId) {
        String sql = "DELETE FROM account WHERE account_id = ?";

        try {
            jdbcTemplate.update(sql, accountId);
        } catch (CannotGetJdbcConnectionException e) {
            throw new DaoException("Unable to connect to server or database", e);
        } catch (BadSqlGrammarException e) {
            throw new DaoException("SQL syntax error", e);
        } catch (DataIntegrityViolationException e) {
            throw new DaoException("Data integrity violation", e);
        }
    }

    private Account mapRowToAccount(SqlRowSet rs) {
        Account account = new Account();
        account.setAccountId(rs.getInt("account_id"));
        account.setUserId(rs.getInt("user_id"));
        account.setBalance(rs.getBigDecimal("balance"));
        return account;
    }
}
