import javax.swing.*;
import java.applet.*;
import java.awt.*;
import java.net.*; // for URL

//============================================
// Display flags in a grid
// click a flag to open it in editor
//============================================

public class NetFlagGridPanel extends NetFlagPanel {
    boolean TESTING = false;
    int numHistoryItems = 150; // number of flag elements in history
    int pWidth;
    int pHeight;
    // Dimensions of small flags
    int flagW = 140;   //111;
    int flagH = 88;    //70;
    int flagsPerPage = 20;
    int startRow = 0;
    Flag[] flags = new Flag[flagsPerPage];
    JLabel[] labelsDate = new JLabel[flagsPerPage];
    JLabel[] labelsDomain = new JLabel[flagsPerPage];
    FlagBuilder fb;
    Dimension D;
    String domain;
    // link to next/prev page
    JLabel nextLink;
    JLabel prevLink;
    Font smallFont = new Font("Arial", Font.PLAIN, 9);
    //
    FlagInfoPanel flagInfoPanel;
    Thumbnail bttnPrev;
    Thumbnail bttnNext;
    Thumbnail bttnNetflag;


    public NetFlagGridPanel(NetFlagGrid app, int w, int h) {
        super(app, 930, 630);
        //
        this.setBackground(Color.darkGray);
        this.setBounds(0, 0, 930, 630);
        this.setVisible(true);
    }


    public void init() {
        this.setLayout(null);
        this.setBackground(Color.darkGray);
        D = this.getSize();
        //
        URL appletBase = parapp.getCodeBase();
        URL flagdataURL = null;
        try {
            flagdataURL = new URL(appletBase, "nf_shapedata.gzip");
        }
        catch (Exception e) {
            System.out.println("NetFlagLite.init(): error forming flagdata URL: " + e);
        }

        FlagDescription.preLoadFlags(flagdataURL);
        domain = appletBase.getHost();

        // create a flag factory for small flag
        fb = new FlagBuilder(0, 0, flagW, flagH, parapp);

        // To show flag details onclick
        flagInfoPanel = new FlagInfoPanel(this, parapp);
        this.add(flagInfoPanel);

        // status messages
        statusLabel = new JLabel();
        statusLabel.setBounds(590, 12, 136, 14);
        statusLabel.setForeground(Color.yellow);
        statusLabel.setFont(new Font("Arial", Font.PLAIN, 10));
        statusLabel.setText("Loading flag...");
        this.add(statusLabel);

        // add prev and next buttons (will load images during run()
        bttnPrev = new Thumbnail(null, 20, 2, 18, 18);
        bttnPrev.setImage(null);
        this.addClickable(bttnPrev);

        bttnNext = new Thumbnail(null, 70, 2, 18, 18);
        bttnNext.setImage(null);
        this.addClickable(bttnNext);

        bttnNetflag = new Thumbnail(null, 360, 2, 18, 18);
        bttnNetflag.setImage(null);
        this.addClickable(bttnNetflag);

        // create 4x5 grid of flags
        for (int n = 0; n < flags.length; n++) {
            // flag
            flags[n] = new Flag();
            flags[n].bgColor = Color.darkGray;
            flags[n].setBounds( (n % 5) * 195, ( (n / 5) * 155) + 42, flagW, flagH);
            flags[n].rowsPerPage = 30;
            this.addClickable(flags[n]);
            // date text
            labelsDate[n] = new JLabel("");
            labelsDate[n].setBounds( (n % 5) * 195, ( (n / 5) * 155) + 136, flagW, 16);
            labelsDate[n].setFont(smallFont);
            labelsDate[n].setForeground(Color.lightGray.darker());
            this.add(labelsDate[n]);
            // domain
            labelsDomain[n] = new JLabel("");
            labelsDomain[n].setBounds( (n % 5) * 195, ( (n / 5) * 155) + 152, flagW, 16);
            labelsDomain[n].setFont(smallFont);
            labelsDomain[n].setForeground(Color.lightGray.darker());
            this.add(labelsDomain[n]);
        }

        // Start background process to load small flags
        startIt();
    }


