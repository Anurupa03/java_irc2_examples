package com.iconloop.score.example;

import score.Address;
import score.Context;
import score.annotation.External;

import java.math.BigInteger;

public class IRC2CappedToken extends Basic {

    private final BigInteger cap;
    public IRC2CappedToken(String name, String symbol, int decimals, BigInteger initialSupply, BigInteger cap) {
        super(name, symbol, decimals);
        this.cap = cap.multiply(pow10(decimals));

        // mint the initial token supply here
        Context.require(initialSupply.compareTo(BigInteger.ZERO) >= 0);
        Context.require(cap.compareTo(BigInteger.ZERO) >= 0);
        _mint(Context.getCaller(), initialSupply.multiply(pow10(decimals)));
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
        Context.require(Context.getCaller().equals(Context.getOwner()),"Only owner can mint tokens");
        Context.require(cap().compareTo(super.totalSupply().add(amount)) >=0, "Cap:capped exceeded");
        super._mint(Context.getCaller(), amount);
    }

    @External
    public void mintTo(Address to, BigInteger amount){
        Context.require(Context.getCaller().equals(Context.getOwner()),"Only owners can mint tokens");
        Context.require(cap().compareTo(super.totalSupply().add(amount)) >=0, "Cap:capped exceeded");
        super._mint(to, amount);
    }

    @External(readonly = true)
    public BigInteger cap(){
        return this.cap;
    }

}
