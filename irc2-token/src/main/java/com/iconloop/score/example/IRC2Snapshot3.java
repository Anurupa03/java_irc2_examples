package com.iconloop.score.example;

import com.iconloop.score.token.irc2.IRC2Basic;
import score.*;
import score.annotation.EventLog;
import score.annotation.External;

import java.math.BigInteger;

public class IRC2Snapshot3 extends IRC2Basic {

    private final VarDB<Address> minter = Context.newVarDB("minter", Address.class);

    private DictDB<Address,Snapshots> accountBalanceSnapshots = Context.newDictDB("accountBalanceSnapshots",Snapshots.class);
    private Snapshots totalSupplySnapshots;
    private Counters currentSnapshotId;


    public IRC2Snapshot3(String _name, String _symbol, int _decimals, BigInteger _initialSupply) {
        super(_name, _symbol, _decimals);
        this.currentSnapshotId = new Counters();

        // mint initial token supply
        Context.require(_initialSupply.compareTo(BigInteger.ZERO) >= 0);
        _mint(Context.getCaller(), _initialSupply.multiply(pow10(_decimals)));
    }

    private static BigInteger pow10(int exponent) {
        BigInteger result = BigInteger.ONE;
        for (int i =0; i < exponent; i++) {
            result = result.multiply(BigInteger.TEN);
        }
        return result;
    }

    private BigInteger snapshot(){
        currentSnapshotId.value.set(BigInteger.ZERO);
        currentSnapshotId.increment();
        BigInteger currentId = getCurrentSnapshotId2();
        Snapshot(currentId); // event log

        return currentId;

    }

    @External(readonly = true)
    public BigInteger getCurrentSnapshotId2(){
        return currentSnapshotId.current();
    }

    @External
    // calling snapshot() internal function
    public void callSnapshot(){
        snapshot();

    }

    private Pair<Boolean,BigInteger> valueAt(BigInteger snapshotId, Snapshots snapshots){
        Context.require(snapshotId.compareTo(BigInteger.ZERO)>=0,"IRC20Snapshot: id is 0");
        Context.require(snapshotId.compareTo(getCurrentSnapshotId2())<=0,"IRC20Snapshot: nonexistent id");

        BigInteger index = ArraysUtil.upperBound(snapshots.ids,snapshotId);

        if (index.equals(BigInteger.valueOf(snapshots.ids.size()))){
            return new Pair<>(false,BigInteger.ZERO);
        }
        else {
            return new Pair<>(true,snapshots.values.get(index.intValue()));
        }
    }

    @External(readonly = true)
    public BigInteger balanceOfAt(Address account,BigInteger snapshotId){
        Pair<Boolean,BigInteger> valueAt = valueAt(snapshotId,accountBalanceSnapshots.get(account));
        Context.require(valueAt.getFirst());

        return balanceOf(account);
    }

    @External(readonly = true)
    public BigInteger totalSupplyAt(BigInteger snapshotId){
        Pair<Boolean,BigInteger> valueAt = valueAt(snapshotId,totalSupplySnapshots);
        Context.require(valueAt.getFirst());
        return totalSupply();
    }

    private void updateAccountSnapshots(Address account){
        updateSnapshots(accountBalanceSnapshots.get(account),balanceOf(account));
    }


    private void updateTotalSupplySnapshots(){
        updateSnapshots(totalSupplySnapshots,totalSupply());
    }

    private void updateSnapshots(Snapshots snapshots, BigInteger currentValue){
        BigInteger currentId = getCurrentSnapshotId2();
        Context.require(lastSnapshotId(snapshots.ids).compareTo(currentId)<0);

        snapshots.ids.add(currentId);
        snapshots.values.add(currentValue);
    }

    private BigInteger lastSnapshotId(ArrayDB<BigInteger> ids){
        if (ids.size() == 0){
            return BigInteger.ZERO;
        }
        else {
            return ids.get(ids.size()-1);
        }
    }

    // mint
    @External
    public void mint(BigInteger _amount) {
        // simple access control - only the minter can mint new token
        Context.require(Context.getCaller().equals(minter.get()));
        _mint(Context.getCaller(), _amount);
    }
    @External
    public void mintTo(Address _account, BigInteger _amount) {
        // simple access control - only the minter can mint new token
        Context.require(Context.getCaller().equals(minter.get()));
        _mint(_account, _amount);
    }

    @External
    public void setMinter(Address _minter) {
        // simple access control - only the contract owner can set new minter
        Context.require(Context.getCaller().equals(Context.getOwner()));
        minter.set(_minter);
    }

    @External
    public void burn(BigInteger _amount) {
        _burn(Context.getCaller(), _amount);
    }

    @EventLog(indexed = 1)
    public void Snapshot(BigInteger id){}
}
