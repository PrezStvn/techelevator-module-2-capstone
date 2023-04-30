package com.techelevator.tenmo.service;

public class BizLogicException extends RuntimeException{
    public BizLogicException(){
        super();
    }

    public BizLogicException(String message){
        super(message);
    }

}
