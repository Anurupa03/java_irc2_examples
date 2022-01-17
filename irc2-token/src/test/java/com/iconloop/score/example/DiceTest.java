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

import java.math.BigInteger;

import static java.math.BigInteger.TEN;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.spy;

public class DiceTest extends TestBase {
    private static final String name = "DICE";
    private static final String symbol = "DIC";
    private static final int decimals = 18;
    private static final BigInteger initialSupply = BigInteger.valueOf(1000);
    private static Boolean onUdateVar = false;
    private static BigInteger totalSupply = initialSupply.multiply(TEN.pow(decimals));

    private static final ServiceManager sm = getServiceManager();
    private static final Account owner = sm.createAccount();
    private static final Account testAccount = sm.createAccount();
    private static Score tokenScore;
    private static Dice2 tokenSpy;

    @BeforeAll
    public static void setup() throws Exception{
        tokenScore = sm.deploy(owner,Dice2.class,name,symbol,decimals,initialSupply,onUdateVar);
        owner.addBalance(symbol, totalSupply);

        tokenSpy = (Dice2) spy(tokenScore.getInstance());
        tokenScore.setInstance(tokenSpy);
    }

    @Test
    void toggle_game_status(){
        assertEquals(false,tokenScore.call("getGameStatus"));
        tokenScore.invoke(owner,"toggleGameStatus");
        assertEquals(true,tokenScore.call("getGameStatus"));
    }

    public void bet(Account owner,BigInteger upper,BigInteger lower, String userSeed, BigInteger sideBetAmount,
                    String sideBetType){
        tokenScore.invoke(owner,"setDiceScore",tokenScore.getAddress());
//        tokenScore.invoke(owner,"toggleGameStatus");
        try {
            tokenScore.invoke(owner,"callBet",upper,lower,userSeed,sideBetAmount,sideBetType);
        }
        catch (AssertionError error){
            throw error;
        }
    }

    public void expectErrorMessage(Executable contractCall, String errorMessage) {
        AssertionError e = Assertions.assertThrows(AssertionError.class, contractCall);
        assertEquals(errorMessage, e.getMessage());
    }

    @DisplayName("Range of upper and lower value")
    @Test
    void upper_and_lower_range_test(){
        BigInteger upper = BigInteger.valueOf(7);
        BigInteger lower = BigInteger.valueOf(20);
        String userSeed = "";
        BigInteger sideBetAmount = BigInteger.valueOf(2).multiply(TEN.pow(decimals));
        String sideBetType = "digits_match";

        tokenScore.invoke(owner,"toggleGameStatus");

        Executable upperCall = () -> bet(owner,BigInteger.valueOf(100),lower,userSeed,sideBetAmount,sideBetType);
        String expectedErrorMessage = "Invalid bet.";
        expectErrorMessage(upperCall,expectedErrorMessage);

        Executable LowerCall = () -> bet(owner,upper,BigInteger.valueOf(100),userSeed,sideBetAmount,sideBetType);
        String expectedErrorMessageLower = "Invalid bet.";
        expectErrorMessage(LowerCall,expectedErrorMessageLower);


    }

    @Test
    void gap_test(){
        BigInteger upper = BigInteger.valueOf(7);
        BigInteger lower = BigInteger.valueOf(20);
        String userSeed = "";
        BigInteger sideBetAmount = BigInteger.valueOf(2).multiply(TEN.pow(decimals));
        String sideBetType = "digits_match";

        Executable upperAndLowerCall = () -> bet(owner,BigInteger.valueOf(96),lower,userSeed,sideBetAmount,sideBetType);
        String expectedErrorMessage = "Invalid gap. Choose upper and lower values such that gap is between 0 to 95";
        expectErrorMessage(upperAndLowerCall,expectedErrorMessage);
    }

    @Test
    void betTest(){
        tokenScore.invoke(owner,"setDiceScore",tokenScore.getAddress());
        tokenScore.invoke(owner,"toggleGameStatus");
        BigInteger upper = BigInteger.valueOf(70);
        BigInteger lower = BigInteger.valueOf(20);
        String userSeed = "";
        BigInteger sideBetAmount = BigInteger.valueOf(2).multiply(TEN.pow(decimals));
        String sideBetType = "digits_match";
        tokenScore.invoke(owner,"callBet",upper,lower,userSeed,sideBetAmount,sideBetType);

       
    }
}
