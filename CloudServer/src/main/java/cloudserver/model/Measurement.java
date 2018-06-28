package cloudserver.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class Measurement {
    private float value;
    private int timestamp;

    public Measurement(float value, int timestamp){
        this.value = value;
        this.timestamp = timestamp;
    }

    public Measurement(){}
}
