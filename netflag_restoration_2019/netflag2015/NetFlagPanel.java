import javax.swing.*;
import java.applet.AppletContext;
import java.awt.*;
import java.io.*; // for save/load flag history
import java.net.*;        // for URL


public class NetFlagPanel extends ScrollablePanel
{
  boolean TESTING = true; // !!! changed to test
  public Flag largeFlag;
  protected JLabel statusLabel;
  public FlagBuilder FD;

  private int width, height;
  Thumbnail bttnEdit;
  Thumbnail bttnDone;
  Thumbnail bttnNetflag;
  JLabel lblNetflag;
  private ThumbnailPanel flagsList;
  private KeywordPanel keywordList;
  private FlagInfoPanel flagInfoPanel;
  private ElementPanel elementPanel;
  private ShapePanel shapePanel;
  private ColorPanel colorList;
  private EditMenuPanel editMenu;
  private SavePanel saveDialog;
  public JApplet parapp;
  public URL appletBase;
  public URL flagdataURL;
  public String domain;
  int numHistoryItems = 30;  // number of flag elements in history (see setHistoryMode())
  boolean historyMode = false;


  public NetFlagPanel(JApplet parapp) {
      this.parapp = parapp;
  }


  public NetFlagPanel(JApplet parentApplet, int w, int h) {
      // make panel as big as applet
      width = w;
      height = h;
      setOpaque(true);
      setBackground(Color.darkGray);
      setBounds(0,0,w,h);
      setBorder(false);
      // setup urls relative to applet codebase
      parapp = parentApplet;
      appletBase = parapp.getCodeBase();
      domain = appletBase.getHost();
      flagdataURL = makeURL("nf_shapedata.gzip");
  }


  /**
   * Make a url pointing to the given filename, relative to the applet code base.
   * @param filename
   * @return a url
   */
  public URL makeURL(String filename) {
      URL appletBase = parapp.getCodeBase();
      URL u = null;
      try {
          u = new URL(appletBase, filename);
      }
      catch (Exception e) {
          System.out.println("NetFlagPanel.makeURL(): error forming URL to " +filename+ ": " + e);
      }
      return u;
  }


  public void setHistoryMode(boolean bool)
  {
    historyMode = bool;
    numHistoryItems = (historyMode)? 150 : 30;
  }


  public void init()
  {
    Font toolbarFont = new Font("Arial", Font.PLAIN, 12);
    Font buttonFont = new Font("Arial", Font.BOLD, 30);

    // status messages
    statusLabel = new JLabel();
    statusLabel.setBounds(590,12,136,14);
    statusLabel.setForeground(Color.yellow);
    statusLabel.setFont(new Font("Arial", Font.PLAIN, 10));
    statusLabel.setText("Loading flag...");
    this.add(statusLabel);

    editMenu = new EditMenuPanel(this,parapp);
    this.add(editMenu);

    saveDialog = new SavePanel(this);
    this.add(saveDialog);

    elementPanel = new ElementPanel(this,parapp);
    this.add(elementPanel);

    flagInfoPanel = new FlagInfoPanel(this,parapp);
    this.add(flagInfoPanel);

    flagsList = new ThumbnailPanel(this,FlagDescription.countryNames);
    this.add(flagsList);

    keywordList = new KeywordPanel(this,FlagDescription.keywords);
    this.add(keywordList);

    shapePanel = new ShapePanel(this,parapp);
    this.add(shapePanel);

    colorList = new ColorPanel(this,FlagDescription.colors);
    this.add(colorList);

    // Make flag designer 'factory'
    // Has to be same width height as flag
    FD = new FlagBuilder(0,0,700,440,parapp); //

    // Make flag same width height as flagBuilder
    largeFlag = new Flag(/*this*/);
    largeFlag.setBounds(25,32,700,440);
    largeFlag.setCursor(new Cursor(Cursor.HAND_CURSOR));
    largeFlag.addMouseMotionListener(new MListener(largeFlag,this));
    largeFlag.addMouseListener(new NetFlagListener(largeFlag,this));
    this.add(largeFlag);

    // Make a scrollbar for flag history
    numItems = numHistoryItems;
    rowsPerPage = largeFlag.rowsPerPage;
    this.addScrollbar();
    scroller.setBounds(730,32,16,440);
    if (!historyMode) {
      scroller.setVisible(false);
    }

    // buttons at top of flag
    bttnNetflag = new Thumbnail(null,360,8,18,18);
    bttnNetflag.setImage(null);
    this.addClickable(bttnNetflag);

    // add plus and asterisk buttons
    bttnEdit = new Thumbnail(null,50,8,18,18);
    bttnEdit.setImage(null);
    bttnEdit.setVisible(!historyMode);
    this.addClickable(bttnEdit);

    bttnDone = new Thumbnail(null,150,8,18,18);
    bttnDone.setImage(null);
    bttnDone.setVisible(false);
    this.addClickable(bttnDone);

    // listen for Escape key
    addKeyListener(new KeyPressListener(this));
    requestFocus();  // keyboard focus

    // Start background process to load history and flag data
    startIt();
  }


