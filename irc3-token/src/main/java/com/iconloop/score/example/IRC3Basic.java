package com.iconloop.score.example;

import com.iconloop.score.util.EnumerableIntMap;

import com.iconloop.score.util.IntSet;
import score.Address;
import score.BranchDB;
import score.Context;
import score.DictDB;
import score.annotation.EventLog;
import score.annotation.External;

import java.math.BigInteger;

public abstract class IRC3Basic implements IRC3 {
    protected static final Address ZERO_ADDRESS = new Address(new byte[Address.LENGTH]);
    private final String name;
    private final String symbol;

    private final DictDB<Address, IntSet> holderTokens = Context.newDictDB("holders", IntSet.class);
    private final EnumerableIntMap<Address> tokenOwners = new EnumerableIntMap<>("owners", Address.class);
    private final DictDB<BigInteger, Address> tokenApprovals = Context.newDictDB("approvals", Address.class);
    private final BranchDB<Address, DictDB<Address, Boolean>> operatorApproval = Context.newBranchDB("approval", Boolean.class);

    public IRC3Basic(String _name, String _symbol){
        this.name = _name;
        this.symbol =_symbol;
    }

    @External(readonly=true)
    public String name() {
        return name;
    }

    @External(readonly=true)
    public String symbol() {
        return symbol;
    }

    @External(readonly=true)
    public int balanceOf(Address _owner) {
        Context.require(!ZERO_ADDRESS.equals(_owner));
        var tokens = holderTokens.get(_owner);
        return (tokens != null) ? tokens.length() : 0;
    }

    @External(readonly=true)
    public Address ownerOf(BigInteger tokenId) {
        return tokenOwners.getOrThrow(tokenId, "Non-existent token");
    }

    @External(readonly=true)
    public Address getApproved(BigInteger tokenId) {
        return tokenApprovals.getOrDefault(tokenId, ZERO_ADDRESS);
    }

    @External
    public void approve(Address _to, BigInteger _tokenId) {
        Address owner = ownerOf(_tokenId);
        Context.require(!owner.equals(_to));
        Context.require(owner.equals(Context.getCaller()));
        _approve(_to, _tokenId);
    }

    private void _approve(Address to, BigInteger tokenId) {
        tokenApprovals.set(tokenId, to);
        Approval(ownerOf(tokenId), to, tokenId);
    }

    @External
    public void transfer(Address _to, BigInteger _tokenId) {
        Address owner = ownerOf(_tokenId);
        Context.require(owner.equals(Context.getCaller()));
        _transfer(owner, _to, _tokenId);
    }

    @External
    public void transferFrom(Address _from, Address _to, BigInteger _tokenId) {
        Address owner = ownerOf(_tokenId);
        Address spender = Context.getCaller();
        Context.require(owner.equals(spender) || getApproved(_tokenId).equals(spender));
        _transfer(_from, _to, _tokenId);
    }

    private void _transfer(Address from, Address to, BigInteger tokenId) {
        Context.require(ownerOf(tokenId).equals(from));
        Context.require(!to.equals(ZERO_ADDRESS));
        // clear approvals from the previous owner
        _approve(ZERO_ADDRESS, tokenId);

        _removeTokenFrom(tokenId, from);
        _addTokenTo(tokenId, to);
        tokenOwners.set(tokenId, to);
        Transfer(from, to, tokenId);
    }

    @External(readonly=true)
    public int totalSupply() {
        return tokenOwners.length();
    }

    protected void _mint(Address to, BigInteger tokenId) {
        Context.require(!ZERO_ADDRESS.equals(to));
        Context.require(!_tokenExists(tokenId));

        _addTokenTo(tokenId, to);
        tokenOwners.set(tokenId, to);
        Transfer(ZERO_ADDRESS, to, tokenId);
    }



    protected void _burn(BigInteger tokenId) {
        Address owner = ownerOf(tokenId);
        // clear approvals
        _approve(ZERO_ADDRESS, tokenId);

        _removeTokenFrom(tokenId, owner);
        tokenOwners.remove(tokenId);
        Transfer(owner, ZERO_ADDRESS, tokenId);
    }

    private void _addTokenTo(BigInteger tokenId, Address to) {
        var tokens = holderTokens.get(to);
        if (tokens == null) {
            tokens = new IntSet(to.toString());
            holderTokens.set(to, tokens);
        }
        tokens.add(tokenId);
    }

    private void _removeTokenFrom(BigInteger tokenId, Address from) {
        var tokens = holderTokens.get(from);
        Context.require(tokens != null, "tokens don't exist for this address");
        tokens.remove(tokenId);
        if (tokens.length() == 0) {
            holderTokens.set(from, null);
        }
    }

    @External(readonly = true)
    public boolean _tokenExists(BigInteger tokenId) {
        return tokenOwners.contains(tokenId);
    }

    @External(readonly=true)
    public boolean isApprovedForAll(Address _owner, Address _operator) {
        return operatorApproval.at(_owner).getOrDefault(_operator, false);
    }

    @EventLog(indexed=3)
    public void Transfer(Address _from, Address _to, BigInteger _tokenId) {
    }

    @EventLog(indexed=3)
    public void Approval(Address _owner, Address _approved, BigInteger _tokenId) {
    }
}
