package com.iconloop.score.example;

import com.iconloop.score.token.irc2.IRC2Basic;
import score.ArrayDB;
import score.Context;
import score.BranchDB;
import score.Address;
import score.annotation.EventLog;


import java.math.BigInteger;

public class IRC2Snapshot2 extends IRC2Basic {

    private final BranchDB<BigInteger, ArrayDB<BigInteger>> id = Context.newBranchDB("id",BigInteger.class);
    private final BranchDB<BigInteger,ArrayDB<BigInteger>> values = Context.newBranchDB("values",BigInteger.class);
    private final Counters currentSnapshotId;

    public IRC2Snapshot2(String _name, String _symbol, int _decimals) {
        super(_name, _symbol, _decimals);

        this.currentSnapshotId = new Counters();
    }

    private BigInteger snapshot(){
        currentSnapshotId.increment();
        BigInteger currentId = getCurrentSnapshotId();
        Snapshot(currentId);
        return currentId;
    }

    public BigInteger getCurrentSnapshotId(){
        return currentSnapshotId.current();
    }

    private Pair<Boolean,BigInteger> valueAt(BigInteger snapshotId, BranchDB<BigInteger,ArrayDB<BigInteger>>sid, BranchDB<BigInteger,ArrayDB<BigInteger>>sval){
        Context.require(snapshotId.compareTo(BigInteger.ZERO)>=0,"IRC20Snapshot: id is 0");
        Context.require(snapshotId.compareTo(getCurrentSnapshotId())<=0,"IRC20Snapshot: nonexistent id");

        BigInteger index = ArraysUtil.upperBound(sid.at(snapshotId),snapshotId);

        if (index.equals(BigInteger.valueOf(sid.at(snapshotId).size()))){
            return new Pair<>(false,BigInteger.ZERO);
        }
        else {
            return new Pair<>(true,sval.at(snapshotId).get(index.intValue()));
        }
    }

    public BigInteger balanceOfAt(Address account, BigInteger snapshotId){
        Pair<Boolean,BigInteger> valueAt = valueAt(snapshotId,id,values);
        Context.require(valueAt.getFirst());

        return balanceOf(account);
    }

    public BigInteger totalSupplyAt(BigInteger snapshotId){
        Pair<Boolean,BigInteger> valueAt = valueAt(snapshotId,id,values);

        Context.require(valueAt.getFirst());

        return totalSupply();
    }

    private void updateBalance(Address account){
        update(id,values,balanceOf(account));
    }

    private void updateTotalSupply(){
        update(id,values,totalSupply() );
    }
    private void update(BranchDB<BigInteger,ArrayDB<BigInteger>> id, BranchDB<BigInteger,ArrayDB<BigInteger>> value, BigInteger currentValue){
        BigInteger currentId = getCurrentSnapshotId();
        Context.require(lastSnapshotId(id.at(currentValue)).compareTo(currentId)<0);

        id.at(currentValue).add(currentId);
        value.at(currentId).add(currentValue);

    }

    private BigInteger lastSnapshotId(ArrayDB<BigInteger> ids){
        if (ids.size() == 0){
            return BigInteger.ZERO;
        }
        else {
            return ids.get(ids.size()-1);
        }
    }

    @EventLog(indexed = 1)
    public void Snapshot(BigInteger id){}
}
