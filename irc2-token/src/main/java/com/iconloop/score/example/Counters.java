package com.iconloop.score.example;

import score.Context;

import java.math.BigInteger;

public class Counters {

    protected BigInteger _value;

    public BigInteger current(){
        return this._value;
    }

    public void increment(){
        _value = _value.add(BigInteger.ONE);
    }

    public void decrement(){
        Context.require(_value.compareTo(BigInteger.ZERO)>0,"Counter:decrement overflow");
        _value = _value.subtract(BigInteger.ONE);
    }

    public void reset(){
        _value = BigInteger.ZERO;
    }


}
