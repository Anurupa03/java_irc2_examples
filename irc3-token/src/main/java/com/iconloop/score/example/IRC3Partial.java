package com.iconloop.score.example;

import com.iconloop.score.util.EnumerableSet;
import score.*;
import score.annotation.External;

import java.math.BigInteger;

public class IRC3Partial extends IRC3{

    protected static final Address ZERO_ADDRESS = new Address(new byte[Address.LENGTH]);
    public IRC3Partial(String _name, String _symbol) {
        super(_name, _symbol);
    }

    private DictDB<BigInteger,AssetFunction> assets = Context.newDictDB("assets",AssetFunction.class);

    private BranchDB<Address, BranchDB<BigInteger, ApproveUnits>> tokenApprovalUnits = Context.newBranchDB("tokenApprovalUnits",ApproveUnits.class);
    private BranchDB<BigInteger, EnumerableSet<BigInteger>> slotTokens = Context.newBranchDB("slotTokens",EnumerableSet.class);


    private String contractURI;

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
            tokenApprovalUnits.at(from).at(tokenId).approvals.set(Context.getCaller(),
                            newUnits);
        }

        Context.require(!ZERO_ADDRESS.equals(to),"transfer to the zero address");

        if (!super._tokenExists(tokenId)){
            super._mint(to,targetTokenId);
        }
        else {
            Context.require(ownerOf(targetTokenId).equals(to),"target token owner mismatch");
        }
        AssetLibrary aa = new AssetLibrary();
        // pass asset target tokenId as assetLibrary here
        assets.get(tokenId).transfer(aa,,transferUnits);

        // emit Partial transfer eventlog here
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
        BigInteger mergeUnits = assets.get(tokenId).merge(aa,assets.get(targetTokenId)); // need assetLibrary here not asset function

        // emit Merge event log here
    }

    private void splitUnits(BigInteger tokenId, BigInteger newTokenId, BigInteger splitUnits){
        Context.require(isApprovedOrOwner(Context.getCaller(),tokenId),"not owner nor approved");
        Context.require(!super._tokenExists(newTokenId),"new Token already exits");

        // sub units how to access this from assets

        Address owner = ownerOf(tokenId);
        mintUnits(owner,newTokenId,assets.get(tokenId).,splitUnits); // slot from assets
    }

    private void mintUnits(Address minter, BigInteger tokenId, BigInteger slot, BigInteger units){
        if ((super._tokenExists(tokenId))){
            super._mint(minter, tokenId);

        }
        AssetLibrary aa = new AssetLibrary();
        assets.get(tokenId).mint(aa,slot,units);

        if (! slotTokens.at(slot).contains(tokenId)){
            slotTokens.at(slot).add(tokenId);
        }

        // throw Mint event log here
    }

    // brun lai override


    private void burnUnits(BigInteger tokenId, BigInteger burnUnits){
        Address owner = ownerOf(tokenId);
        assets.get(tokenId).burn();

        // emit Burn evenlog here
        return assets.get(tokenId).; // how to acess units from here
    }

    @External(readonly = true)
    public void approve(Address to, BigInteger tokenId, BigInteger units){
        approveUnits(Context.getCaller(),to,tokenId,units);
    }

    private void approveUnits(Address owner, Address to, BigInteger tokenId, BigInteger units){
        Context.require(ownerOf(tokenId).equals(owner),"only owner");
        tokenApprovalUnits.at(owner).at(tokenId).isValid.set(true);
        tokenApprovalUnits.at(owner).at(tokenId).approvals.set(to,units);

        // event log for Approval units
    }

    @External(readonly = true)
    public BigInteger unitsInToken(BigInteger tokenId){
        return assets.get(tokenId).;
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
