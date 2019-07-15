import javax.swing.*;


class KeywordPanel extends ScrollablePanel
{
  public NetFlagPanel nfp;
  public String[] keywords = null;
  private boolean initialized = false;
  JLabel[] names;


  public KeywordPanel(NetFlagPanel nfp, String[] keywords)
  {
    int y=0;
    JLabel l;

    this.nfp = nfp;
    this.keywords = keywords;

    setBounds(180,50,130,400);
    setVisible(false);

    // init list parameters
    startShowRow = 0;
    numItems = keywords.length;
    rowsPerPage = 17;

    names = new JLabel[rowsPerPage];

    // make blank rows of names
    for (y=0; y < rowsPerPage; y++) {
      // add country name label
      l = new JLabel( "nada" );
      l.setBounds(5,(23*y)+6,100,20);
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
    for (i=startShowRow; i<numItems && y<rowsPerPage; i++,y++) {
      // Fill page with keywords
      names[y].setText(keywords[i]);
      names[y].setVisible(true);
    }
    // fill in trailing blank rows
    for (; y < rowsPerPage; y++) {
      names[y].setVisible(false);
    }
  }


  public void handleClick(JComponent c, int x, int y)
  {
    String objClass = c.getClass().getName();
    if (objClass.indexOf("JLabel") >= 0) {
      JLabel l = (JLabel)c;
      int[] flagIds = FlagDescription.findKeyword(l.getText());
      nfp.openList(flagIds);
    }
  }

}