  public synchronized void run()
  {
    System.out.println("Load toolbar images");
    // load interface images (plus sign and asterisk)
    Image tmpi = parapp.getImage(parapp.getDocumentBase(),"arrow_down.gif");
    if (FD.waitForImage(tmpi,this)) {
      bttnEdit.setImage(tmpi);
      bttnEdit.repaint();
    }
    tmpi = parapp.getImage(parapp.getDocumentBase(),"red_dot.gif");
    if (FD.waitForImage(tmpi,this)) {
      bttnDone.setImage(tmpi);
      bttnDone.repaint();
    }
    tmpi = parapp.getImage(parapp.getDocumentBase(),"arrow_up.gif");
    if (FD.waitForImage(tmpi,this)) {
      bttnNetflag.setImage(tmpi);
      bttnNetflag.repaint();
    }
    // load last 100 changes to flag
    // Comment out for testElement.html version of netflag
    loadflagHistory();
    // track user changes from this point on
    largeFlag.setStartMods();
  }


  public void setStatus(String t)
  {
    statusLabel.setText(t);
    repaint();
  }


  // Process mouse over/exit on buttons (edit, done)

  public void handleMouseEnter(JComponent c)
  {
    if (c == bttnEdit) {
      this.setStatus("Add to the flag");
    }
    else if (c == bttnDone) {
      this.setStatus("Save your changes");
    }
    else if (c == bttnNetflag || c == lblNetflag) {
      this.setStatus("net.flag options");
    }
    else if (c == scroller) {
      if (scroller.isEnabled()) {
        this.setStatus("View recent changes");
      } else {
        this.setStatus("Save your changes to enable");
      }
    }
  }

  public void handleMouseExit(JComponent c)
  {
    this.setStatus("");
  }

  // Process clicks on buttons (edit, done)

  public void handleClick(JComponent c, int x, int y)
  {
    // have to get focus back for Escape key to work
    // seems like any click on an awt.Button takes focus
    // away from this panel
    requestFocus();  // keyboard focus
    //
    if (c == bttnEdit) {
      openEditMenu();
    }
    else if (c == bttnNetflag || c == lblNetflag) {
        // open a menu page in the parent folder
      AppletContext ac = this.parapp.getAppletContext();
      try {
        ac.showDocument( new URL(this.appletBase,"menu.html"), "_parent" );
      }
      catch (Exception ex) {
        System.out.println("Error opening menu.html");
      }
    }
    else if (c == bttnDone) {
      openSaveDialog();
    }
    else {
      closePanels();
    }
  }


  // respond to a keypress
  public void handleKey(String keyHit)
  {
    if (keyHit.equals("esc")) {
      closePanels();
    }
  }

