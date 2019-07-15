//////////
class FlagAnimator extends Thread
{
    Flag flag;
    int counter=800;
    public FlagAnimator( Flag flag ) {
      this.flag = flag;
    }
    public synchronized void run() {
       System.out.println("START Animation Thread ");
       while (counter > 0) {
           try {
               this.wait(10);  //100
           }
           catch (Exception e) {
               System.out.println(e);
           }
//           flag.drawElements();
           flag.repaint();
           //counter--;
       }
       System.out.println("EXIT Animation Thread ");
    }
    public void stopIt() {
      counter=0;
    }
}
