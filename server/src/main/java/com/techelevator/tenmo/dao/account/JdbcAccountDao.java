package com.techelevator.tenmo.dao.account;

import com.techelevator.tenmo.TenmoApplication;
import com.techelevator.tenmo.exception.DaoException;
import com.techelevator.tenmo.model.Account;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.dao.DataAccessException;
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

    private static final Logger logger = LogManager.getLogger(TenmoApplication.class);

    private final JdbcTemplate jdbcTemplate;

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

    @Override
    public int findByAccountId(String username) {
        return 0;
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
    public boolean create(int userId, BigDecimal balance) {
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

        logger.info("Created account " + newAccountId + " for user " + userId);
        return true;
    }

    @Override
    public Account update(Account account, int userId) {
        return account;
    }

    @Override
    public Void delete(int accountId) {
        return null;
    }

    private Account mapRowToAccount(SqlRowSet rs) {
        Account account = new Account();
        account.setAccountId(rs.getInt("account_id"));
        account.setUserId(rs.getInt("user_id"));
        account.setBalance(rs.getBigDecimal("balance"));
        return account;
    }
}