  public void closePanels()
  {
      hide(colorList);
      hide(shapePanel);
      hide(flagsList);
      hide(keywordList);
      hide(elementPanel);
      hide(flagInfoPanel);
      hide(editMenu);
  }

  public void shiftTo(int row)
  {
    // move back in flag history
    closePanels();
    largeFlag.setstartShowRow(largeFlag.getNumElements() - (row + largeFlag.rowsPerPage));
    System.out.println("STARTSHOWROW=" + largeFlag.startShowRow + " visible=" + this.scroller.getVisibleAmount() + " min=" + this.scroller.getMinimum() + " max=" + this.scroller.getMaximum());
    largeFlag.repaint();
  }


  // Start thread to save flag changes to server.
  // First time we save fileID is null so we save changes
  // with no name.  For subsequent saves fileID has a value,
  // then we pass that value to CGI to save to same file.

  public cgiDataPoster saveFlagHistory(String fileID, String domainStr, String commentStr)
  {
      cgiDataPoster storage = new cgiDataPoster();
      storage.setURL("http://" +domain+ "/cgi-bin/nf_history.pl");
      System.out.println("SAVING FLAG ROWS ID=" + fileID);
      if (domainStr == null) domainStr = "";
      if (commentStr == null) commentStr = "";

      System.out.println("NFP.SAVE FLAG: "
                          + "filename=" +fileID+ "&"
                          + "domain=" +domainStr+ "&"
                          + "comment=" +commentStr+ "&");

      storage.setPostData("filename=" +fileID+ "&"
                          + "domain=" +domainStr+ "&"
                          + "comment=" +commentStr+ "&"
                          + largeFlag.getChanges());

      storage.start();
      // reset edit button and history scrollbar
      bttnDone.setVisible(false);
      scroller.setEnabled(true);
      scroller.setVisible(true);
      return storage;
  }


  // load meanings for the flag elements of a country

  public void loadflagMeanings(String abbrev)
  {
    int flagId = FlagDescription.getCountryId(abbrev);
    if (FlagDescription.flags[flagId].meaningsLoaded == false) {
      String[] meaningFields = new String[3];
      cgiDataPoster storage = new cgiDataPoster();
      String[] meanings;
      if (TESTING) {
          meanings = storage.readURL("http://potatoland.org/cgi-bin/nf_getflagdata.pl?country=" + abbrev);
      }
      else {
          meanings = storage.readURL("http://" + domain + "/cgi-bin/nf_getflagdata.pl?country=" + abbrev);
      }
      //
      //System.out.println("GOT MEANINGS:");
      for (int i=0; i < meanings.length; i++) {
        // field 0: abbrev
        // field 1: element name
        // field 2: meaning text
        //System.out.println("\t" + meanings[i]);
        FlagDescription.parseRecord(meanings[i],meaningFields,"\t");
        FlagDescription.setMeaning(flagId,meaningFields[1],meaningFields[2]);
      }
      FlagDescription.flags[flagId].meaningsLoaded = true;
    }
  }


