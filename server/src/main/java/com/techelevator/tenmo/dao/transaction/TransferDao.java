package com.techelevator.tenmo.dao.transaction;

import com.techelevator.tenmo.model.Transfer;

import java.util.List;

public interface TransferDao {

    void sendTransfer(Transfer transfer);

    List<Transfer> getSenders();

    List<Transfer> getReceivers();
}
