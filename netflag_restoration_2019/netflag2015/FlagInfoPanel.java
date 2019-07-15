import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.applet.*;
import java.util.StringTokenizer;

/**
 * Show a flag, its title (country name), and some brief info in
 * a floating panel.
 */
class FlagInfoPanel
    extends ScrollablePanel {
    // flag information and imagemap panel
    private JPanel toc;
    private NetFlagPanel nfp;
    private FlagBuilder fbuilder;
    private FlagDescription FD;
    private JLabel countryNameLabel;
    JButton closeButton;
    JButton addButton;
    Image smallFlagBuffer = null;
    Flag flag = null;
    JApplet parapp;
    String meaning = "";
    String countryName = "";
    Element currentElement = null;
    boolean isCustomFlag = false;

    public FlagInfoPanel(NetFlagPanel nfp, JApplet parapp) {
        this.nfp = nfp;
        this.parapp = parapp;
        this.fbuilder = new FlagBuilder(0, 0, 250, 150, parapp);

        this.setBounds(100, 60, 400, 350);

        countryNameLabel = new JLabel("Click the flag to select a shape");
        countryNameLabel.setBounds(30, 12, 340, 25);
        countryNameLabel.setFont(new Font("arial", Font.BOLD, 14));
        this.add(countryNameLabel);

        addButton = new JButton("Add to the flag");
        addButton.setBounds(50, 300, 200, 20);
        addButton.setEnabled(false);
        this.addClickable(addButton);

        closeButton = new JButton("Ok");
        closeButton.setBounds(300, 300, 50, 20);
        this.addClickable(closeButton);

        this.setVisible(false);
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        // background
        g.setColor(Color.lightGray);
        g.fillRect(0, 0, (int) D.width - 1, (int) D.height - 1);
        // border
        g.setColor(Color.darkGray);
        g.drawRect(0, 0, (int) D.width - 1, (int) D.height - 1);
        g.setColor(Color.black);
        if (isCustomFlag) {
            //System.out.println("infopanel meaning=" + meaning);
            drawWrappedText(g, meaning, 30, 220, 350, 200);
        }
        else {
            if (currentElement != null && FD != null) {
                //System.out.println("DRAW MEANING for element id: " + currentElement.element + " name="+ currentElement.myName);
                meaning = FD.getElement(currentElement.element).meaning;
            }
            if (meaning != null && meaning != "") {
                //System.out.println("\tMEANING=" + meaning);
                drawWrappedText(g, meaning, 30, 220, 350, 200);
            }
        }
    }

    public void drawWrappedText(Graphics g, String txt, int x, int y, int w, int h) {
        FontMetrics fm = g.getFontMetrics();
        StringTokenizer st = new StringTokenizer(txt);
        int len = 0;
        int line = 0;
        int lineWidth = 0;
        String word = "";
        while (st.hasMoreTokens()) {
            word = st.nextToken();
            len = fm.stringWidth(word);
            if (lineWidth + len > w) {
                //System.out.println("Break line at word " + word);
                lineWidth = 0;
                line += 14; // font height
            }
            g.drawString(word, x + lineWidth, y + line);
            lineWidth += (len + 8); // add 10: width of space
        }
    }

    public void open(String countryname) {
        this.countryName = countryname;
        this.setVisible(true);
        if (nfp.historyMode) {
            // browsing history: don't allow edits
            addButton.setVisible(false);
            closeButton.setLocation(150, 300);
        }

        if (smallFlagBuffer == null) {
            smallFlagBuffer = this.createImage(fbuilder.w, fbuilder.h);
        }
        if (flag == null) {
            flag = new Flag();
            flag.setBounds(70, 40, fbuilder.w, fbuilder.h);
            flag.addMouseMotionListener(new MListener(flag, nfp));
            this.addClickable(flag);
        }
        if (countryname != null) {
            if (flag == null) {
                System.out.println("FlagInfoPanel.open(): flag is null");
            }
            FD = FlagDescription.getFlagDesc(countryname);
            flag.reset();
            fbuilder.makeFlag(countryname, flag);
            countryNameLabel.setText(countryname); // + "  --  Click the flag to select a shape");
            this.countryName = countryname;
            this.meaning = "Click the flag to select a shape";
            addButton.setEnabled(false);
            repaint();
        }
        else {
            countryNameLabel.setText("Country name is blank");
        }

        // Load flag meanings in background thread.
        startIt();
    }

    public void openCustom(Flag customFlag, int flagW, int flagH) {
        isCustomFlag = true;

        // adjust size of panel and close button position
        //this.setBounds(100,60,350,330);
        closeButton.setBounds(175, 290, 50, 20);
        this.setVisible(true);

        if (smallFlagBuffer == null) {
            smallFlagBuffer = this.createImage(fbuilder.w, fbuilder.h);
        }
        if (flag == null) {
            flag = new Flag();
            flag.setBounds(70, 40, fbuilder.w, fbuilder.h);
            flag.addMouseMotionListener(new MListener(flag, nfp));
            this.addClickable(flag);
        }
        if (customFlag != null) {
            // copy user defined "custom" flag into flag object
            // to display on panel.
            Element customElement;
            Element newElement;
            flag.reset();
            for (int i = 0; i <= customFlag.getNumElements(); i++) {
                customElement = customFlag.getElement(i);
                if (customElement != null) {
                    newElement = fbuilder.makeFlagElement(customElement.country, customElement.element);
                    newElement.moveTo( (int) (customElement.xPercent * (float) fbuilder.w),
                                      (int) (customElement.yPercent * (float) fbuilder.h));
                    flag.add(newElement);
                }
            }
            meaning = customFlag.comment;
            countryName = "" + customFlag.country;
            countryNameLabel.setText(countryName);
            addButton.setVisible(false);
            repaint();
        }
        else {
            countryNameLabel.setText("Country name is blank");
        }

        // Load flag meanings in background thread.
        //startIt();
    }

    // Load flag meanings in thread
    //
    public synchronized void run() {
        currentElement = null;
        String abbrev = FlagDescription.getAbbrev(countryName);
        if (abbrev != null) {
            System.out.println("FlagInfo: get meanings for abbrev=" + abbrev);
            this.nfp.loadflagMeanings(abbrev);
        }
    }

    public void handleClick(JComponent c, int x, int y) {
        if (c == closeButton) {
            this.setVisible(false);
        }
        else if (c == flag) {
            // get clicked element from small flag
            Element f = flag.processMouseClick(x, y);
            if (f != null) {
                System.out.println("add " + f.myName + " to netflagpanel");
                // create same element in large flag
                //f = nfp.addFlagElement(f.country,f.element);
                // now edit large element in large flag
                //nfp.openElementPanel(f);
                currentElement = f;
                addButton.setLabel("Add " + currentElement.element + " to the flag");
                addButton.setEnabled(true);
                repaint();
            }
        }
        else if (c == addButton) {
            nfp.addFlagElement(currentElement.country, currentElement.element);
        }
    }

}