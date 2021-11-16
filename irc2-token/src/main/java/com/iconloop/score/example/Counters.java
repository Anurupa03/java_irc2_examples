package com.iconloop.score.example;
// follow transactionCount
import score.Context;
import score.VarDB;

import java.math.BigInteger;

public class Counters {

    private final VarDB<BigInteger> value = Context.newVarDB("value",BigInteger.class);

    public BigInteger current(){
        return value.get();
    }

    public void increment(){
        value.get().add(BigInteger.ONE);
    }

    public void decrement(){
        Context.require(value.get().compareTo(BigInteger.ZERO)>0,"Counter:decrement overflow");
        value.get().subtract(BigInteger.ONE);
    }

    public void reset(){
        value.set(BigInteger.ZERO);
    }

}
