package com.iconloop.score.example;

import score.Address;
import score.annotation.External;
import score.annotation.Optional;

import java.math.BigInteger;

public class IRC2PausableToken extends IRC2Pausable{
    public IRC2PausableToken(String _name, String _symbol, int _decimals) {
        super(_name, _symbol, _decimals);
    }

    @External
    public void transfer(Address _to, BigInteger _value, @Optional byte[] _data){
        if (pauseStatus()){
            System.out.println("The token is pause");
        }
//        super.transfer();
    }
}
