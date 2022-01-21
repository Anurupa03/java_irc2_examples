package com.iconloop.score.example;

import score.Address;
import score.Context;
import score.annotation.External;
import score.annotation.Optional;

import java.math.BigInteger;

public class IRC2Pausable extends IRC2Basic{

    private boolean pause;
    public IRC2Pausable(String name, String symbol, int decimals) {
        super(name, symbol, decimals);
    }

    @External
    public void pause(){
        Context.require(Context.getCaller().equals(Context.getOwner()),"Pause: Only owner can pause token transfer");
        setPause(true);
    }

    @External
    public void unpause(){
        Context.require(Context.getCaller().equals(Context.getOwner()),"Pause: Only owner can unpause token transfer");
        setPause(false);
    }

    @External
    public void setPause(boolean pauseVal){
        this.pause = pauseVal;
    }

    @External
    public boolean getPause(){
        return this.pause;
    }

    @External
    public boolean pauseStatus(){
        return getPause();
    }

    @External
    // do i need to add @override here?
    public void transfer(Address to, BigInteger value, @Optional byte[] data){
        if (data == null){
            data ="transfer".getBytes();
        }
        Context.require(Context.getCaller().equals(Context.getOwner()),"Transfer:Only owners can transfer token");
        Context.require(!pauseStatus(),"Transfer: Paused token can not be transferred");
        super.transfer(to, value, data);
    }
}
