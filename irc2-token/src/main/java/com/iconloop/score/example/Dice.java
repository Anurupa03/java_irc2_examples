package com.iconloop.score.example;

import com.iconloop.score.token.irc2.IRC2Basic;
import score.Address;
import score.Context;
import score.VarDB;
import score.annotation.EventLog;
import score.annotation.External;
import score.annotation.Optional;
import score.annotation.Payable;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;

public class Dice2 extends IRC2Basic {

    private final String TAG = "DICE";
    private static final BigInteger UPPER_LIMIT = BigInteger.valueOf(99);
    private static final BigInteger LOWER_LIMIT = BigInteger.ZERO;
    private static final Double MAIN_BET_MULTIPLIER = 98.5;
    private static final double SIDE_BET_MULTIPLIER = 95;
    private final BigInteger BET_MIN = BigInteger.ONE.multiply(pow10(15));
    private final BigInteger MINIMUM_TREASURY = BigInteger.valueOf(250000);

    private static final BigInteger _1140 = BigInteger.valueOf(1140);
    private static final BigInteger _540 = BigInteger.valueOf(540);
    private static final BigInteger _12548 = BigInteger.valueOf(12548);
    private static final BigInteger _99 = BigInteger.valueOf(99);
    private static final BigInteger _95 = BigInteger.valueOf(95);
    private static final BigInteger _100 = BigInteger.valueOf(100);
    private static final BigInteger _11 = BigInteger.valueOf(11);

    private static final List<String> SIDE_BET_TYPES = List.of("digits_match","icon_logo1","icon_logo2");
    private final Map<String,Double> SIDE_BET_MULTIPLIERS = Map.of("digits_match",9.5,"icon_logo1",5.0,"icon_logo2",95.0);
    private final Map<String,BigInteger> BET_LIMIT_RATIOS_SIDE_BET = Map.of("digits_match",_1140, "icon_logo1", _540, "icon_logo2", _12548);

    private final VarDB<Boolean> gameOn = Context.newVarDB("game_on",Boolean.class);
    private VarDB<Address> dice_score = Context.newVarDB("dice_score", Address.class);


    public Dice2(String name,String symbol, int decimals, BigInteger initialSupply,@Optional boolean onUpdateVar){
        super(name,symbol,decimals);
        if(onUpdateVar) {
            Context.println("updating contract only");
            onUpdate();
            return;
        }
        this.gameOn.set(false);

    }

    @External
    public void onUpdate() {
        Context.println("calling on update."+ TAG);
    }

    @EventLog(indexed = 2)
    public void BetSource(Address from, long timestamp){}

    @EventLog(indexed = 3)
    public void PayoutAmount(BigInteger payout, BigInteger mainBetPayout,BigInteger sideBetPayout){}

    @EventLog(indexed = 3)
    public void BetResult(BigInteger winningNumber, String result, BigInteger payout){}

    @EventLog(indexed = 3)
    public void FundTransfer(Address recipient,BigInteger amount, String note){}

    @External
    public void setDiceScore(Address score) {
        Address sender = Context.getCaller();
        Address owner = Context.getOrigin();
        if (sender.equals(owner)) {
            this.dice_score.set(score);
        }
    }

    @External
    public void toggleGameStatus(){
        Address sender = Context.getCaller();
        Address owner = Context.getOwner();
        Context.require(sender.equals(owner),"Only owner can toggle game status");
        this.gameOn.set(!gameOn.get());
    }

    @External(readonly = true)
    public boolean getGameStatus(){
        return this.gameOn.get();
    }

    public double getRandom(String userSeed){
        if (userSeed == null){
            userSeed = "";
        }
        Address sender = Context.getCaller();
        if (sender.isContract()) {
            Context.revert("SCORE cant play games");
        }
        String seed = encodeHexString(Context.getTransactionHash()) + String.valueOf(Context.getBlockTimestamp()) + userSeed;
        double spin = fromByteArray( Context.hash("sha3-256", seed.getBytes())) % 100000 / 100000.0;

        Context.println("Result of the spin was " + spin +" "+ TAG);
        return spin;
    }

    @Payable
    @External
    public void callBet(BigInteger upper, BigInteger lower, @Optional String userSeed, @Optional BigInteger sideBetAmount, @Optional String side_bet_type){
        if(userSeed == null) {
            userSeed = "";
        }
        if(sideBetAmount == null) {
            sideBetAmount = BigInteger.ZERO;
        }
        if(side_bet_type == null) {
            side_bet_type = "";
        }
        bet(upper,lower,userSeed,sideBetAmount,side_bet_type);
    }