    /**
     * Make a url pointing to the given filename, relative to the applet code base.
     * @param filename
     * @return a url
     */
    public URL makeURL(String filename) {
        URL u = null;
        try {
            u = new URL(appletBase, filename);
        }
        catch (Exception e) {
            System.out.println("NetFlagLite.makeURL(): error forming URL to " +filename+ ": " + e);
        }
        return u;
    }


    public boolean loadFlagHistory(int row) {
        System.out.println("Load Flag History startrow=" + row);
        // clear all flags
        for (int i = 0; i < flagsPerPage; i++) {
            labelsDate[i].setText("");
            labelsDomain[i].setText("");
            flags[i].reset();
            flags[i].repaint();
        }

        // Load the last 100 rows of net.flag changes
        cgiDataPoster storage = new cgiDataPoster();
        String[] flagHistory; // = storage.readURL("http://" + domain + "/cgi-bin/nf_listflags.pl?startrow=" + row);
        String[] tokens = new String[10];
        int line = 0;
        int x = 0, y = 0;
        float xPercent = 0F, yPercent = 0F;
        int whichFlag = 0;
        Flag flag = flags[0];

        if (TESTING) {
            flagHistory = storage.readURL( makeURL("nf_listflags_test.txt") );
        }
        else {
            flagHistory = storage.readURL("http://" + domain + "/cgi-bin/nf_listflags.pl?startrow=" + row);
        }

        // show flags
        for (line = 0; line < flagHistory.length && flagHistory[line] != null; line++) {
            if (flagHistory[line].startsWith("FLAG")) {
                String flagDate = getDate(flagHistory[line]);
                String flagDomain = getDomain(flagHistory[line]);
                String flagComment = getComment(flagHistory[line]);
                // starting a new flag
                // first line has flag date-time
                //System.out.println("GOT FLAG: " + whichFlag + ":" + flagHistory[line]);
                flag.repaint();
                flag = flags[whichFlag];
                labelsDate[whichFlag].setText(flagDate);
                labelsDomain[whichFlag].setText(flagDomain);
                flag.country = flagDomain;
                flag.comment = flagComment;
                whichFlag++;
            }
            else {
                //System.out.println("PROCESS ElEMENT: " + flagHistory[line]);
                if (FlagDescription.parseRecord(flagHistory[line], tokens, ",")) {
                    try {
                        x = Integer.parseInt(tokens[2]);
                        y = Integer.parseInt(tokens[3]);
                    }
                    catch (Exception e) {
                        System.out.println("loadflagHistory():" + e);
                        System.out.println("line: x=" + tokens[2] + " y=" + tokens[3]);
                    }
                    Element el = fb.makeFlagElement(tokens[0], tokens[1]);
                    if (el != null) {
                        // KLUDGE:
                        // Log file stores x,y as absolute pixel positions
                        // in full size flag (700x440).
                        // Have to scale these vals to smaller size flag
                        // so set x,y in proportion to original flag size
                        xPercent = ( (float) x / (float) 700);
                        yPercent = ( (float) y / (float) 440);
                        x = (int) (xPercent * (float) (flagW));
                        y = (int) (yPercent * (float) (flagH));

                        // Store ((float)x/(float)700) into element
                        // this is the hi-res position of element.  Once we
                        // multiply by small size flagW and flagH, positions
                        // lose precision, then don't scale well back to larger
                        // flag. These are not the standard x,y position, so we
                        // can't go to ElementDescription to get them.
                        el.setXYpercents(xPercent, yPercent);
                        el.moveTo(x, y);
                        flag.add(el);
                    }
                }
            }
        }
        // repaint last flag
        flag.repaint();
        repaint();
        return true;
    }


