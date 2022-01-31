package com.iconloop.score.example;

import com.iconloop.score.test.Account;
import com.iconloop.score.test.Score;
import com.iconloop.score.test.ServiceManager;
import com.iconloop.score.test.TestBase;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import score.Context;

import java.math.BigInteger;

import static java.math.BigInteger.TEN;
import static java.math.BigInteger.valueOf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.spy;

public class DiceTest extends TestBase {
    private static final String name = "DICE";
    private static final String symbol = "DIC";
    private static final int decimals = 18;
    private static final BigInteger initialSupply = BigInteger.valueOf(1000);
    private static final Boolean onUdateVar = false;
    private static final BigInteger totalSupply = initialSupply.multiply(TEN.pow(decimals));

    private static final ServiceManager sm = getServiceManager();
    private static final Account owner = sm.createAccount();
    private static final Account testAccount = sm.createAccount();
    private static Score tokenScore;
    private static Dice tokenSpy;

    @BeforeAll
    public static void setup() throws Exception{
        tokenScore = sm.deploy(owner,Dice.class,onUdateVar);
        owner.addBalance(symbol, totalSupply);

        tokenSpy = (Dice) spy(tokenScore.getInstance());
        tokenScore.setInstance(tokenSpy);
    }

    public void bet(Account owner,BigInteger upper,BigInteger lower, String userSeed, BigInteger sideBetAmount,
                    String sideBetType){
        tokenScore.invoke(owner,"set_dice_score",tokenScore.getAddress());
//        tokenScore.invoke(owner,"set_dice_score","cxe36ce66a334f0ef311cb29d7833ebbfe2637f4e1");
        tokenScore.invoke(owner,"toggle_game_status");
        try {
            tokenScore.invoke(owner,"call_bet",upper,lower,userSeed,sideBetAmount,sideBetType);
        }
        catch (AssertionError error){
            throw error;
        }
    }

    public void expectErrorMessage(Executable contractCall, String errorMessage) {
        AssertionError e = Assertions.assertThrows(AssertionError.class, contractCall);
        assertEquals(errorMessage, e.getMessage());
    }

    @Test
    void toggle_game_status(){
        assertEquals(false,tokenScore.call("get_game_status"));
        tokenScore.invoke(owner,"toggle_game_status");
        assertEquals(true,tokenScore.call("get_game_status"));
    }

    @DisplayName("Range of upper and lower value")
    @Test
    void upper_and_lower_range_test(){
        BigInteger upper = BigInteger.valueOf(7);
        BigInteger lower = BigInteger.valueOf(20);
        String userSeed = "";
        BigInteger sideBetAmount = BigInteger.valueOf(2).multiply(TEN.pow(decimals));
        String sideBetType = "digits_match";

        Executable upperCall = () -> bet(owner,BigInteger.valueOf(100),lower,userSeed,sideBetAmount,sideBetType);
        String expectedErrorMessage = "Reverted(0): Invalid bet. Choose a number between 0 to 99";
        expectErrorMessage(upperCall,expectedErrorMessage);
    }

    @Test
    void gap_test(){
        BigInteger upper = BigInteger.valueOf(20);
        BigInteger lower = BigInteger.valueOf(2);
        String userSeed = "";
        BigInteger sideBetAmount = BigInteger.valueOf(2).multiply(TEN.pow(decimals));
        String sideBetType = "digits_match";

        Executable upperAndLowerCall = () -> bet(owner,BigInteger.valueOf(99),lower,userSeed,sideBetAmount,sideBetType);
        String expectedErrorMessage = "Reverted(0): Invalid gap. Choose upper and lower values such that gap is between 0 to 95";
        expectErrorMessage(upperAndLowerCall,expectedErrorMessage);

    }

    @Test
    void side_bet_test(){
        BigInteger upper = BigInteger.valueOf(70);
        BigInteger lower = BigInteger.valueOf(20);
        String userSeed = "";
        BigInteger sideBetAmount = BigInteger.ZERO;
        String sideBetType = "digits_match";

        Executable upperAndLowerCall = () -> bet(owner,BigInteger.valueOf(99),lower,userSeed,sideBetAmount,sideBetType);
        String expectedErrorMessage = "Reverted(0): should set both side bet type as well as side bet amount";
        expectErrorMessage(upperAndLowerCall,expectedErrorMessage);
    }

    @Test
    void betTest(){
        tokenScore.invoke(owner,"set_dice_score",tokenScore.getAddress());
        tokenScore.invoke(owner,"toggle_game_status");
        BigInteger upper = BigInteger.valueOf(70);
        BigInteger lower = BigInteger.valueOf(20);
        String userSeed = "";
        BigInteger sideBetAmount = BigInteger.valueOf(2).multiply(TEN.pow(decimals));
        String sideBetType = "digits_match";
        // tokenScore.call(owner,true,BigInteger.valueOf(100),"callBet",upper,lower,userSeed,sideBetAmount,sideBetType);
        tokenScore.invoke(owner,"call_bet",upper,lower,userSeed,sideBetAmount,sideBetType);


    }
}
