package moss.mystery.energymonitor.classifier;

public class ClassifiedApp {
    public String name;
    public String confidence;
    public String classification;

    public ClassifiedApp(String name, String confidence, String classification){
        this.name = name;
        this.confidence = confidence;
        this.classification = classification;
    }
}
