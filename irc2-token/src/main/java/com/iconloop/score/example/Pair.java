package com.iconloop.score.example;

// tuple designed for valueAt function of snapshot
public class Pair<A,B> {
    public final A a;
    public final B b;

    public Pair(A a , B b){
        this.a = a;
        this.b =b;
    }

    public A getFirst(){
        return this.a;
    }

    public B getSecond(){
        return this.b;
    }
}
