package com.iconloop.score.example;

import com.iconloop.score.token.irc2.IRC2Basic;
import score.*;


import java.math.BigInteger;


public class snapshot extends IRC2Basic {

    private BranchDB<BigInteger, ArrayDB<BigInteger>> snapshotIds = Context.newBranchDB("snapshotId",BigInteger.class);
    private BranchDB<BigInteger, ArrayDB<BigInteger>> snapshotValues = Context.newBranchDB("snapshotValues", BigInteger.class);

    private DictDB<BigInteger,snapshot> x = Context.newDictDB("x",snapshot.class);
    // importing Counters class and Arrays class
    public VarDB<Counters> _currentSnapshotId = Context.newVarDB("currentSnapshotId", Counters.class);
    public VarDB<Arrays> id = Context.newVarDB("id", Arrays.class);


    // private ArrayDB<BigInteger> currentSnapshotId = Context.newArrayDB("currentSnapshotId",BigInteger.class);
    // totalSupplySnapshots should have snapshotId and snapshotValues
    // currentSnapshotId should have values what does this values mean
    public snapshot(String _name, String _symbol, int _decimals) {
        super(_name, _symbol, _decimals);
    }

    public BigInteger _snapshot(){
        Counters currentSnapshotId = this._currentSnapshotId.get();
        currentSnapshotId.increment();


        BigInteger currentId = _getCurrentSnapshotId();
        // emit Snapshot evenlog
        return currentId;
    }

    public BigInteger _getCurrentSnapshotId(){
        Counters currentSnapshotId = this._currentSnapshotId.get();
        return currentSnapshotId.current();
    }

    public BigInteger balanceOfAt(Address account, BigInteger snapshotId){
        // the bool condition
        return balanceOf(account);
    }

    public BigInteger totalSupplyAt(BigInteger snapshotId){
        // the bool condition
        return totalSupply();
    }

    // value at should return boolean and Biginteger
    public DictDB _valueAt(BigInteger snapshotId, DictDB x){

        Context.require(snapshotId.compareTo(BigInteger.ZERO)>0,"IRC20Snapshot: id is 0");
        Context.require(snapshotId.compareTo(_getCurrentSnapshotId())<= 0,"IRC20Snapshot: nonexistent id");


        x.set(snapshotIds,snapshotValues);



        Arrays ids = this.id.get();
        BigInteger index = ids.upperBound();

        if (index ==)
//        BigInteger index = ids.upperBound(snapshotId,snapshotValues);






    }

}
