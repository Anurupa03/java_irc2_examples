package com.iconloop.score.example;

import com.iconloop.score.token.irc2.IRC2Basic;
import score.Address;
import score.Context;
import score.annotation.EventLog;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class IRC2Snapshot extends IRC2Basic {

    private Map<Address,Snapshots> _accountBalanceSnapshots;
    private Snapshots _totalSupplySnapshots;
    private Counters __currentSnapshotId;

    public IRC2Snapshot(String _name, String _symbol, int _decimals) {
        super(_name, _symbol, _decimals);

        this._accountBalanceSnapshots = new HashMap<Address,Snapshots>();
        this._totalSupplySnapshots = new Snapshots();
        this.__currentSnapshotId = new Counters();
    }

    public BigInteger _snapshot(){
        __currentSnapshotId.increment();
        BigInteger currentId = _getCurrentSnapshotId();
        Snapshot(currentId);
        return currentId;
    }

    public BigInteger _getCurrentSnapshotId(){
        return __currentSnapshotId.current();
    }

    public BigInteger balanceOfAt(Address account, BigInteger snapshotId){
        Pair<Boolean,BigInteger> valueAt = _valueAt(snapshotId,_accountBalanceSnapshots.get(account));
        Context.require(valueAt.getFirst());

        return balanceOf(account);
    }

    public BigInteger totalSupplyAt(BigInteger snapshotId){
        Pair<Boolean,BigInteger> valueAt = _valueAt(snapshotId,_totalSupplySnapshots);
        Context.require(valueAt.getFirst());
        return totalSupply();
    }

    public Pair<Boolean,BigInteger> _valueAt(BigInteger snapshotId, Snapshots snapshots){
        Context.require(snapshotId.compareTo(BigInteger.ZERO)>=0,"IRC20Snapshot: id is 0");
        Context.require(snapshotId.compareTo(_getCurrentSnapshotId())<=0,"IRC20Snapshot: nonexistent id");

        BigInteger index = ArraysUtil.upperBound(snapshots.ids,snapshotId);


        if (index.equals(BigInteger.valueOf(snapshots.ids.length))){
            return new Pair<>(false,BigInteger.ZERO);
        }

        else {
            return new Pair<>(true,snapshots.values[index.intValue()]);
        }
    }

    private void __updateAccountSnapshot(Address account){
        _updateSnapshots(_accountBalanceSnapshots.get(account),balanceOf(account));
    }

    private void _updateTotalSupplySnapshot(){
        _updateSnapshots(_totalSupplySnapshots,totalSupply());
    }

    private void _updateSnapshots(Snapshots snapshots, BigInteger currentValue){
        BigInteger currentId = _getCurrentSnapshotId();
        Context.require(_lastSnapshotId(snapshots.ids).compareTo(currentId)<0);

        ArrayList<BigInteger> idsList = new ArrayList<>(Arrays.asList(snapshots.ids));
        idsList.add(currentId);

        ArrayList<BigInteger> valuesList = new ArrayList<>(Arrays.asList(snapshots.values));
        valuesList.add(currentValue);

    }

    private BigInteger _lastSnapshotId(BigInteger[] ids){
        if(ids.length == 0){
            return BigInteger.ZERO;
        }
        else {
            return ids[ids.length-1];
        }
    }

    @EventLog(indexed = 1)
    public void Snapshot(BigInteger id){}
}