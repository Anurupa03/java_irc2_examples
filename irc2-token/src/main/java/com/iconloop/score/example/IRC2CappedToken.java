package com.iconloop.score.example;

import score.Context;
import java.math.BigInteger;

public class IRC2CappedToken extends IRC2Cap {
    public IRC2CappedToken(String _name, String _symbol, int _decimals, BigInteger _initialSupply, BigInteger _cap) {
        super(_name, _symbol, _decimals);

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
}
