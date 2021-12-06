package com.iconloop.score.example;

import score.*;

import java.math.BigInteger;

public class ApproveUnits {
    public VarDB<Boolean> isValid = Context.newVarDB("isValid",Boolean.class);
    public DictDB<Address,BigInteger> approvals = Context.newDictDB("approvals",BigInteger.class);
//    public DictDB<BigInteger,Boolean> isValid = Context.newDictDB("isValid",boolean.class);
//    public BranchDB<Address, DictDB<Address,BigInteger>> approvals = Context.newBranchDB("approvals",BigInteger.class);


   }
