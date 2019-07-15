import javax.swing.*;
import java.awt.*;
import java.net.*; // for URL

//============================================
// Display latest flag in a small applet
// with no editing functionality
//============================================

public class NetFlagLite  extends JApplet  implements Runnable {
    Thread process = null;
    int numHistoryItems = 150; // number of flag elements in history
    Flag flag;
    FlagBuilder fb;
    Dimension D;
    String domain;
    protected JLabel statusLabel;
    URL appletBase = null;
    URL flagdataURL = null;

    public NetFlagLite() {
    }

    public void init() {
        appletBase = this.getDocumentBase();
        try {
            flagdataURL = new URL(appletBase, "nf_shapedata.gzip");
        }
        catch (Exception e) {
            System.out.println("NetFlagLite.init(): error forming flagdata URL: " + e);
        }
        domain = appletBase.getHost();
        //
        this.setBackground(Color.darkGray);
        this.getContentPane().setLayout(null);
        D = this.getSize();

        // create a flag factory for small flag
        fb = new FlagBuilder(0, 0, D.width - 2, D.height - 2, this);
        flag = new Flag();
        flag.bgColor = Color.lightGray;
        flag.setBounds(1, 1, D.width - 2, D.height - 2);
        flag.rowsPerPage = 50;

        //
        // status messages
        statusLabel = new JLabel();
        statusLabel.setBounds(1, 1, 132, 14);
        statusLabel.setBackground(Color.lightGray);
        statusLabel.setForeground(Color.black);
        statusLabel.setFont(new Font("Arial", Font.PLAIN, 10));
        statusLabel.setText("Loading flag...");

        this.getContentPane().add(statusLabel);
        this.getContentPane().add(flag);

        flag.repaint();
    }


    public void setStatus(String t) {
        statusLabel.setText(t);
        repaint();
    }


    public boolean loadFlagHistory() {
        setStatus("Load database");
        FlagDescription.preLoadFlags(flagdataURL);

        // Load the last 100 rows of net.flag changes
        setStatus("Load latest net.flag");
        cgiDataPoster storage = new cgiDataPoster();
        String[] flagHistory = storage.readURL("http://" + domain + "/cgi-bin/nf_history.pl?get");

        setStatus("Load images");

        // Parse the last 30 rows of history only
        int line = 0;
        String[] tokens = new String[10];
        int x = 0, y = 0;

        for (line = 120; line < numHistoryItems && flagHistory[line] != null; line++) {
            if (FlagDescription.parseRecord(flagHistory[line], tokens, ",")) {
                try {
                    x = Integer.parseInt(tokens[2]);
                    y = Integer.parseInt(tokens[3]);
                }
                catch (Exception e) {
                    System.out.println("loadflagHistory():" + e);
                    System.out.println("line: x=" + tokens[2] + " y=" + tokens[3]);
                }
                Element F = fb.makeFlagElement(tokens[0], tokens[1]);
                if (F != null) {
                    // KLUDGE:
                    // Log file stores x,y as absolute pixel positions
                    // have to scale these vals to smaller size flag
                    // so set x,y in proportion to original flag size
                    x = (int) ( ( (float) x / (float) 700) * (float) (D.width));
                    y = (int) ( ( (float) y / (float) 440) * (float) (D.height));
                    F.moveTo(x, y);
                    flag.add(F);
                }
            }
        }
        System.out.println("Done flag history");
        // won't need this any more
        statusLabel.setVisible(false);
        flag.repaint();
        return true;
    }


    public void start() {
        // Start the thread
        process = new Thread(this);
        process.start();
    }


    public void stop() {
        process = null;
    }


    public void run() {
        // load the flag data and latest net.flag
        loadFlagHistory();
        // hang out
        while (process != null) {
            flag.repaint();
            try {
                Thread.sleep(200);
            }
            catch (InterruptedException e) {}
        }
    }

}