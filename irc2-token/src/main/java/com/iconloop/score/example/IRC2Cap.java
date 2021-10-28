package com.iconloop.score.example;

import com.iconloop.score.token.irc2.IRC2Basic;
import score.Address;
import score.Context;
import score.annotation.External;

import java.math.BigInteger;

public class IRC2Cap extends IRC2Basic {

    private BigInteger _cap;
    public IRC2Cap(String _name, String _symbol, int _decimals) {
        super(_name, _symbol, _decimals);
    }

    @External
    public void _mint(Address owner, BigInteger amount){

        BigInteger capValue = totalSupply().add(amount);
        if (capValue.compareTo(_cap) > 0 ){
            Context.revert("Cap exceeded ");
        }
//        Context.require(capValue.compareTo(_cap) <= 0,"Cap exceed");
        super._mint(owner, amount);
    }

    @External
    public BigInteger cap(){
        return this._cap;
    }
}
