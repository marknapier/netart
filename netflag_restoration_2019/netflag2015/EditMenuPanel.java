import javax.swing.*;
import java.awt.event.*;

class EditMenuPanel extends ScrollablePanel
{
  private NetFlagPanel nfp;
  JLabel countryButton = new JLabel("country");
  JLabel shapeButton = new JLabel("shape");
  JLabel meaningButton = new JLabel("meaning");
  JLabel colorButton = new JLabel("color");


  public EditMenuPanel(NetFlagPanel nfp, JApplet parapp)
  {
    this.nfp = nfp;
    this.setBounds(50,50,300,50);
    this.setVisible(false);

    countryButton.reshape(25,14,50,20);
    this.addClickable(countryButton);

    shapeButton.reshape(95,14,50,20);
    this.addClickable(shapeButton);

    meaningButton.reshape(160,14,50,20);
    this.addClickable(meaningButton);

    colorButton.reshape(240,14,50,20);
    this.addClickable(colorButton);
  }


  public void handleClick(JComponent c, int x, int y)
  {
    if (c == countryButton) {
      nfp.openList();
    }
    else if (c == meaningButton) {
      nfp.openKeywords();
    }
    else if (c == shapeButton) {
      nfp.openShapes();
    }
    else if (c == colorButton) {
      nfp.openColors();
    }
    else {
      System.out.println("ElementMenu click on nada");
    }
  }

  // Process mouse over/exit on buttons (edit, done)

  public void handleMouseEnter(JComponent c)
  {
    if (c == countryButton) {
      nfp.setStatus("List flags by country name");
    }
    else if (c == shapeButton) {
      nfp.setStatus("List flags by shapes");
    }
    else if (c == meaningButton) {
      nfp.setStatus("List flags by meaning");
    }
    else if (c == colorButton) {
      nfp.setStatus("List flags by colors");
    }
  }


  public void handleMouseExit(JComponent c)
  {
    nfp.setStatus("");
  }

}
