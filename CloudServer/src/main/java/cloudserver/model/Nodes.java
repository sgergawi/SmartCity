package cloudserver.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;
import java.util.Vector;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class Nodes{

    private List<Node> nodes;

    public Nodes(List<Node> nodes){
        this.nodes = nodes;
    }
    public Nodes(){this.nodes = new Vector<>();}
    public List<Node> getNodes(){
        return nodes;
    }
    public Nodes setNodes(List<Node> nodes){
        this.nodes = nodes;
        return this;
    }

}
