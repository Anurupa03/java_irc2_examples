package com.iconloop.score.example;


import score.Context;

import java.math.BigInteger;

public class IRC2BasicToken2 extends Basic{
    public IRC2BasicToken2(String name, String symbol, int decimals, BigInteger initialSupply) {
        super(name, symbol, decimals);

        // mint the initial token supply here
        Context.require(initialSupply.compareTo(BigInteger.ZERO) >= 0);
        super._mint(Context.getCaller(),initialSupply.multiply(BigInteger.TEN.pow(decimals)));
    }

}
