package com.techelevator.tenmo.dao.account;

import com.techelevator.dao.BaseDaoTests;
import com.techelevator.tenmo.model.Account;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;

import static org.junit.Assert.*;

public class JdbcAccountDaoTest extends BaseDaoTests {

    private static final Account ACCOUNT_1 = new Account(2001, 1001, BigDecimal.valueOf(1000));
    private static final Account ACCOUNT_2 = new Account(2002, 1002, BigDecimal.valueOf(1001));
    private static final Account ACCOUNT_3 = new Account(2003, 1003, BigDecimal.valueOf(1003));

    private Account testAccount;

    private JdbcAccountDao sut;

    @Before
    public void setup() {
        sut = new JdbcAccountDao((JdbcTemplate) dataSource);
        testAccount = new Account(3001,4000,BigDecimal.valueOf(5000));
    }


    @Test
    public void findByAccountId_returns_correct_id() {
        Account account = sut.findByAccountId(2001);
        assertAccountsMatch(ACCOUNT_1, account);

        account = sut.findByAccountId(2);
        assertAccountsMatch(ACCOUNT_2, account);
    }

    @Test
    public void findByAccountId_returns_null_when_id_not_found() {
        Account account = sut.findByAccountId(23);
        Assert.assertNull(account);
    }

    @Test
    public void getBalance() {
    }

    @Test
    public void create_returns_account_with_id_and_expected_values() {
        Account createdAccount = sut.create(4000, BigDecimal.valueOf(5000));

        int newId = createdAccount.getAccountId();
        Assert.assertTrue(newId > 0);

        Account retrievedAccount = sut.findByAccountId(newId);
        assertAccountsMatch(createdAccount, retrievedAccount);
        assertAccountsMatch(createdAccount, testAccount);
    }

    @Test
    public void updated_account_has_expected_values_when_retrieved() {
        Account accountToUpdate = sut.findByAccountId(2001);

        accountToUpdate.setUserId(2004);
        accountToUpdate.setBalance(BigDecimal.valueOf(6000));

        sut.update(accountToUpdate);

        Account retrievedAccount = sut.findByAccountId(2001);
        assertAccountsMatch(accountToUpdate, retrievedAccount);
    }

    @Test
    public void deleted_account_cant_be_retrieved() {
        sut.delete(2002);

        Account retrievedAccount = sut.findByAccountId(2002);
        Assert.assertNull(retrievedAccount);
    }

    private void assertAccountsMatch(Account expected, Account actual) {
        Assert.assertEquals(expected.getAccountId(), actual.getAccountId());
        Assert.assertEquals(expected.getBalance(), actual.getBalance());
        Assert.assertEquals(expected.getUserId(), actual.getUserId());
    }
}