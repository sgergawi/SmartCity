package cloudserver.model;


import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class Node {
    private int id, sensorsPort, otherNodesPort, xPos, yPos;
    private String selfIp;

    public Node(int id, int sensorsPort, int otherNodesPort, int xPos, int yPos, String selfIp){
        this.id=id;
        this.sensorsPort=sensorsPort;
        this.otherNodesPort=otherNodesPort;
        this.xPos=xPos;
        this.yPos=yPos;
        this.selfIp=selfIp;

    }
    public int getId() {
        return id;
    }


    public int getSensorsPort() {
        return sensorsPort;
    }


    public int getOtherNodesPort() {
        return otherNodesPort;
    }


    public int getxPos() {
        return xPos;
    }


    public int getyPos() {
        return yPos;
    }

    public String getSelfIp() {
        return selfIp;
    }


}
