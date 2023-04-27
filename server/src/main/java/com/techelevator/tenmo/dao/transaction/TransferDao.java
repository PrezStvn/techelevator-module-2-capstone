package com.techelevator.tenmo.dao.transaction;

import com.techelevator.tenmo.model.Transfer;

import java.math.BigDecimal;
import java.util.List;

public interface TransferDao {

    List<Transfer> getAllTransfers();

    List<Transfer> getTransfersForUser(int transferId);

    // these methods work together to implement the sending

    Transfer getTransfer(int transferId);

    Transfer createTransfer(int senderId, int receiverId, BigDecimal transferAmount);

    Transfer updateTransfer(Transfer transfer, int transferId);

}
