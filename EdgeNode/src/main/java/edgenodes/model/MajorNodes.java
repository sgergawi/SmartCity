package edgenodes.model;

import cloudserver.model.SmartCity;

import java.util.List;
import java.util.Vector;

public class MajorNodes {
    private List<SmartCity.Node> majorThanMe;

    private static MajorNodes majors;

    public synchronized static MajorNodes getInstance(){
        if(majors==null){
            majors = new MajorNodes();
        }
        return majors;
    }
    private MajorNodes(){majorThanMe=new Vector<>();}

    public synchronized void addMajorThanMe(SmartCity.Node node){
        this.majorThanMe.add(node);
    }

    @Override
    public String toString(){
        return this.majorThanMe!=null?this.majorThanMe.toString():null;
    }

    public List<SmartCity.Node> getMajorThanMe(){
        return this.majorThanMe;
    }


}
