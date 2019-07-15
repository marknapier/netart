import javax.swing.*;
import java.awt.*;


class ColorPanel extends ScrollablePanel
{
  public NetFlagPanel nfp;
  public String[] colors = null;
  private boolean initialized = false;
  JLabel[] names;


  public ColorPanel(NetFlagPanel nfp, String[] colors)
  {
    int y=0;
    JLabel l;

    this.nfp = nfp;
    this.colors = colors;

    setBounds(180,50,130,400);
    setVisible(false);

    // init these before loop below
    numItems = colors.length;
    rowsPerPage = 13;
    names = new JLabel[rowsPerPage];

    // make blank rows of names
    for (y=0; y < rowsPerPage; y++) {
      // add country name label
      l = new JLabel( "nada" );
      l.setFont(new Font("Arial",Font.PLAIN,10));
      l.setBounds(5,(30*y)+6,100,20);
      names[y] = l;
      this.addClickable(l);
    }

    // Add a scrollbar
    addScrollbar();

    // show first page of list
    //fillPage();
  }


  public void open()
  {
    fillPage();
    refreshScroller();
    this.setVisible(true);
    repaint();
  }


  public void shiftTo(int row)
  {
    // show a page of keywords
    startShowRow = row;
    fillPage();
    repaint();
  }


  // show one page of keyword list
  //
  public void fillPage()
  {
    int y=0, i=0;
    Color bgcolor = null;
    for (i=startShowRow; i<numItems && y<rowsPerPage; i++,y++) {
      // Fill page with keywords
      bgcolor = getRGB(colors[i]);
      names[y].setOpaque(true);
      names[y].setBackground(bgcolor);
      names[y].setForeground(bgcolor);  // to hide text
      names[y].setText(colors[i]);   // put rgb vals into label
      names[y].setVisible(true);
    }
    // fill in trailing blank rows
    for (; y < rowsPerPage; y++) {
      names[y].setVisible(false);
    }
  }


  public Color getRGB(String colorrgb)
  {
    int r, g, b;
    String[] rgbtokens = new String[3];
        if (FlagDescription.parseRecord(colorrgb,rgbtokens,",") == false) {
          System.out.println("ColorPanel: Can't read RGB values for " + colorrgb);
        }
        // grab id, x,y and char
        try {
             r = Integer.parseInt(rgbtokens[0]);
             g = Integer.parseInt(rgbtokens[1]);
             b = Integer.parseInt(rgbtokens[2]);
        }
        catch(Exception e) {
             System.out.println("ColorPanel getRGB():"+ e);
             return null;
        }
        return new Color(r,g,b);
  }


  public void handleClick(JComponent c, int x, int y)
  {
    String objClass = c.getClass().getName();
    if (objClass.indexOf("JLabel") >= 0) {
      JLabel l = (JLabel)c;
      int[] flagIds = FlagDescription.findColor(l.getText());
      nfp.openList(flagIds);
    }
  }
}