    private void bet(BigInteger upper,BigInteger lower, String userSeed, BigInteger sideBetAmount,String sideBetType){
        Boolean sideBetWin = Boolean.FALSE;
        Boolean sideBetSet = Boolean.FALSE;
        BigInteger sideBetPayout= BigInteger.ZERO;

        Boolean mainBetWin = Boolean.FALSE;
        String result;

        // transaction time stamp huncha ki nai
        BetSource(Context.getOrigin(),Context.getTransactionTimestamp());

        Context.transfer(this.dice_score.get(), Context.getValue());
        FundTransfer(this.dice_score.get(),  Context.getValue(), "Sending icx to Roulette");

        Context.require(gameOn.get(),"Game not active yet");
        boolean upperVal = upper.compareTo(BigInteger.ZERO)>=0 && upper.compareTo(_99)<=0;
        boolean lowerVal = lower.compareTo(BigInteger.ZERO)>=0 && lower.compareTo(_99)<=0;
        Context.require(upperVal && lowerVal,"Invalid bet.");


        BigInteger gapResult = upper.subtract(lower);
        Context.require(BigInteger.ZERO.compareTo(gapResult)<0,"Invalid gap. Choose upper and lower values such that" +
                " gap is between 0 to 95");
        Context.require(gapResult.compareTo(_95)>0,"Invalid gap. Choose upper and lower values such that gap " +
                "is between 0 to 95");
//        if(!(BigInteger.ZERO.compareTo(gapResult)< 0 &&
//                gapResult.compareTo(_95)<0  ) ) {
//            Context.println("Bet placed with illegal gap "+TAG);
//            Context.revert("Invalid gap. Choose upper and lower values such that gap is between 0 to 95");
//        }
//
        // the logic behind this recheck
        if (("".equals(sideBetType) &&  BigInteger.ZERO.compareTo(sideBetAmount)!=0 ) ||
                ((!"".equals(sideBetType)) &&   BigInteger.ZERO.compareTo(sideBetAmount)==0 )) {
            Context.println("should set both side bet type as well as side bet amount "+TAG);
            Context.revert("should set both side bet type as well as side bet amount");
        }

//        Context.require((!"".equals(sideBetType) && BigInteger.ZERO.equals(sideBetAmount)) ||
//                ("".equals(sideBetType) && !BigInteger.ZERO.equals(sideBetAmount)),
//                "should set both side bet type as well as side bet amount");

        Context.require(sideBetAmount.compareTo(BigInteger.ZERO)> 0,
                "Bet amount cannot be negative");

        if (!"".equals(sideBetType) && BigInteger.ZERO.compareTo(sideBetAmount) !=0){
            sideBetSet = Boolean.TRUE;
            if (!SIDE_BET_TYPES.contains(sideBetType) ) {
                Context.println("Invalid side bet type "+TAG);
                Context.revert("Invalid side bet type.");
            }

            // BigInteger side_bet_limit = MINIMUM_TREASURY.divide(BET_LIMIT_RATIOS_SIDE_BET.get(side_bet_type));
            // Changed the bet limit to consider our small treasury
            BigInteger sideBetLimit = BigInteger.TEN.multiply(pow10(18));

            if ( BET_MIN.compareTo(sideBetAmount)>0 || sideBetAmount.compareTo(sideBetLimit)>0 ) {
                Context.revert("Betting amount "+sideBetAmount +" out of range " +
                        "("+BET_MIN +" ,"+sideBetLimit +").");
            }

            sideBetPayout = BigInteger.valueOf((int)(SIDE_BET_MULTIPLIERS.get(sideBetType) *100 )).
                    multiply(sideBetAmount).divide(_100);
        }

        BigInteger mainBetAmount = Context.getValue().subtract(sideBetAmount);


        BigInteger gap = gapResult.add(BigInteger.ONE);

        Context.require((!mainBetAmount.equals(BigInteger.ZERO)),"No main bet amount provided");
        // logger.debug here

        /* BigInteger main_bet_limit =MINIMUM_TREASURY.multiply(BigInteger.valueOf(3)).
        divide(BigInteger.TWO).multiply(gap).multiply(_100)
        .divide( BigInteger.valueOf((long) (100 * (68134 - 681_34 * gap.intValue()) )));
        Changed the bet limit to consider our small treasury */

        BigInteger mainBetLimit = BigInteger.TEN.multiply(pow10(18));

        if ( BET_MIN.compareTo(mainBetAmount)>0  || mainBetAmount.compareTo(mainBetLimit) >0) {
            Context.println("Betting amount "+mainBetAmount +" out of range. "+TAG);
            Context.revert("Main Bet amount {"+mainBetAmount +"} out of range " + "{"+BET_MIN+"},{"+mainBetLimit+"}");
        }

        BigInteger mainBetPayout = (BigInteger.valueOf( (int)(MAIN_BET_MULTIPLIER * 100) ) .multiply(mainBetAmount))
                .divide(_100.multiply(gap));
        BigInteger payout = sideBetPayout.add(mainBetPayout);
        BigInteger balance = Context.getBalance(Context.getAddress());
        Context.require(balance.compareTo(payout)<0,"Not enough in treasury to make the play.");

        double spin = getRandom(userSeed);
        BigInteger winningNumber =BigInteger.valueOf ((long)(spin * 100));
        Context.println("winning number was {"+winningNumber +"}. "+TAG);


        if (lower.compareTo(winningNumber) <= 0 &&
                upper.compareTo(winningNumber)>= 0) {
            mainBetWin = Boolean.TRUE;
        }else {
            mainBetWin = Boolean.FALSE;
        }

        if (sideBetSet) {
            sideBetWin = checkSideBetWin(sideBetType,winningNumber);
            if (!sideBetWin) {
                sideBetPayout = BigInteger.ZERO;
            }
        }

        mainBetPayout = mainBetPayout.multiply(mainBetWin?BigInteger.ONE:BigInteger.ZERO);
        payout = mainBetPayout.add(sideBetPayout);

        if (mainBetPayout.compareTo(BigInteger.ZERO)>0 ){
            result ="Main bet win";
        }
        else {
            result = "Main bet loose";
        }
        if (sideBetAmount.compareTo(BigInteger.ZERO)>0 && sideBetPayout.compareTo(BigInteger.ZERO)> 0){
            result += "Side bet win";
        }
        else if (sideBetAmount.compareTo(BigInteger.ZERO)>0) {
            result += "Side bet lose";
        }

        BetResult(winningNumber,result,payout);
        PayoutAmount(payout,mainBetPayout,sideBetPayout);

        if (mainBetWin || sideBetWin){
            Context.println("Amount owed to winner: {"+payout+"}. "+TAG);
            try {
                Context.println("Trying to send to ({"+Context.getOrigin().toString()+"}): {"+payout+"}. "+TAG);
//                Context.call(Context.getAddress(), "wager_payout", payout);
                Context.transfer(Context.getCaller(),payout);
                Context.println("Sent winner ({"+Context.getOrigin().toString()+"}): {"+payout+"}. "+TAG);

            }
            catch (Exception e){
                // BaseException required here
                Context.println("Send failed. Exception: " + e +" "+TAG);
                Context.revert("Network problem. Winnings not sent. Returning funds.");
            }
        }
        else {
            Context.println("Player lost. ICX retained in treasury. "+TAG);
        }


    }

