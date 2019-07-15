import javax.swing.*;
import java.awt.*;
import java.applet.*;

class ThumbnailPanel  extends ScrollablePanel {
    public NetFlagPanel nfp;
    private Image tinyflag = null;
    public String[] countries = null;
    private boolean initialized = false;
    int[] allFlagIds = null;
    Thumbnail[] thumbs;
    JLabel[] names;
    int[] flagsToShow = null;
    int[] flagsOnThisPage = null;
    static Image[] thumbnails = null;

    public ThumbnailPanel(NetFlagPanel nfp, String[] countries) {
        int y = 0;
        JLabel l;
        Thumbnail tnf = null;

        this.nfp = nfp;
        this.countries = countries;

        this.setBounds(430, 50, 180, 400);
        this.setVisible(false);

        numItems = countries.length;
        rowsPerPage = 13;
        thumbnails = new Image[countries.length];
        thumbs = new Thumbnail[rowsPerPage];
        names = new JLabel[rowsPerPage];

        // make blank rows of thumbnails/names
        for (y = 0; y < rowsPerPage; y++) {
            // add country name label
            l = new JLabel("nada");
            l.setBounds(55, (30 * y) + 6, 100, 20);
            names[y] = l;
            this.addClickable(l);

            // add thumbnail panel
            tnf = new Thumbnail(null, 10, (30 * y) + 4, 33, 20);
            tnf.setImage(null);
            thumbs[y] = tnf;
            this.addClickable(tnf);
        }

        // Add a scrollbar
        addScrollbar();
    }

    // If open() has no id list param, then
    // make a dummy id list that contains all flag ids.
    //
    public void open() {
        if (allFlagIds == null) {
            allFlagIds = new int[FlagDescription.countryNames.length];
            for (int idx = 0; idx < FlagDescription.countryNames.length; idx++) {
                allFlagIds[idx] = idx;
            }
        }
        open(allFlagIds);
    }

    // Pass a list of flag ids to open(). These are the flags
    // to be displayed in the window.
    //
    public void open(int[] flagIds) {
        this.setVisible(true);
        repaint();

        // Prepare panel to show list of flag ids
        //
        flagsToShow = flagIds;
        numItems = flagIds.length;
        startShowRow = 0;
        // show the first page
        fillPage();
        refreshScroller();
        repaint();
    }

    public void shiftTo(int row) {
        // show a page of flags
        startShowRow = row;
        fillPage();
        repaint();
    }

    // show one page of flag list
    //
    public void fillPage() {
        int y = 0, i = 0, idx = 0;
        for (i = startShowRow; i < numItems && y < rowsPerPage; i++, y++) {
            // Fill page with thumbnail images and names
            idx = flagsToShow[i];
            thumbs[y].setImage( (thumbnails == null) ? null : thumbnails[idx]);
            thumbs[y].setText(countries[idx]);
            names[y].setText(countries[idx]);
            thumbs[y].setVisible(true);
            names[y].setVisible(true);
        }
        // fill in trailing blank rows
        for (; y < rowsPerPage; y++) {
            thumbs[y].setVisible(false);
            names[y].setVisible(false);
        }
        // preload images if necessary (threaded)
        loadImages();
    }

    // refresh one thumbnail image (assumes that page has
    // been filled (fillPage()) and that thumbnails[] has
    // been initialized (see loadImages())
    //
    public void fillOneImage(int flagId) {
        int y = 0, i = 0;
        for (i = startShowRow; i < numItems && y < rowsPerPage; i++, y++) {
            if (flagsToShow[i] == flagId) {
                thumbs[y].setImage(thumbnails[flagId]);
                break;
            }
        }
    }

    // Load thumbnail images for a list of flag ids.
    // Presumably these ids are the flags on the
    // current page of ThumbnailPanel, so we call
    // tp.fillOneImage() to refresh the image on the panel.

    public synchronized void run() {
        // load all thumbnails
        String imageName = "";
        Image tinyflag = null;
        int flagId = 0;
        for (int i = 0; i < flagsOnThisPage.length && runIt; i++) {
            // load image if not already loaded
            flagId = flagsOnThisPage[i];
            if (this.thumbnails[flagId] == null) {
                imageName = this.countries[flagId].toLowerCase();
                imageName = imageName.replace(' ', '_'); // kill spaces
                imageName = imageName.replace('\'', '_'); // kill '
                imageName = "thumbnails/" + imageName + "_tn.gif";
                tinyflag = nfp.parapp.getImage(nfp.parapp.getDocumentBase(), imageName);
                //System.out.println("loading thumbnail image " + imageName);
                if (nfp.FD.waitForImage(tinyflag, this)) {
                    this.thumbnails[flagId] = tinyflag;
                    this.fillOneImage(flagId); // refresh image if visible
                }
            }
        }
    }

    // Start a thread that will load thumbnail images
    // for the current page of flags.

    public void loadImages() {
        int y = 0, i = 0;
        flagsOnThisPage = new int[rowsPerPage];
        for (i = startShowRow; i < numItems && y < rowsPerPage; i++, y++) {
            flagsOnThisPage[y] = flagsToShow[i];
        }
        startIt();
    }

    public void handleClick(JComponent c, int x, int y) {
        String objClass = c.getClass().getName();
        if (objClass.indexOf("JLabel") >= 0) {
            JLabel l = (JLabel) c;
            nfp.openFlagInfo(l.getText());
        }
        else if (objClass.indexOf("Thumbnail") >= 0) {
            Thumbnail tf = (Thumbnail) c;
            nfp.openFlagInfo(tf.getText());
        }
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        // background
        g.setColor(Color.lightGray);
        g.fillRect(0, 0, (int) D.width - 1, (int) D.height - 1);
        // border
        g.setColor(Color.darkGray);
        g.drawRect(0, 0, (int) D.width - 1, (int) D.height - 1);
    }
}