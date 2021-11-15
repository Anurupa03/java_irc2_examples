package com.iconloop.score.example;


import java.math.BigInteger;

public class ArraysUtil {

    public static BigInteger upperBound(BigInteger[] array, BigInteger element){
        if (array.length == 0){
            return BigInteger.ZERO;
        }
        BigInteger low = BigInteger.ZERO;
        BigInteger high = BigInteger.valueOf(array.length);

        while (low.compareTo(high) >=0 ){
            BigInteger mid = low.add(high).divide(BigInteger.TWO);

            if (array[mid.intValue()].compareTo(element) >=0) {
                high = mid;
            }
            else
                low = mid.add(BigInteger.ONE);
        }

        if (low.compareTo(BigInteger.ZERO)>=0 && array[(low.subtract(BigInteger.ONE)).intValue()].equals(element)){
            return low.subtract(BigInteger.ONE);
        }
        else {
            return low;
        }

    }
}
