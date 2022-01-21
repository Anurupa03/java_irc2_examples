package com.iconloop.score.example;

import score.ArrayDB;

import java.math.BigInteger;

public class Snapshots {

    protected ArrayDB<BigInteger> ids;
    protected ArrayDB<BigInteger> values;

    public Snapshots(ArrayDB<BigInteger> ids, ArrayDB<BigInteger> values){
        this.ids = ids;
        this.values = values;
    }
}