  public boolean loadflagHistory()
  {
      System.out.println("LOAD DATABASE");
      setStatus("Load database");

      // Load all essential flag data at beginnning
      FlagDescription.preLoadFlags(flagdataURL);

      System.out.println("LOAD HISTORY");
      setStatus("Load history");

      // Load the last 150 rows of net.flag changes
      cgiDataPoster storage = new cgiDataPoster();
      String[] flagHistory;

      // TEST: load history from local file
      if (TESTING) {
        System.out.println("TESTING TESTING TESTING TESTING!!!!!!!!");
        //flagHistory = storage.readURL("http://" +domain+ "/cgi-bin/nf_history.pl?gettest");
        flagHistory = storage.readURL(makeURL("flaghistory.txt"));
      }
      else {
        //System.out.println("LOADING FROM HOST URL: " + domain);
        flagHistory = storage.readURL("http://" +domain+ "/cgi-bin/nf_history.pl?get="+numHistoryItems);
      }

      //System.out.println("LOADED HISTORY:");
      setStatus("Show history");

      // Parse the rows of history
      int line=0;
      String[] tokens = new String[10];
      int x=0, y=0;

      for (line=0; line < numHistoryItems && flagHistory[line] != null; line++) {
// DEBUG flag data
//        System.out.println("PROCESS ElEMENT: " + flagHistory[line]);
        if (FlagDescription.parseRecord(flagHistory[line],tokens,",")) {
          try {
             x  = Integer.parseInt(tokens[2]);
             y  = Integer.parseInt(tokens[3]);
          }
          catch(Exception e) {
             System.out.println("loadflagHistory():"+ e);
             System.out.println("line: x="+tokens[2] + " y="+tokens[3]);
          }
          Element F = FD.makeFlagElement(tokens[0],tokens[1]);
          if (F != null) {
            F.moveTo(x,y);
            largeFlag.add(F);
            // KLUDGE: set the flag startShowRow to -1
            // When the flag repaints it will set the
            // visible page to the last page of flag elements
            // added.  Need to do this so flag repaints
            // as it loads.
            largeFlag.startShowRow = -1;
            if (historyMode) {
              largeFlag.repaint();
            }
          }
          // wait a bit so repaint can happen
          try { Thread.sleep(30); }
          catch (Exception e) {}
        }
      }

      setStatus("net.flag ready");

      // sync scrollbar with flag
      refreshScroller();
      largeFlag.repaint();

      return true;
  }


  public void openSaveDialog()
  {
    if (saveDialog.isVisible()) {
      saveDialog.close();
    }
    else {
      closePanels();
      saveDialog.open();
    }
  }

  public void openEditMenu()
  {
    if (editMenu.isVisible()) {
      editMenu.setVisible(false);
    }
    else {
      closePanels();
      editMenu.setVisible(true);
    }
  }

  public void openKeywords()
  {
    editMenu.setVisible(false);
    keywordList.open();
  }

  public void openShapes()
  {
    editMenu.setVisible(false);
    shapePanel.open();
  }

  public void openColors()
  {
    editMenu.setVisible(false);
    colorList.open();
  }

  public void openList()
  {
    editMenu.setVisible(false);
    flagsList.setVisible(true);
    flagsList.open();
  }

  public void openList(int[] flagIds)
  {
    editMenu.setVisible(false);
    flagsList.setVisible(true);
    flagsList.open(flagIds);
  }

  public void openFlagInfo(String country)
  {
    //hide(flagsList);
    hide(keywordList);
    hide(shapePanel);
    hide(colorList);
    flagInfoPanel.open(country);
  }

  public void openElementPanel(Element f, int x, int y)
  {
    closePanels();
    elementPanel.open(f, x, y);
  }

  public void refreshScroller()
  {
    // jump to last page of flag history
    // and adjust scrollbar to match
    scroller.setMaximum(largeFlag.getNumElements());
    // reverse direction of scrollbar (top is most recent element)
    scroller.setValue(0);
    largeFlag.setstartShowRow( largeFlag.getNumElements()-largeFlag.rowsPerPage);

  }

  public Element addFlagElement(String flagName, String elementName)
  {
    Element f = FD.makeFlagElement(flagName,elementName);
    addFlagElement(f);
    return f;
  }

  public void addFlagElement(Element f)
  {
    //System.out.println("NetFlagPanel.addflagelement():" + f.myName + " " + f.element);
    if (largeFlag.add(f) == true) {
      hide(flagsList);
      bttnDone.setVisible(true);
      scroller.setEnabled(false);
      refreshScroller();
      largeFlag.repaint();
    }
  }

  public void hide(JComponent somePanel)
  {
    somePanel.setVisible(false);
  }

  public void showStatus(String s)
  {
    System.out.println(s);
  }

  public void update___(Graphics g)
  {
    paint(g);
  }
}

