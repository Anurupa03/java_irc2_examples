package com.iconloop.score.example;
// follow transactionCount
import score.Context;
import score.VarDB;

import java.math.BigInteger;

public class Counters {

    protected final VarDB<BigInteger> value = Context.newVarDB("value",BigInteger.class);


//    public  void set(){
//
//        this.value.set(BigInteger.ZERO);
//    }

    public BigInteger current(){
        return this.value.get();
    }

    public void increment(){ // which one will it be?
        // difference between using this and value
        this.value.set(this.value.get().add(BigInteger.ONE));
//        value.get().add(BigInteger.ONE);
    }

    public void decrement(){
        Context.require(value.get().compareTo(BigInteger.ZERO)>0,"Counter:decrement overflow");
        this.value.set(current().subtract(BigInteger.ONE));
//        value.get().subtract(BigInteger.ONE);
    }

    public void reset(){
        this.value.set(BigInteger.ZERO);
    }

}
