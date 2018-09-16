package edgenodes.model;

public class Semaphore {
    public synchronized void startCritical(int threadsNumber){
        try{
            //Wait for at most 10 sec or till someone notifies me
            System.out.println("Aspetto nella zona critica: "+threadsNumber);
            this.wait(10000*(threadsNumber==0?1:threadsNumber));
            System.out.println(threadsNumber);
        } catch(InterruptedException ie)
            {
                ie.printStackTrace();
            }
    }

    public synchronized void exit(){
        System.out.println("Risveglio");
        this.notify();
    }
}
