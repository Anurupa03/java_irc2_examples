package com.iconloop.score.example;


import com.iconloop.score.token.irc2.IRC2Basic;
import score.Context;
import score.annotation.External;



public class IRC2Pausable extends IRC2Basic {

    public IRC2Pausable(String _name, String _symbol, int _decimals) {
        super(_name, _symbol, _decimals);
    }


    @External
    public boolean pause(){
        if (Context.getCaller() != Context.getOwner()){
            System.out.println("Only owners can pause");}
        return true;
    }

    @External
    public boolean unpause(){
        if (Context.getCaller() != Context.getOwner()){
            System.out.println("Only owners can unpause");}
        return false;

    }

    @External
    public boolean pauseStatus(){
        return pause();
    }





//make transfer method;
//add checks related to pause
//super garera parent method lai call garne
}
