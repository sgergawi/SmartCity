package cloudserver.model;


import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Node {
    int id, sensorsPort, otherNodesPort, xPos, yPos;
    String selfIp;

}
