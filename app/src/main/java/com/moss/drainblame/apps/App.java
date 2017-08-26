package com.moss.drainblame.apps;

//Information on process state within a battery interval

public class App {
    public final String name;               //Packagename, if known, else process name
    public final boolean unknownPackage;    //True if no app could be associated with this process
    public long ticks;

    public App(String name, boolean unknownPackage){
        this(name, 0, unknownPackage);
    }

    public App(String name, long ticks, boolean unknownPackage){
        this.name = name;
        this.ticks = ticks;
        this.unknownPackage = unknownPackage;
    }

    public void addTicks(long ticks){
        this.ticks += ticks;
    }
}