package com.iconloop.score.example;

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

public class Dice {
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
    private static final BigInteger _95 = BigInteger.valueOf(95);
    private static final BigInteger _100 = BigInteger.valueOf(100);
    private static final BigInteger _11 = BigInteger.valueOf(11);

    private static final List<String> SIDE_BET_TYPES = List.of("digits_match","icon_logo1","icon_logo2");
    private final Map<String,Double> SIDE_BET_MULTIPLIERS = Map.of("digits_match",9.5,"icon_logo1",5.0,"icon_logo2",95.0);
    private final Map<String,BigInteger> BET_LIMIT_RATIOS_SIDE_BET = Map.of("digits_match",_1140, "icon_logo1", _540, "icon_logo2", _12548);

    private final VarDB<Boolean> gameOn = Context.newVarDB("game_on",Boolean.class);
    private final VarDB<Address> dice_score = Context.newVarDB("dice_score", Address.class); //treasury score

    public Dice(@Optional boolean on_update_var){
        if (on_update_var) {
            Context.println("updating contract only");
            onUpdate();

        }
        this.gameOn.set(false);

    }

    public void onUpdate() {
        Context.println("calling on update. " + TAG);
    }

    @EventLog(indexed = 3)
    public void BetPlaced(BigInteger amount, BigInteger upper, BigInteger lower) {
    }

    @EventLog(indexed = 2)
    public void BetSource(Address _from, BigInteger timestamp) {
    }

    @EventLog(indexed = 3)
    public void PayoutAmount(BigInteger payout, BigInteger main_bet_payout, BigInteger side_bet_payout) {
    }

    @EventLog(indexed = 3)
    public void BetResult(String spin, BigInteger winningNumber, BigInteger payout) {
    }

    @EventLog(indexed = 2)
    public void FundTransfer(Address recipient, BigInteger amount, String note) {
    }

    @External(readonly = true)
    public Address get_score_owner(){
        return Context.getOwner();
    }

    @External
    public void set_dice_score(Address _score){
        Address sender = Context.getCaller();
        Address owner = Context.getOwner();
        if (sender.equals(owner)){
            this.dice_score.set(_score);
        }
    }

    @External(readonly = true)
    public Address get_dice_score(){
        return this.dice_score.get();
    }

    @External
    public void toggle_game_status(){
        Address sender = Context.getCaller();
        Address owner = Context.getOwner();
        Context.require(sender.equals(owner),"Only owner can toggle game status");
        this.gameOn.set(!gameOn.get());
    }

    @External(readonly = true)
    public boolean get_game_status(){
        return this.gameOn.get();
    }

    public double getRandom(String user_seed){
        if (user_seed==null){
            user_seed ="";
        }
        Address sender = Context.getCaller();
        Context.require(!sender.isContract(),"Score can not play games");
        String seed = encodeHexString(Context.getTransactionHash()) + String.valueOf(Context.getBlockTimestamp()) + user_seed;
        double spin = fromByteArray( Context.hash("sha3-256", seed.getBytes())) % 100000 / 100000.0;

        Context.println("Result of the spin was " + spin +" "+ TAG);
        return spin;
    }

    @Payable
    @External
    public void call_bet(BigInteger upper, BigInteger lower, @Optional String user_seed, @Optional BigInteger side_bet_amount, @Optional String side_bet_type){
        if(user_seed == null) {
            user_seed = "";
        }
        if(side_bet_amount == null) {
            side_bet_amount = BigInteger.ZERO;
        }
        if(side_bet_type == null) {
            side_bet_type = "";
        }
        bet(upper,lower,user_seed,side_bet_amount,side_bet_type);
    }

