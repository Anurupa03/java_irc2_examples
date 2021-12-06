package com.iconloop.score.example;

import score.Address;
import score.Context;
import score.annotation.External;

import java.math.BigInteger;

public class IRC3Token extends IRC3{

    public IRC3Token(String _name, String _symbol) {
        super(_name, _symbol);
    }

    @External
    public void mint(BigInteger _tokenId) {
        Context.require(Context.getCaller().equals(Context.getOwner()));
        super._mint(Context.getCaller(), _tokenId);
    }

    @External
    public void burn(BigInteger _tokenId) {
        Address owner = ownerOf(_tokenId);
        Context.require(Context.getCaller().equals(owner));
        super._burn(_tokenId);
    }
}
