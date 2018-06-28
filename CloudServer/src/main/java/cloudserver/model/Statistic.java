package cloudserver.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class Statistic {
    private float mean, devstd;

    public Statistic(float mean, float devstd){
        this.mean = mean;
        this.devstd = devstd;
    }
    public Statistic(){}

}
