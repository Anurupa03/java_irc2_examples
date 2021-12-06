package com.iconloop.score.example;

import com.iconloop.score.token.irc3.IRC3Basic;
import com.iconloop.score.util.IntSet;
import score.*;
import score.annotation.External;

import java.math.BigInteger;

public class IRC3Enumerable extends IRC3Basic {

    //    private final EnumerableIntMap<Address> ownedToken = new EnumerableIntMap<>("tokenOwners",Address.class);
//    private final DictDB<BigInteger,BigInteger> ownedTokenIndex = Context.newDictDB("tokenIndex",BigInteger.class);
//    private final BranchDB<BigInteger,ArrayDB<BigInteger>> allTokenIndex2 = Context.newBranchDB("allTokenIndex2",ArrayDB.class);

    protected static final Address ZERO_ADDRESS = new Address(new byte[Address.LENGTH]);

    private final DictDB<Address, IntSet> ownedToken = Context.newDictDB("ownedtoken",IntSet.class);
    private final BranchDB<BigInteger,ArrayDB<BigInteger>> ownedTokenIndex = Context.newBranchDB("ownedTokenIndex",ArrayDB.class);
    private final ArrayDB<BigInteger> allTokens = Context.newArrayDB("allToken", BigInteger.class);
    private final DictDB<BigInteger,BigInteger> allTokenIndex= Context.newDictDB("allTokenIndex", BigInteger.class);

    public IRC3Enumerable(String _name, String _symbol) {
        super(_name, _symbol);
    }

    @External(readonly = true)
    public BigInteger tokenOfOwnerByIndex(Address owner, BigInteger index){
        Context.require(index.compareTo(BigInteger.valueOf(super.balanceOf(owner)))<0,
                "IRC3Enumerable: owner index out of bounds");
        return ownedToken.get(owner).at(index.intValue());
    }

    @External(readonly = true)
    public int totalSupply(){
        return allTokens.size();
    }

    @External(readonly = true)
    public BigInteger tokenByIndex(BigInteger index){
        Context.require(index.compareTo(BigInteger.valueOf(totalSupply()))<0,"IRC3Enumerable: global index out of bounds");
        return allTokens.get(index.intValue());
    }


    private void addTokenToOwnerEnumeration(Address to, BigInteger tokenId){
        BigInteger length = BigInteger.valueOf(super.balanceOf(to));

        var tokens = ownedToken.get(to);
        if (tokens == null){
            tokens = new IntSet(to.toString());
            ownedToken.set(to,tokens);
        }
        tokens.add(tokenId);
        ownedTokenIndex.at(tokenId).add(length);
    }

    private void addTokenToAllTokensEnumeration(BigInteger tokenId){
        allTokenIndex.set(tokenId,BigInteger.valueOf(allTokens.size()));
        allTokens.add(tokenId);
    }

    private void removeTokenFromOwnerEnumeration(Address from, BigInteger tokenId){
        BigInteger lastTokenIndex = BigInteger.valueOf(super.balanceOf(from)).subtract(BigInteger.ONE);
        BigInteger tokenInd = ownedTokenIndex.at(tokenId).get(tokenId.intValue());

        Context.require(tokenInd.equals(lastTokenIndex));
        BigInteger lastTokenId = ownedToken.get(from).at(lastTokenIndex.intValue());

        var tokens = ownedToken.get(from);
        ownedToken.set(from,tokens);
        tokens.add(lastTokenId);

        ownedTokenIndex.at(lastTokenId).add(tokenId);

        ownedTokenIndex.at(tokenId).removeLast();
        ownedToken.get(from).remove(lastTokenIndex);

    }

    private void removeTokenFromAllTokensEnumeration(BigInteger tokenId){
        BigInteger lastTokenIndex = BigInteger.valueOf(allTokens.size()).subtract(BigInteger.ONE);
        BigInteger tokenIndex = allTokenIndex.get(tokenId);
        BigInteger lastTokenId = allTokenIndex.get(lastTokenIndex);

        allTokens.set(tokenIndex.intValue(),lastTokenId);
        allTokenIndex.set(lastTokenId,tokenIndex);

        allTokenIndex.set(tokenId,null);
        allTokens.pop();

    }

}
