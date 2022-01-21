package com.iconloop.score.example;

import score.Context;
import score.VarDB;

import java.math.BigInteger;

public class Counter {

    public final VarDB<BigInteger> value = Context.newVarDB("value",BigInteger.class);

    public BigInteger current(){
        return this.value.get();
    }

    public void increment(){
        this.value.set(this.value.get().add(BigInteger.ONE));
    }

    public void decrement(){
        Context.require(value.get().compareTo(BigInteger.ZERO)>0,"Counter:decrement overflow");
        this.value.set(current().subtract(BigInteger.ONE));
    }

    public void reset(){
        this.value.set(BigInteger.ZERO);
    }
}