    public String getDate(String flagInfo) {
        String[] monthNames = {
            "", "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
        String[] tokens = new String[10];
        String retDate = "";
        if (FlagDescription.parseRecord(flagInfo, tokens, "|")) {
            String sFlagNum = tokens[1];
            String sDate = tokens[2];
            // Make a date string from date-time
            String year;
            int month;
            int day;
            String hour;
            String minute;
            try {
                month = Integer.parseInt(sDate.substring(2, 4));
                day = Integer.parseInt(sDate.substring(4, 6));
                year = sDate.substring(0, 2);
                hour = sDate.substring(7, 9);
                minute = sDate.substring(9, 11);
                retDate = "" + day + "-" + monthNames[month] + "-" + year + " " + hour + ":" + minute;
            }
            catch (Exception e) {
                System.out.println(e);
            }
        }
        return retDate;
    }


    public String getDomain(String flagInfo) {
        String[] tokens = new String[10];
        String retDomain = "";
        if (FlagDescription.parseRecord(flagInfo, tokens, "|")) {
            retDomain = tokens[4];
            if (retDomain == null) {
                retDomain = "";
            }
            else if (retDomain.equals("nodomain.com")) {
                retDomain = "";
            }
        }
        return retDomain;
    }


    public String getComment(String flagInfo) {
        String[] tokens = new String[10];
        String retComment = "";
        if (FlagDescription.parseRecord(flagInfo, tokens, "|")) {
            retComment = tokens[5];
            if (retComment == null) {
                retComment = "";
            }
            else if (retComment.equals("(no comment)")) {
                retComment = "";
            }
        }
        return retComment;
    }

    // Process mouse over/exit on buttons (prev, next)

    public void handleMouseEnter(JComponent c) {
        if (c == bttnPrev) {
            this.setStatus("Previous page of flags");
        }
        else if (c == bttnNext) {
            this.setStatus("Next page of flags");
        }
        else if (c == bttnNetflag || c == lblNetflag) {
            this.setStatus("net.flag options");
        }
        else if (c.getClass().getName().indexOf("Flag") >= 0) {
            this.setStatus("Click to view flag");
        }
    }


    public void handleMouseExit(JComponent c) {
        this.setStatus("");
    }


    public void handleClick(JComponent c, int x, int y) {
        String objClass = c.getClass().getName();
        int flagsCount = 500; // !!!! get this from server
        if (objClass.indexOf("Flag") >= 0) {
            Flag f = (Flag) c;
            //System.out.println("Clicked on flag ");
            flagInfoPanel.openCustom(f, flagW, flagH);
        }
        else if (c == bttnNext) {
            if (startRow + flagsPerPage < flagsCount) {
                startRow += flagsPerPage;
                startIt();
            }
        }
        else if (c == bttnPrev) {
            if (startRow - flagsPerPage >= 0) {
                startRow -= flagsPerPage;
                startIt();
            }
        }
        else if (c == bttnNetflag || c == lblNetflag) {
            AppletContext ac = this.parapp.getAppletContext();
            try {
                ac.showDocument(new URL(this.appletBase, "menu.html"), "_parent");
            }
            catch (Exception ex) {
                System.out.println("Error opening menu.html");
            }
        }
    }


    public synchronized void run() {
        System.out.println("Run()");
        // load interface images (plus sign and asterisk)
        setStatus("Load images...");
        System.out.println("Run() get image");
        Image tmpi = parapp.getImage(parapp.getDocumentBase(), "arrow_left.gif");
        if (FD.waitForImage(tmpi, this)) {
            bttnPrev.setImage(tmpi);
            bttnPrev.repaint();
        }
        //System.out.println("Run() waitforimage");
        tmpi = parapp.getImage(parapp.getDocumentBase(), "arrow_rite.gif");
        if (FD.waitForImage(tmpi, this)) {
            bttnNext.setImage(tmpi);
            bttnNext.repaint();
        }
        //System.out.println("Run() waitforimage");
        tmpi = parapp.getImage(parapp.getDocumentBase(), "arrow_up.gif");
        if (FD.waitForImage(tmpi, this)) {
            bttnNetflag.setImage(tmpi);
            bttnNetflag.repaint();
        }

        System.out.println("Run() load history");
        // load the flags
        setStatus("Load flags...");
        repaint();
        loadFlagHistory(startRow);
    }

}