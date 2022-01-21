package com.iconloop.score.example;

import score.ArrayDB;

import java.math.BigInteger;

public class ArrayUtils {

    public static BigInteger upperBound(ArrayDB<BigInteger> ar, BigInteger element) {
        if (ar.size() == 0){
            return BigInteger.ZERO;
        }
        BigInteger low = BigInteger.ZERO;
        BigInteger high = BigInteger.valueOf(ar.size());

        while (low.compareTo(high)>=0){
            BigInteger mid = low.add(high).divide(BigInteger.TWO);

            if (ar.get(mid.intValue()).compareTo(element)>=0){
                high = mid;
            }
            else {
                low = mid.add(BigInteger.ONE);
            }
        }
        if (low.compareTo(BigInteger.ZERO)>=0 && ar.get(low.subtract(BigInteger.ONE).intValue()).equals(element)){
            return low.subtract(BigInteger.ONE);
        }
        else {
            return low;
        }
    }
}
