package moss.mystery.energymonitor.classifier;

public class ClassifierLibrary {

    //TODO: Finalise this
    //Return an integer representing interval length - negative short, 0 medium, positive long
    public static int classifyInterval(long length){
        if(length < 2000000){
            return -1;
        }
        if(length > 4000000){
            return 1;
        }
        return 0;
    }
}
