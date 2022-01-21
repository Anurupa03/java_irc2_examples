package com.iconloop.score.example;

import score.Context;

import java.math.BigInteger;

public class IRC2MintableToken extends IRC2Minatble{
    public IRC2MintableToken(String name, String symbol, int decimals, BigInteger initialSupply) {
        super(name, symbol, decimals);

        //mint the initial token supply here
        Context.require(initialSupply.compareTo(BigInteger.ZERO) >= 0);
        _mint(Context.getCaller(), initialSupply.multiply(pow10(decimals)));
    }

    private static BigInteger pow10(int exponent) {
        BigInteger result = BigInteger.ONE;
        for (int i = 0; i < exponent; i++) {
            result = result.multiply(BigInteger.TEN);
        }
        return result;
    }
}
