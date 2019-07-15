import java.awt.*;
import java.awt.event.*;

public class SaverFrame extends Frame {

  int width, height;
  SaveGifPanel sgp;

  //Construct the frame
  public SaverFrame(int w, int h) {
    boolean isApplication = true;
    width = w;
    height = h;
  }

  //Overridden so we can exit on System Close
  protected void processWindowEvent(WindowEvent e) {
    super.processWindowEvent(e);
    if(e.getID() == WindowEvent.WINDOW_CLOSING) {
      this.setVisible(false);
    }
  }

  public void init()
  {
    sgp = new SaveGifPanel(null);
    Button b = new Button("ok");
    b.setBounds(10,10,100,20);
    this.setSize(new Dimension(width, height));
    this.setLayout(null);
    this.add(sgp);
  }

}
