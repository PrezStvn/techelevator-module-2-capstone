package com.techelevator.tenmo.model;

import org.springframework.data.relational.core.mapping.Column;

import java.math.BigDecimal;

public class Transfer {
    private int transferId;
    private int senderId;
    private int receiverId;
    private BigDecimal transferAmount;
    // approved or not

    private Status status;



    public Transfer(){}

    public Transfer(int transferId, int senderId, int receiverId, BigDecimal transferAmount, Status status){
        this.transferId = transferId;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.transferAmount = transferAmount;
        this.status = status;
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

    public void setStatus(Status status) {
        this.status = status;
    }
}
