import javax.swing.*;
import java.awt.event.*;

////////////////////////////////////////////////////

class SaveGifPanel extends ScrollablePanel
{
  JButton okButton;
  JButton cancelButton;
  JLabel question;
  private NetFlagPanel nfp;


  public SaveGifPanel(NetFlagPanel nfp)
  {
    this.nfp = nfp;
    this.setVisible(false);
    this.setBounds(200,80,230,100);

    question = new JLabel("Save your changes?");
    question.setBounds(20,12,200,20);
    this.add(question);

    okButton = new JButton("Ok");
    okButton.reshape(20,60,50,20);
    this.addClickable(okButton);

    cancelButton = new JButton("Cancel");
    cancelButton.reshape(100,60,90,20);
    this.addClickable(cancelButton);
  }


  public void open()
  {
    question.setText("Save your changes?");
    super.open();
  }


  public void handleClick(JComponent c, int x, int y)
  {
    if (c == okButton) {
      //nfp.saveFlagHistory(question);
      //this.setVisible(false);
    }
    else if (c == cancelButton) {
      this.getParent().setVisible(false);
    }
    //nfp.largeFlag.repaint();
  }

}


