package com.iconloop.score.example;

import score.Context;
import score.annotation.External;

import java.math.BigInteger;

public class IRC2Burnable extends  Basic{
    public IRC2Burnable(String name, String symbol, int decimals) {
        super(name, symbol, decimals);
    }

    @External
    public void burn(BigInteger amount) {
        _burn(Context.getCaller(), amount);
    }
}
