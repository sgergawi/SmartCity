package cloudserver.model;


import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement
public class GroupMeasurements {
    List<Measurement> localMeasurements;
    float globalMean;



}
