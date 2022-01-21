//package com.iconloop.score.example;
//
//import score.*;
//import score.annotation.EventLog;
//import score.annotation.External;
//
//import java.math.BigInteger;
//
//public class IRC2Snapshot extends IRC2Basic {
//
//    private final Address zeroAddress = new Address(new byte[Address.LENGTH]);
//    private final VarDB<Address> minter = Context.newVarDB("minter", Address.class);
//
//    private final DictDB<Address, Snapshots> accountBalanceSnapshots = Context.newDictDB("accountBalanceSnapshots",Snapshots.class);
//    private Snapshots totalSupplySnapshots;
//    private final Counter currentSnapshotId;
//
//
//    public IRC2Snapshot(String name, String symbol, int decimals, BigInteger initialSupply) {
//        super(name, symbol, decimals);
//        this.currentSnapshotId = new Counter();
//        currentSnapshotId.value.set(BigInteger.ZERO);
//
//        Snapshots object = new Snapshots();
//        object.ids.add(BigInteger.ONE);
//        object.values.add(BigInteger.ONE);
//        this.accountBalanceSnapshots.set(zeroAddress,object);
//
//        // mint initial token supply
//        Context.require(initialSupply.compareTo(BigInteger.ZERO) >= 0);
//
//        _mint(Context.getCaller(), initialSupply.multiply(pow10(decimals)));
//    }
//
//    private static BigInteger pow10(int exponent) {
//        BigInteger result = BigInteger.ONE;
//        for (int i =0; i < exponent; i++) {
//            result = result.multiply(BigInteger.TEN);
//        }
//        return result;
//    }
//
//    private BigInteger snapshot(){
//        if (currentSnapshotId.value.get() == null){
//            currentSnapshotId.value.set(BigInteger.ZERO);
//        }
//        currentSnapshotId.increment();
//        BigInteger currentId = getCurrentSnapshotId();
//        Snapshot(currentId); // event log
//        return currentId;
//    }
//
//    @External(readonly = true)
//    public BigInteger getCurrentSnapshotId(){
//        System.out.println(currentSnapshotId.current());
//        return currentSnapshotId.current();
//    }
//
//    // this is just to test
//    @External
//    // calling snapshot() internal function
//    public void callSnapshot(){
//        snapshot();
//
//    }
//
//    private Pair<Boolean,BigInteger> valueAt(BigInteger snapshotId, Snapshots snapshots){
//        Context.require(snapshotId.compareTo(BigInteger.ZERO)>=0,"IRC20Snapshot: id is 0");
//        Context.require(snapshotId.compareTo(getCurrentSnapshotId())<=0,"IRC20Snapshot: nonexistent id");
//
//        BigInteger index = ArrayUtils.upperBound(snapshots.ids,snapshotId);
//
//        if (index.equals(BigInteger.valueOf(snapshots.ids.size()))){
//            return new Pair<>(false,BigInteger.ZERO);
//        }
//        else {
//            return new Pair<>(true,snapshots.values.get(index.intValue()));
//        }
//    }
//
//    @External(readonly = true)
//    public BigInteger balanceOfAt(Address account,BigInteger snapshotId){
//
//        Pair<Boolean,BigInteger> valueAt = valueAt(snapshotId,accountBalanceSnapshots.get(account));
//        Context.require(valueAt.getFirst());
//        System.out.println("we haer hehe");
//
//        return balanceOf(account);
//    }
//
//    @External(readonly = true)
//    public BigInteger totalSupplyAt(BigInteger snapshotId){
//        Pair<Boolean,BigInteger> valueAt = valueAt(snapshotId,totalSupplySnapshots);
//        Context.require(valueAt.getFirst());
//        return totalSupply();
//    }
//
//    private void updateAccountSnapshots(Address account){
//        updateSnapshots(accountBalanceSnapshots.get(account),balanceOf(account));
//    }
//
//
//    private void updateTotalSupplySnapshots(){
//        updateSnapshots(totalSupplySnapshots,totalSupply());
//    }
//
//    private void updateSnapshots(Snapshots snapshots, BigInteger currentValue){
//        BigInteger currentId = getCurrentSnapshotId();
//        Context.require(lastSnapshotId(snapshots.ids).compareTo(currentId)<0);
//
//        snapshots.ids.add(currentId);
//        snapshots.values.add(currentValue);
//    }
//
//    private BigInteger lastSnapshotId(ArrayDB<BigInteger> ids){
//        if (ids.size() == 0){
//            return BigInteger.ZERO;
//        }
//        else {
//            return ids.get(ids.size()-1);
//        }
//    }
//
//    // added for test purpose only
//    // mint new tokens
//    @External
//    public void mint(BigInteger amount) {
//        Context.require(Context.getCaller().equals(minter.get()));
//        _mint(Context.getCaller(), amount);
//    }
//
//    // for test purpose only
//    // mint tokens to
//    @External
//    public void mintTo(Address account, BigInteger amount) {
//        Context.require(Context.getCaller().equals(minter.get()));
//        _mint(account, amount);
//    }
//
//    // for test purpose only
//    // set minter
//    @External
//    public void setMinter(Address minter) {
//        Context.require(Context.getCaller().equals(Context.getOwner()));
//        this.minter.set(minter);
//    }
//
//    //for test only
//    // destroy token
//    @External
//    public void burn(BigInteger amount) {
//        _burn(Context.getCaller(), amount);
//    }
//
//    @EventLog(indexed = 1)
//    public void Snapshot(BigInteger id){}
//}
