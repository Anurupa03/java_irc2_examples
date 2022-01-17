package com.iconloop.score.example;

import score.Address;
import score.ArrayDB;
import score.Context;

import java.math.BigInteger;

public class Snapshots{

    protected ArrayDB<BigInteger> ids;
    protected ArrayDB<BigInteger> values;

    public Snapshots(ArrayDB<BigInteger> ids, ArrayDB<BigInteger> values){
        this.ids = ids;
        this.values = values;
    }
    // keys in enmurableintmap is the address
    // entries ra values is id and values

    public Snapshots(Address account){
        ArrayDB<BigInteger> ids = Context.newArrayDB(account+"ids",BigInteger.class);
        ArrayDB<BigInteger> values = Context.newArrayDB(account+"values",BigInteger.class);
    }



}

// getters and setters here