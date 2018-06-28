package cloudserver.utility;

import java.util.List;

public class CloudServerUtility {
    public static double getDevStd(List<Float> elements, float mean){
        double accumulator = 0;
        for(Float elem: elements){
            accumulator = accumulator+Math.pow((elem-mean),2);
        }
        return accumulator/elements.size();
    }
}
