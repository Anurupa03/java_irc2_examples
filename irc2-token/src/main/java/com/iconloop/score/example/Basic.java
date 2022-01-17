package com.iconloop.score.example;

import com.iconloop.score.token.irc2.IRC2;
import score.Address;
import score.Context;
import score.DictDB;
import score.VarDB;
import score.annotation.EventLog;
import score.annotation.External;
import score.annotation.Optional;

import java.math.BigInteger;

public abstract class Basic implements IRC2 {
    protected static final Address ZERO_ADDRESS = new Address(new byte[Address.LENGTH]);
    private final String name;
    private final String symbol;
    private final int decimals;
    private final VarDB<BigInteger> totalSupply = Context.newVarDB("total_supply", BigInteger.class);
    private final DictDB<Address, BigInteger> balances = Context.newDictDB("balances", BigInteger.class);

    public Basic(String name, String symbol, int decimals) {
        this.name = name;
        this.symbol = symbol;
        this.decimals = decimals;

        // decimals must be larger than 0 and less than 21
        Context.require(decimals >= 0, "decimals needs to be positive");
        Context.require(decimals <= 21, "decimals needs to be equal or lower than 21");
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
    public BigInteger decimals() {
        return BigInteger.valueOf(decimals);
    }

    @External(readonly=true)
    public BigInteger totalSupply() {
        return totalSupply.getOrDefault(BigInteger.ZERO);
    }

    @External(readonly=true)
    public BigInteger balanceOf(Address owner) {
        return safeGetBalance(owner);
    }

    @External
    public void transfer(Address to, BigInteger value, @Optional byte[] data) {
        Address from = Context.getCaller();

        // check some basic requirements
        Context.require(value.compareTo(BigInteger.ZERO) >= 0, "Transfer: Value needs to be positive");
        Context.require(safeGetBalance(from).compareTo(value) >= 0, "Transfer: Insufficient balance");

        // adjust the balances
        safeSetBalance(from, safeGetBalance(from).subtract(value));
        safeSetBalance(to, safeGetBalance(to).add(value));

        // if the recipient is SCORE, call 'tokenFallback' to handle further operation
        byte[] dataBytes = (data == null) ? new byte[0] : data;
        if (to.isContract()) {
            Context.call(to, "tokenFallback", from, value, dataBytes);
        }

        // emit Transfer event
        Transfer(from, to, value, dataBytes);
    }

    /**
     * Creates `amount` tokens and assigns them to `owner`, increasing the total supply.
     */
    protected void _mint(Address owner, BigInteger amount) {
        Context.require(!ZERO_ADDRESS.equals(owner), "Mint: Owner address cannot be zero address");
        Context.require(amount.compareTo(BigInteger.ZERO) >= 0, "Mint: Amount needs to be positive");

        totalSupply.set(totalSupply.getOrDefault(BigInteger.ZERO).add(amount));
        safeSetBalance(owner, safeGetBalance(owner).add(amount));
        Transfer(ZERO_ADDRESS, owner, amount, "mint".getBytes());
    }

    /**
     * Destroys `amount` tokens from `owner`, reducing the total supply.
     */
    protected void _burn(Address owner, BigInteger amount) {
        Context.require(!ZERO_ADDRESS.equals(owner), "Burn: Owner address cannot be zero address");
        Context.require(amount.compareTo(BigInteger.ZERO) >= 0, "Burn: amount needs to be positive");
        Context.require(safeGetBalance(owner).compareTo(amount) >= 0, "Burn: Insufficient balance");

        safeSetBalance(owner, safeGetBalance(owner).subtract(amount));
        totalSupply.set(totalSupply.getOrDefault(BigInteger.ZERO).subtract(amount));
        Transfer(owner, ZERO_ADDRESS, amount, "burn".getBytes());
    }

    private BigInteger safeGetBalance(Address owner) {
        return balances.getOrDefault(owner, BigInteger.ZERO);
    }

    private void safeSetBalance(Address owner, BigInteger amount) {
        balances.set(owner, amount);
    }

    @EventLog(indexed=3)
    public void Transfer(Address from, Address to, BigInteger value, byte[] data) {}

}
