package com.iconloop.score.example;

import com.iconloop.score.util.EnumerableSet;
import score.Address;
import score.BranchDB;
import score.Context;
import score.DictDB;
import score.annotation.EventLog;
import score.annotation.External;

import java.math.BigInteger;

public class IRC3Partial2 extends IRC3{

    protected static final Address ZERO_ADDRESS = new Address(new byte[Address.LENGTH]);
    public IRC3Partial2(String _name, String _symbol) {
        super(_name, _symbol);
    }

    private final DictDB<BigInteger,AssetLibrary2> assetFunction = Context.newDictDB("assetsFunction",AssetLibrary2.class);
    private final DictDB<BigInteger, AssetLibrary2.Asset> assets = Context.newDictDB("assets", AssetLibrary2.Asset.class);
    private final BranchDB<Address, DictDB<BigInteger, ApproveUnits>> tokenApprovalUnits = Context.newBranchDB("tokenApprovalUnits",ApproveUnits.class);
    private final BranchDB<BigInteger, EnumerableSet<BigInteger>> slotTokens = Context.newBranchDB("slotTokens",EnumerableSet.class);


    private String contractURI;

    // Eventlogs
    @EventLog(indexed = 4)
    public void Merge(Address owner, BigInteger tokenId, BigInteger targteTokenId, BigInteger units){}

    @EventLog(indexed = 4)
    public void Split(Address owner,BigInteger tokenId, BigInteger newTokenId, BigInteger units){}

    @EventLog(indexed = 4)
    public void Mint(Address minter, BigInteger tokenId, BigInteger slot, BigInteger units) {}

    @EventLog(indexed = 3)
    public void Burn(Address owner, BigInteger tokenId, BigInteger units){}

    @EventLog(indexed = 4)
    public void ApprovalUnits(Address owner, Address to, BigInteger tokenId, BigInteger units){}

    @EventLog(indexed = 5)
    public void PartialTransfer(Address from, Address to, BigInteger tokenId, BigInteger targetTokenId, BigInteger units){}


    private void setContractURI (String uri){
        this.contractURI = uri;
    }

    @External(readonly = true)
    public String getContractURI(String memory){
        return  contractURI;
    }

    private void transferUnitsFrom(Address from, Address to, BigInteger tokenId, BigInteger targetTokenId,
                                   BigInteger transferUnits){
        Context.require(from.equals(ownerOf(tokenId)),"source token owner mismatch");

        if (Context.getCaller() != from && ! super.isApprovedForAll(from, Context.getCaller())){
            BigInteger newUnits = balanceOf(Context.getCaller()).subtract(transferUnits);
            tokenApprovalUnits.at(from).get(tokenId).approvals.set(Context.getCaller(),
                    newUnits);
        }

        Context.require(!ZERO_ADDRESS.equals(to),"transfer to the zero address");


        if (!super._tokenExists(tokenId)){
            super._mint(to,targetTokenId);
        }
        else {
            Context.require(ownerOf(targetTokenId).equals(to),"target token owner mismatch");
        }
        AssetLibrary self = new AssetLibrary();
        // pass asset targettokenId as assetLibrary here
        assetFunction.get(tokenId).transfer(self,,transferUnits);
//        AssetLibrary2.Asset aaa = ass.get(tokenId).new Asset();


        PartialTransfer(from,to,tokenId,targetTokenId,transferUnits);
    }

    private boolean isApprovedOrOwner(Address owner, BigInteger tokenId) {
        // modify this
        if (Context.getCaller().equals(owner) && ownerOf(tokenId).equals(Context.getCaller()) ){
            return true;
        }
        else{
            return false;
        }
    }

