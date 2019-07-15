import javax.swing.*;
//import java.awt.*;
import java.awt.event.*;

////////////////////////////////////////////////////

class FlagGridPanel extends ScrollablePanel
{
  JLabel question;
  private NetFlagGrid nfp;
  String flagFileID = "";


  public FlagGridPanel(NetFlagGrid nfp)
  {
    this.nfp = nfp;
    this.setVisible(true);
    this.setBounds(0,110,100,20);

    question = new JLabel("Next");
    question.setBounds(1,1,100,18);
    this.addClickable(question);
  }


  public void open()
  {
    super.open();
  }


  public void handleClick(JComponent c, int x, int y)
  {
    if (c == question) {
      // Start background thread to watch nfp progress
      startIt();
    }
  }


  public synchronized void run()
  {
    //nfp.loadFlagHistory(20);
  }


/*
  public synchronized void runOLD()
  {
    //System.out.println("START sp.run() ");
    String mesg = "";
    boolean saved = false;
    // Timeout in 100 seconds
    int counter=400;
    // start to save flag changes
    cgiDataPoster saverThread = nfp.saveFlagHistory(flagFileID);
    // check messages
    while (counter > 0 && saved == false) {
      mesg = saverThread.getMessage();
      if (mesg != null && mesg.startsWith("Saved")) {
        System.out.println("  GOT SAVED MESSAGE");
        saved = true;
        flagFileID = mesg.substring(mesg.indexOf("ID=")+3);
        //System.out.println("  GOT ID=" + flagFileID);
        mesg = mesg.substring(0,mesg.indexOf("ID="));
      }
      if (mesg != null && mesg.startsWith("Failed")) {
        System.out.println("  GOT FAILED MESSAGE");
        counter = 0;
      }
      question.setText(mesg);
      try {Thread.sleep(250);}
      catch (Exception e) {System.out.println(e);}
      counter--;
    }
    if (saved) {
      System.out.println("SAVED OKAY");
//      okButton.setVisible(false);
//      cancelButton.setVisible(false);
//      doneButton.setVisible(true);
      okButton.setBounds(420,60,50,20); // hide
      cancelButton.setBounds(500,60,90,20); // hide
      doneButton.setBounds(60,60,80,20); // show
      repaint();
      // wait 2 secs then hide panel
      // Netscape can't show the doneButton, so
      // there's no way for user to close panel.
      // just close the panel after a delay.
      try {Thread.sleep(2000);}
      catch (Exception e) {System.out.println(e);}
      this.close();
    }
  }
*/

}


