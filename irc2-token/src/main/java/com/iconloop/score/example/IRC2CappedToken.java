package com.iconloop.score.example;

import com.iconloop.score.token.irc2.IRC2Basic;
import score.Address;
import score.Context;
import score.annotation.External;

import java.math.BigInteger;

public class IRC2CappedToken extends IRC2Basic {

    private BigInteger cap;
    public IRC2CappedToken(String _name, String _symbol, int _decimals, BigInteger _initialSupply, BigInteger _cap) {
        super(_name, _symbol, _decimals);
        this.cap = _cap.multiply(pow10(_decimals));

        // mint the initial token supply here
        Context.require(_initialSupply.compareTo(BigInteger.ZERO) >= 0);
        Context.require(_cap.compareTo(BigInteger.ZERO) >= 0);
        _mint(Context.getCaller(), _initialSupply.multiply(pow10(_decimals)));
    }

    private static BigInteger pow10(int exponent) {
        BigInteger result = BigInteger.ONE;
        for (int i = 0; i < exponent; i++) {
            result = result.multiply(BigInteger.TEN);
        }
        return result;
    }

    @External
    public void mint(BigInteger amount){
        Context.require(Context.getCaller().equals(Context.getOwner()),"Only owners can mint tokens");
        Context.require(cap().compareTo(super.totalSupply().add(amount)) >=0, "cap exceeded");
        super._mint(Context.getCaller(), amount);
    }

    @External
    public void mintTo(Address _to, BigInteger amount){
        Context.require(Context.getCaller().equals(Context.getOwner()),"Only owners can mint tokens");
        Context.require(cap().compareTo(super.totalSupply().add(amount)) >=0, "cap exceeded");
        super._mint(_to, amount);
    }

    @External(readonly = true)
    public BigInteger cap(){
        return this.cap;
    }

}
