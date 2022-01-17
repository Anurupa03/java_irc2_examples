//package com.iconloop.score.example;
//
//import com.iconloop.score.test.Account;
//import com.iconloop.score.test.Score;
//import com.iconloop.score.test.ServiceManager;
//import com.iconloop.score.test.TestBase;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//
//import java.math.BigInteger;
//
//
//import static org.mockito.Mockito.spy;
//
//class IRC2SnapshotTest extends TestBase {
//    private static final String name = "Snapshot";
//    private static final String symbol = "SAP";
//    private static final int decimals = 18;
//    private static final BigInteger initialSupply = BigInteger.valueOf(1000);
//    private static BigInteger totalSupply = initialSupply.multiply(BigInteger.TEN.pow(decimals));
//
//    private Score tokenScore;
//    private static final ServiceManager sm = getServiceManager();
//    private static final Account owner = sm.createAccount();
//    private static IRC2Snapshot3 tokenSpy;
//
//    @BeforeEach
//    void setup() throws Exception {
//        tokenScore = sm.deploy(owner, IRC2Snapshot3.class,
//                name,symbol,decimals,initialSupply );
//        owner.addBalance(symbol,totalSupply);
//
//        tokenSpy = (IRC2Snapshot3) spy(tokenScore.getInstance());
//        tokenScore.setInstance(tokenSpy);
//    }
//
//
//
//
//    @Test
//    void getCurrentSnapshotid(){
//        BigInteger currentId = BigInteger.ONE;
//        tokenScore.invoke(owner,"getCurrentSnapshotId");
//
//
//    }
//
//    @Test
//    void balanceOfAt(){
//        Account anurupa = sm.createAccount();
//        BigInteger id = (BigInteger) tokenScore.call("getCurrentSnapshotId2");
//
//        tokenScore.invoke(owner,"balanceOfAt","hx0000000000000000000000000000000000000000",id);
//    }
//
//    @Test
//    void valueAt(){
//
//    }
//
//    @Test
//    void totalSupplyAt(){
//        BigInteger id = (BigInteger) tokenScore.call("getCurrentSnapshotId2");
//
//        tokenScore.invoke(owner,"balanceOfAt",id);
//    }
//
//
//}
