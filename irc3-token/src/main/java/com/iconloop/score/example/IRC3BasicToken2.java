package com.iconloop.score.example;

import score.Address;
import score.Context;
import score.annotation.External;

import java.math.BigInteger;

public class IRC3BasicToken extends IRC3Basic {
    public IRC3BasicToken(String _name, String _symbol) {
        super(_name, _symbol);
    }

    @External
    public void mint(BigInteger _tokenId) {
        // simple access control - only the contract owner can mint new token
        Context.require(Context.getCaller().equals(Context.getOwner()));
        super._mint(Context.getCaller(), _tokenId);
    }

    @External
    public void burn(BigInteger _tokenId) {
        // simple access control - only the owner of token can burn it
        Address owner = ownerOf(_tokenId);
        Context.require(Context.getCaller().equals(owner));
        super._burn(_tokenId);
    }
}
