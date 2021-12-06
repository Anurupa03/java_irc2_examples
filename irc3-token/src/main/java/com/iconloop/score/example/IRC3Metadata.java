package com.iconloop.score.example;

import score.Context;
import score.DictDB;
import score.annotation.EventLog;
import score.annotation.External;

import java.math.BigInteger;

public class IRC3Metadata {

    private final String name;
    private final String symbol;

    public IRC3Metadata(String _name, String _symbol){
        this.name = _name;
        this.symbol = _symbol;
    }

    private final DictDB<BigInteger, String> tokenURIs = Context.newDictDB("token_uri", String.class);

    @External(readonly=true)
    public String tokenURI(BigInteger _id) {
        return tokenURIs.get(_id);
    }

    @EventLog(indexed=1)
    public void URI(BigInteger _id, String _value) {}

    protected void _setTokenURI(BigInteger _id, String _uri) {
        Context.require(_uri.length() > 0, "Uri should be set");
        tokenURIs.set(_id, _uri);
        this.URI(_id, _uri);
    }
}
