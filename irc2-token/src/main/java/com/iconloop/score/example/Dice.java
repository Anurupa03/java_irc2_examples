package com.iconloop.score.example;

import score.Address;
import score.Context;
import score.VarDB;
import score.annotation.EventLog;
import score.annotation.External;
import score.annotation.Payable;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.time.LocalDateTime;
import java.util.Map;

public class Dice {

    private final VarDB<Boolean> gameOn = Context.newVarDB("gameOn",Boolean.class);
    private static final int upperLimit = 99; // static is used so the default value is thus
    private static final int lowerLimit = 0;
    private static final double mainBetMultiplier = 98.5;
    private static final double sideBetMultiplier = 95;
    private static final String[] sideBetTypes = {"digits_match","icon_logo1","icon_logo2"};
    private Map<String,Double> sideBetMultipliers;
    private Map<String,Integer> betLimitRatiosSideBet;
    private final BigInteger betMin = BigInteger.ONE.multiply(pow10(15)); // int is small to store betMin
//    private int minimumTreasury = 250000;


    public Dice(Boolean gameStatus){

        sideBetMultipliers.put("digits_match",9.5);
        sideBetMultipliers.put("icon_logo1", 5.0);
        sideBetMultipliers.put("icon_logo2", 95.0);

        betLimitRatiosSideBet.put("digits_match",1140);
        betLimitRatiosSideBet.put("icon_logo1",540);
        betLimitRatiosSideBet.put("icon_logo2",12548);  // maybe map sideBetTypes array to this

        this.gameOn.set(gameStatus);
    }

    public static BigInteger pow10(int exponent) {
        BigInteger result = BigInteger.ONE;
        for (int i =0; i < exponent; i++) {
            result = result.multiply(BigInteger.TEN);
        }

        return result;
    }

    // EventLogs
    @EventLog(indexed = 2)
    public void BetSource(Address _from, long timestamp){}

    @EventLog(indexed = 3)
    public void PayoutAmount(int payout, int mainBetPayout, int sideBetPayout){}

    @EventLog(indexed = 3)
    public void BetResult(int winningNumber, String result, int payout){}

    @EventLog(indexed = 3)
    public void FundTransfer(Address recipient,int amount, String note){}

    @External
    public void toggleGameStatus(){
        Context.require(Context.getCaller().equals(Context.getOwner()),"Only owner can toggle game status");
        gameOn.set(!gameOn.get());
    }

    @External(readonly = true)
    public boolean getGameStatus(){
        return gameOn.get();
    }

//    BigInteger hh = new BigInteger(s);
//    float so = ((hh.floatValue() % 10000)/10000.0F);


    private float getRandom(String userSeed){
        String transactionHash = new String(Context.getTransactionHash());
        String seed = transactionHash + LocalDateTime.now() + userSeed;
        return ByteBuffer.wrap(Context.hash("sha3-256",seed.getBytes())).getInt() % 10000 / 10000.0F;

    }

    @Payable
    @External
    public void callBet(int upper, int lower, String userSeed, BigInteger sideBetAmount, String sideBetType){
        bet(upper, lower, userSeed, sideBetAmount, sideBetType);
    }

