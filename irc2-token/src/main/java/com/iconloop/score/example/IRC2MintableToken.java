package com.iconloop.score.example;

import com.iconloop.score.token.irc2.IRC2Mintable;
import score.Context;

import java.math.BigInteger;

public class IRC2MintableToken extends IRC2Mintable {
    public IRC2MintableToken(String _name, String _symbol, int _decimals, BigInteger _initialSupply) {
        super(_name, _symbol, _decimals);

        // mint initial token supply
        Context.require(_initialSupply.compareTo(BigInteger.ZERO) >= 0);
        _mint(Context.getCaller(), _initialSupply.multiply(pow10(_decimals)));
    }

    private static BigInteger pow10(int exponent) {
        BigInteger result = BigInteger.ONE;
        for (int i =0; i < exponent; i++) {
            result = result.multiply(BigInteger.TEN);
        }
        return result;
    }
}
