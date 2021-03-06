package com.moss.drainblame.classifier;

public class ClassifiedApp {
    public final String name;
    public final int confidence;
    public final int classification;
    public final boolean unknownPackage;//True if this is a process that can't be attributed to an app
    public final boolean network;       //True if app is high when network is high

    public ClassifiedApp(String name, int classification, int confidence, boolean unknownPackage, boolean network){
        this.name = name;
        this.confidence = confidence;
        this.classification = classification;
        this.unknownPackage = unknownPackage;
        this.network = network;
    }
}