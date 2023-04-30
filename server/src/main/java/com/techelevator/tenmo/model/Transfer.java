package com.techelevator.tenmo.model;

import java.math.BigDecimal;

public class Transfer {
    private int transferId;
    private int senderId;
    private int receiverId;
    private BigDecimal transferAmount;
    // approved or not
    private Status status = Status.PENDING;



    public Transfer(){}

    public Transfer(int transferId, int senderId, int receiverId, BigDecimal transferAmount){
        this.transferId = transferId;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.transferAmount = transferAmount;
    }

    public int getTransferId() {
        return transferId;
    }

    public void setTransferId(int transferId) {
        this.transferId = transferId;
    }

    public int getSenderId() {
        return senderId;
    }

    public void setSenderId(int senderId) {
        this.senderId = senderId;
    }

    public int getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(int receiverId) {
        this.receiverId = receiverId;
    }

    public BigDecimal getTransferAmount() {
        return transferAmount;
    }

    public void setTransferAmount(BigDecimal transferAmount) {
        this.transferAmount = transferAmount;
    }

    public Status getStatus() {return status;}

    public void setStatus(int i) {
        switch(i) {
            case 1: this.status = Status.PENDING;
            break;
            case 2: this.status = Status.APPROVED;
            break;
            case 3: this.status = Status.DENIED;
        }
    }
}
