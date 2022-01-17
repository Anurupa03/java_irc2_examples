package com.iconloop.score.example;

import score.Address;
import score.Context;
import score.VarDB;
import score.annotation.External;

import java.math.BigInteger;

public class IRC2Mintable extends Basic {

    private final VarDB<Address> minter = Context.newVarDB("minter", Address.class);

    public IRC2Mintable(String name, String symbol, int decimals) {
        super(name, symbol, decimals);
        // By default, set the minter role to the owner
        if (minter.get() == null) {
            minter.set(Context.getOwner());
        }
    }
    @External
    public void mint(BigInteger amount){
        // mint new tokens
        Context.require(Context.getCaller().equals(minter.get()),
                "Mint: minters or owners can only mint new tokens");
        _mint(Context.getCaller(), amount);
    }

    @External
    public void mintTo(Address account, BigInteger amount) {
        // mint new tokens to address
        Context.require(Context.getCaller().equals(minter.get()),
                "Mint: minters or owners can only mint new tokens");
        super._mint(account, amount);
    }

    @External
    public void setMinter(Address minter) {
        // set minter
        Context.require(Context.getCaller().equals(Context.getOwner()), "Minter: only owners can set minters");
        this.minter.set(minter);

    }

    @External(readonly = true)
    public Address getMinter(){
        return minter.get();
    }

    // TO BE REVIEWED
//    @External(readonly = true)
//    public Boolean isMinter(Address caller){
//        Context.require(getMinter().equals(caller),"Mint:Address is not a minter");
//        return true;
//    }


}
