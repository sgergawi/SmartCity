package edgenodes.utility;

import cloudserver.model.SmartCity;

import java.util.Calendar;
import java.util.Comparator;

public class Utility {

    public static Comparator<SmartCity.NodeMeasurement> getComparator(){
        return new Comparator<SmartCity.NodeMeasurement>() {
            @Override
            public int compare(SmartCity.NodeMeasurement o1, SmartCity.NodeMeasurement o2) {
                Long firstTimestamp =o1.getTimestamp();
                Long secondTimestamp = o2.getTimestamp();
                return firstTimestamp.compareTo(secondTimestamp);
            }
        };
    }

    public static Long generateTimestamp() {
        Calendar c = Calendar.getInstance();
        return c.getTimeInMillis();
    }
}
