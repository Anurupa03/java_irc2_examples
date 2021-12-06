package com.iconloop.score.example;

import com.iconloop.score.util.EnumerableIntMap;

import score.Address;
import score.BranchDB;
import score.Context;
import score.DictDB;
import score.annotation.EventLog;
import score.annotation.External;

import java.math.BigInteger;

public class IRC3 {
    protected static final Address ZERO_ADDRESS = new Address(new byte[Address.LENGTH]);
    private final String name;
    private final String symbol;
    private final DictDB<BigInteger, Address> owners = Context.newDictDB("owners",Address.class);
    private final EnumerableIntMap<Address> tokenOwners = new EnumerableIntMap<>("owners", Address.class);
    private final DictDB<BigInteger, Address> tokenApprovals = Context.newDictDB("approvals", Address.class);
    private final DictDB<Address,BigInteger> balances = Context.newDictDB("balances",BigInteger.class);
    private final BranchDB<Address, DictDB<Address, Boolean>> operatorApproval = Context.newBranchDB("approval", Boolean.class);

    public IRC3(String _name, String _symbol){
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
    public BigInteger balanceOf(Address _owner) {
        Context.require(!ZERO_ADDRESS.equals(_owner));
        return Context.getBalance(_owner);
    }

    @External(readonly=true)
    public Address ownerOf(BigInteger _tokenId) {
        return tokenOwners.getOrThrow(_tokenId, "Non-existent token");
    }

    @External(readonly=true)
    public Address getApproved(BigInteger _tokenId) {
        return tokenApprovals.getOrDefault(_tokenId, ZERO_ADDRESS);
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

        tokenOwners.set(tokenId, to);
        Transfer(ZERO_ADDRESS, to, tokenId);
    }

    protected void _burn(BigInteger tokenId) {
        Address owner = ownerOf(tokenId);
        // clear approvals
        _approve(ZERO_ADDRESS, tokenId);

        tokenOwners.remove(tokenId);
        Transfer(owner, ZERO_ADDRESS, tokenId);
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