    private void merge(BigInteger tokenId, BigInteger targetTokenId){
        Context.require(isApprovedOrOwner(Context.getCaller(),tokenId),"not owner nor approved");
        Context.require(super._tokenExists(targetTokenId),"target token not exists");
        Context.require(!tokenId.equals(targetTokenId), "self merge not allowed");

        Address owner = ownerOf(tokenId);
        Context.require(owner.equals(ownerOf(targetTokenId)),"not same owner");

        AssetLibrary aa = new AssetLibrary();
        BigInteger mergeUnits = assetFunction.get(tokenId).merge(aa, assetFunction.get(targetTokenId)); // need assetLibrary here not asset function

        burn(tokenId);
        Merge(owner,tokenId,targetTokenId,mergeUnits);
    }

    private void splitUnits(BigInteger tokenId, BigInteger newTokenId, BigInteger splitUnits){
        Context.require(isApprovedOrOwner(Context.getCaller(),tokenId),"not owner nor approved");
        Context.require(!super._tokenExists(newTokenId),"new Token already exits");

        // sub units how to access this from assets
        assets.get(tokenId).unit = assets.get(tokenId).unit.subtract(splitUnits);

        Address owner = ownerOf(tokenId);
        // slot from assets
        mintUnits(owner,newTokenId, assetFunction.get(tokenId).,splitUnits);

        Split(owner,tokenId,newTokenId,splitUnits);

    }

    private void mintUnits(Address minter, BigInteger tokenId, BigInteger slot, BigInteger units){
        if ((super._tokenExists(tokenId))){
            super._mint(minter, tokenId);

        }
        AssetLibrary2 bb = new AssetLibrary2();
        AssetLibrary2.Asset aa = bb.new Asset();
        assetFunction.get(tokenId).mint(aa,slot,units);

        if (! slotTokens.at(slot).contains(tokenId)){
            slotTokens.at(slot).add(tokenId);
        }

        Mint(minter,tokenId,slot,units);


    }

    private void burn(BigInteger tokenId){
        BigInteger units = assets.get(tokenId).unit; // get units here
        Address owner = ownerOf(tokenId);
        BigInteger slot = assets.get(tokenId).slot;// get slot here
        if (slotTokens.at(slot).contains(tokenId)){
            slotTokens.at(slot).remove(tokenId);
        }

        assetFunction.set(tokenId,null);
        tokenApprovalUnits.at(owner).set(tokenId,null);
        super._burn(tokenId);

        Burn(owner,tokenId,units);

    }


    private BigInteger burnUnits(BigInteger tokenId, BigInteger burnUnits){
        Address owner = ownerOf(tokenId);
        assetFunction.get(tokenId).burn(burnUnits);

        Burn(owner,tokenId,burnUnits);
        return assets.get(tokenId).unit;
//        return assets.get(tokenId).; // how to acess units from here
    }

    @External(readonly = true)
    public void approve(Address to, BigInteger tokenId, BigInteger units){
        approveUnits(Context.getCaller(),to,tokenId,units);
    }

    private void approveUnits(Address owner, Address to, BigInteger tokenId, BigInteger units){
        Context.require(ownerOf(tokenId).equals(owner),"only owner");
        tokenApprovalUnits.at(owner).get(tokenId).isValid.set(true);
        tokenApprovalUnits.at(owner).get(tokenId).approvals.set(to,units);

        ApprovalUnits(owner,to,tokenId,units);
    }

    @External(readonly = true)
    public BigInteger unitsInToken(BigInteger tokenId){
        return assetFunction.get(tokenId).;
    }

    @External(readonly = true)
    public int balanceOfSlot(BigInteger slot){
        return slotTokens.at(slot).length();
    }

    @External(readonly = true)
    public BigInteger tokenOfSlotByIndex(BigInteger slot, BigInteger index){
        return slotTokens.at(slot).at(index.intValue());
    }

    @External(readonly = true)
    public BigInteger slotOf(BigInteger tokenId){
        return assets.get(tokenId).slot;
    }
}

// line 76 how to pass targettokenId in transfer method
// line 76 is it okay to pass self like thta?
// line 114 how to subtract the units....yesari huncha ki nai
// line 118 pass slots in mintUnits method
// line 161 pass self variable?
// line 164 is it okay to  declare two variable from the same class
// the concept of inner and outer class