    private void bet(int upper, int lower, String userSeed, BigInteger sideBetAmount, String sideBetType){
        boolean sideBetWin = false;
        boolean sideBetSet = false;
        int sideBetPayout = 0;

        BetSource(Context.getOrigin(),Context.getTransactionTimestamp()); // returns long in python we returned int

        // check if game is on
        Context.require(gameOn.get(),"Game not active yet");

        Context.require(upper <=99 && lower <=99 && upper >= 0 && lower >=0,
                "Invalid bet. Choose a number between 0 to 99");
        Context.require((upper - lower) <=95 && (upper-lower)>=0,
                "Invalid gap. Choose upper and lower values such that gap is between 0 to 95");
        Context.require(!sideBetType.isEmpty() && sideBetAmount.equals(0) || sideBetType.isEmpty()
                && !sideBetAmount.equals(0),"should set both side bet type as well as side bet amount"); // check this condition again
        Context.require(sideBetAmount.compareTo(BigInteger.ZERO)< 0, "Bet amount cannot be negative");
        if (!sideBetType.isEmpty() && !sideBetAmount.equals(0)){
            sideBetSet = true;
            // search side bet types
            for (String element : sideBetTypes){
                Context.require(element.contains(sideBetType),"Invalid side bet type");
            }
            BigInteger sideBetLimit = BigInteger.TEN.multiply(pow10(18));
            if (sideBetAmount.compareTo(betMin)>0  || sideBetAmount.compareTo(sideBetLimit)>0){
                Context.revert("Betting amount" + sideBetAmount +  "out of range" + "(" + betMin +","+sideBetLimit + ").");
            }
            // yo flow milcha?
            sideBetPayout = (int) (sideBetMultipliers.get(sideBetType) * 100) * sideBetAmount.intValue() / 100;
        }
        // sideBet amount to Biginteger (consider this)
        BigInteger mainBetAmount = Context.getValue().subtract(sideBetAmount);
        int gap = (upper - lower) + 1;
        Context.require(!mainBetAmount.equals(BigInteger.ZERO),"No main bet amount provided");
        // logger.debug here in java
        // what is that?

        BigInteger mainBetLimit = BigInteger.TEN.multiply(pow10(18));
        if (mainBetAmount.compareTo(betMin)<0 || mainBetAmount.compareTo(mainBetLimit) >0){
            Context.revert("Main bet amount" + mainBetAmount + "out of range" + betMin +"," + mainBetLimit);
        }
        int mainBetPayout = ((int)(mainBetMultiplier) * 100) * mainBetAmount.intValue() / (100 * gap);
        int payout = sideBetPayout + mainBetPayout;

        if (Context.getBalance(Context.getCaller()).compareTo(BigInteger.valueOf(payout)) < 0){
            Context.revert("Not enough in treasury to make the play.");
        }
        float spin = getRandom(userSeed); // define getRandom method
        int winningNumber = (int)(spin * 100); 
        boolean mainBetWin;
        mainBetWin = lower <= winningNumber && winningNumber <= upper;
        if (sideBetSet){
            sideBetWin= checkSideBetWin(sideBetType,winningNumber);
            if (!sideBetWin){
                sideBetPayout = 0;
            }
        }
        mainBetPayout = mainBetPayout * ((mainBetWin) ? 1:0); // can boolean be multiplied???
        payout = mainBetPayout + sideBetPayout;
        String result;
        if (mainBetPayout > 0){
            result ="Main bet win";
        }
        else {
            result = "Main bet loose";
        }
        if (sideBetAmount.compareTo(BigInteger.ZERO)>0 && sideBetPayout > 0){
            result += "Side bet win";
        }
        else if (sideBetAmount.compareTo(BigInteger.ZERO)>0) {
            result += "Side bet lose";
        }
        BetResult(winningNumber,result,payout);
        PayoutAmount(payout,mainBetPayout,sideBetPayout);

        if (mainBetWin || sideBetWin){
            try {
                Context.transfer(Context.getCaller(),BigInteger.valueOf(payout));
            }
            catch (Exception e){ // how to import exception
                // BaseException required here
                Context.revert("Network problem. Winnings not sent. Returning funds.");
            }
        }
    }

    public boolean checkSideBetWin(String sideBetType, int winningNumber){
        if (sideBetType.equals(sideBetTypes[0])){ // digits_match
            return (winningNumber % 11 == 0);
        }
        if (sideBetType.equals(sideBetTypes[1])){ // icon logo1
            return winningNumber == 1;
        }
        if (sideBetType.equals(sideBetTypes[2])){ // icon logo2
            return (winningNumber == 0);
        }
        else {
            return false;
        }
    }

    @Payable
    public void fallback(){
        Context.require(Context.getCaller().equals(Context.getOwner()),
                "Treasury can only be filled by the SCORE owner");
    }




}
