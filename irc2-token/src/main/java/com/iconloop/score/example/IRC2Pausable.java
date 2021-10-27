package com.iconloop.score.example;

import com.iconloop.score.token.irc2.IRC2Basic;
import score.Address;
import score.Context;
import score.annotation.External;
import score.annotation.Optional;

import java.math.BigInteger;

public class IRC2Pausable extends IRC2Basic {

    private boolean _pause;
    public IRC2Pausable(String _name, String _symbol, int _decimals) {
        super(_name, _symbol, _decimals);
    }

    @External
    public void pause(){
        Context.require(Context.getCaller().equals(Context.getOwner()),"Only owners can pause");
        setPause(true);
    }

    @External
    public void unpause(){
        Context.require(Context.getCaller().equals(Context.getOwner()),"Only owner can unpause");
        setPause(false);
    }

    @External
    public void setPause(boolean _pause){
        this._pause = _pause;
    }

    @External
    public boolean getPause(){
        return this._pause;
    }

    @External
    public boolean pauseStatus(){
        return getPause();
    }

    @External
    public void transfer(Address _to, BigInteger _value, @Optional byte[] _data){
        Context.require(Context.getCaller().equals(Context.getOwner()),"Only owners can transfer tokens");
        Context.require(!pauseStatus(),"The token is already paused");
        super.transfer(_to, _value, _data);
    }

}
