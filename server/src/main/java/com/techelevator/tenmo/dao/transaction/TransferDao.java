package com.techelevator.tenmo.dao.transaction;

import com.techelevator.tenmo.model.Transfer;

import java.math.BigDecimal;
import java.util.List;

public interface TransferDao {

    List<Transfer> getAllTransfers();

    List<Transfer> getTransfersForUser(int transferId);

    // these methods work together to implement the sending

    Transfer createTransfer(Transfer transfer, int senderId, int receiverId, BigDecimal transferAmount);

    // make this boolean so that we can return approved status
    Transfer updateTransfer(Transfer transfer, int transferId);

}