    private void bet(BigInteger upper, BigInteger lower, String user_seed,BigInteger side_bet_amount,String side_bet_type) {
        Boolean side_bet_win = Boolean.FALSE;
        Boolean side_bet_set = Boolean.FALSE;
        BigInteger side_bet_payout = BigInteger.ZERO;

        Boolean main_bet_win = Boolean.FALSE;

        BetSource(Context.getOrigin(), BigInteger.valueOf(Context.getBlockTimestamp()));

        Context.transfer(this.dice_score.get(), Context.getValue());
        FundTransfer(this.dice_score.get(), Context.getValue(), "Sending icx to Dice score");
        //Context.call(this.dice_score.get(), "take_wager", Context.getValue());

        Context.require(this.gameOn.get(), "Game not active yet");

        if (!((upper.compareTo(LOWER_LIMIT) >= 0 && upper.compareTo(UPPER_LIMIT) <= 0) &&
                (lower.compareTo(LOWER_LIMIT) >= 0 && lower.compareTo(UPPER_LIMIT) <= 0))) {
            Context.println("Numbers placed with out of range numbers " + TAG);
            Context.revert("Invalid bet. Choose a number between 0 to 99");
        }

        BigInteger gapResult = upper.subtract(lower);
        if (!(BigInteger.ZERO.compareTo(gapResult) < 0 &&
                gapResult.compareTo(_95) < 0)) {
            Context.println("Bet placed with illegal gap " + TAG);
            Context.revert("Invalid gap. Choose upper and lower values such that gap is between 0 to 95");
        }

        if (("".equals(side_bet_type) && BigInteger.ZERO.compareTo(side_bet_amount) != 0) ||
                ((!"".equals(side_bet_type)) && BigInteger.ZERO.compareTo(side_bet_amount) == 0)) {
            Context.println("should set both side bet type as well as side bet amount " + TAG);
            Context.revert("should set both side bet type as well as side bet amount");
        }

        Context.require(side_bet_amount.compareTo(BigInteger.ZERO) > 0,
                "Bet amount cannot be negative");

        if (!"".equals(side_bet_type) && BigInteger.ZERO.compareTo(side_bet_amount) != 0) {
            side_bet_set = Boolean.TRUE;
            if (!SIDE_BET_TYPES.contains(side_bet_type)) {
                Context.println("Invalid side bet type " + TAG);
                Context.revert("Invalid side bet type.");
            }

            // BigInteger side_bet_limit = MINIMUM_TREASURY.divide(BET_LIMIT_RATIOS_SIDE_BET.get(side_bet_type));
            // Changed the bet limit to consider our small treasury
            BigInteger side_bet_limit = BigInteger.TEN.multiply(pow10(18));
            if (BET_MIN.compareTo(side_bet_amount) > 0 || side_bet_amount.compareTo(side_bet_limit) > 0) {
                Context.revert("Betting amount " + side_bet_amount + " out of range " +
                        "(" + BET_MIN + " ," + side_bet_limit + ").");
            }
            side_bet_payout = BigInteger.valueOf((int) (SIDE_BET_MULTIPLIERS.get(side_bet_type) * 100)).
                    multiply(side_bet_amount).divide(_100);
        }

        BigInteger main_bet_amount =Context.getValue().subtract(side_bet_amount);
        BetPlaced(main_bet_amount, upper, lower);
        BigInteger gap = gapResult.add(BigInteger.ONE);

        Context.require((!main_bet_amount.equals(BigInteger.ZERO)),"No main bet amount provided");

        /* BigInteger main_bet_limit =MINIMUM_TREASURY.multiply(BigInteger.valueOf(3)).
                divide(BigInteger.TWO).multiply(gap).multiply(_100)
                .divide( BigInteger.valueOf((long) (100 * (68134 - 681_34 * gap.intValue()) )));
        Changed the bet limit to consider our small treasury */

        BigInteger main_bet_limit = BigInteger.TEN.multiply(pow10(18));

        if (BET_MIN.compareTo(main_bet_amount) > 0 || main_bet_amount.compareTo(main_bet_limit) > 0) {
            Context.println("Betting amount " + main_bet_amount.toString() + " out of range. " + TAG);
            Context.revert("Main Bet amount {" + main_bet_amount.toString() + "} out of range {" + BET_MIN.toString() + "},{" + main_bet_limit.toString() + "}");
        }

        BigInteger main_bet_payout = (BigInteger.valueOf( (int)(MAIN_BET_MULTIPLIER * 100) ) .multiply(main_bet_amount))
                .divide(_100.multiply(gap));

        BigInteger payout = side_bet_payout.add(main_bet_payout);
        BigInteger balance = Context.getBalance(Context.getAddress());
        Context.require(balance.compareTo(payout)>=0,"Not enough in treasury to make the play.");

        double spin = getRandom(user_seed);
        BigInteger winningNumber =BigInteger.valueOf ((long)(spin * 100));
        Context.println("winning number was {"+winningNumber +"}");

        if (lower.compareTo(winningNumber) <= 0 &&
                upper.compareTo(winningNumber) >= 0) {
            main_bet_win = Boolean.TRUE;
        } else {
            main_bet_win = Boolean.FALSE;
        }

        if (side_bet_set) {
            side_bet_win = check_side_bet_win(side_bet_type, winningNumber);
            if (!side_bet_win) {
                side_bet_payout = BigInteger.ZERO;
            }
        }

        main_bet_payout = main_bet_payout.multiply(main_bet_win ? BigInteger.ONE : BigInteger.ZERO);
        payout = main_bet_payout.add(side_bet_payout);
        BetResult(String.valueOf(spin), winningNumber, payout);
        PayoutAmount(payout, main_bet_payout, side_bet_payout);

        if (main_bet_win || side_bet_win) {
            Context.println("Amount owed to winner: {" + payout + "}. " + TAG);
            try {
                Context.println("Trying to send to ({" + Context.getOrigin().toString() + "}): {" + payout + "}. " + TAG);
                Context.call(this.dice_score.get(), "wager_payout", payout);
                Context.println("Sent winner ({" + Context.getOrigin().toString() + "}): {" + payout + "}. " + TAG);

            } catch (Exception e) {
                Context.println("Send failed. Exception: " + e + " " + TAG);
                Context.revert("Network problem. Winnings not sent. Returning funds.");

            }
        } else {
            Context.println("Player lost. ICX retained in treasury. " + TAG);
        }

    }

    public Boolean check_side_bet_win(String side_bet_type, BigInteger winning_number) {

        if (SIDE_BET_TYPES.get(0).equals(side_bet_type)) { //# digits_match
            BigInteger mod = winning_number.mod(_11);
            return (mod.compareTo(BigInteger.ZERO) == 0);
        } else if (SIDE_BET_TYPES.get(1).equals(side_bet_type)) { //for icon logo1 ie for numbers having 1 zero in it
            return (winning_number.toString().contains("0") ||
                    (winning_number.compareTo(BigInteger.ONE) >= 0 &&
                            winning_number.compareTo(BigInteger.TEN) <= 0));
        } else if (SIDE_BET_TYPES.get(2).equals(side_bet_type)) { //or icon logo2 ie for 0
            return (winning_number.compareTo(BigInteger.ZERO) == 0);
        } else {
            return Boolean.FALSE;
        }

    }

    @Payable
    public void fallback() {
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