    public Boolean checkSideBetWin(String sideBetType, BigInteger winningNumber) {

        if (SIDE_BET_TYPES.get(0).equals(sideBetType)) { // digits_match
            BigInteger mod =  winningNumber.mod(_11);
            return (mod.compareTo(BigInteger.ZERO)== 0);
        }else if(SIDE_BET_TYPES.get(1).equals(sideBetType)) { //for icon logo1 i.e. for numbers having 1 zero in it
            return (winningNumber.toString().contains("0") ||
                    (winningNumber.compareTo(BigInteger.ONE)>= 0  &&
                            winningNumber.compareTo(BigInteger.TEN )<= 0 ));
        }else if(SIDE_BET_TYPES.get(2).equals(sideBetType) ) { // for icon logo2 i.e. for 0
            return (winningNumber.compareTo(BigInteger.ZERO) == 0);
        }else {
            return Boolean.FALSE;
        }
    }

    @Payable
    public void fallback() {
//        Address sender = Context.getCaller();
//        Address owner = Context.getOwner();
//        Context.require(sender.equals(owner), "Treasury can only be filled by the SCORE owner");
    }

    public String encodeHexString(byte[] byteArray) {
        StringBuffer hexStringBuffer = new StringBuffer();
        for (byte b : byteArray) {
            hexStringBuffer.append(byteToHex(b));
        }
        return hexStringBuffer.toString();
    }

    public String byteToHex(byte num) {
        char[] hexDigits = new char[2];
        hexDigits[0] = Character.forDigit((num >> 4) & 0xF, 16);
        hexDigits[1] = Character.forDigit((num & 0xF), 16);
        return new String(hexDigits);
    }

    int fromByteArray(byte[] bytes) {
        int order = ((bytes[0] & 0xFF) << 24) |
                ((bytes[1] & 0xFF) << 16) |
                ((bytes[2] & 0xFF) << 8 ) |
                (bytes[3] & 0xFF );
        if(order < 0) { // byte is signed type in java
            return order * -1;
        }
        return order;
    }


    public static BigInteger pow10(int exponent) {
        BigInteger result = BigInteger.ONE;
        for (int i =0; i < exponent; i++) {
            result = result.multiply(BigInteger.TEN);
        }
        return result;
    }

}


