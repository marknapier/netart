import javax.swing.*;
import java.awt.*;
import java.net.*; // for URL
import java.io.*;
import java.util.*;
import java.awt.image.*;


/**
 * Display a list of flags as a slideshow, in a small applet with no
 * editing functionality.  Can also save .flag to .png, for a batch of flags.
 * <P>
 * Loads flag file names from: flaglist.txt.  The file looks like: <BR>
 * <PRE>
 *        050719-230506.flag
 *        050719-231053.flag
 *        050720-104148.flag
 *        ...
 * </PRE>
 * <P>
 * Loads ".flag" files from flags folder (normally each contains 30 flag elements).
 * Uses Swing components.
 * <P>
 * Created: July 2005
 * Author: napier
 */

public class NetFlagViewer  extends JApplet  implements Runnable
{
	// Adjustable Settings:
    //String flagListFile = "flaglist.txt";   // file containing list of flags
    String flagListFile = "flaglist.txt";   // file containing list of flags
    String flagFoldername = "flags2011";  // folder containing .flag files
    int timeDelay = 2000;           // millisecond delay between flags
    float scale = 3f;               // enlarge the flag by this factor (3)
    boolean saveImage = true;      // if true, save each flag to an image file
    boolean useDateFolders = false;  // if true, look for flags in dated folders ie.  folder 0509 holds Sept 2005 flags
    //
    String[] flagFilenames;         // array of flag file names (see loadFlaglist())
    Flag flag;
    FlagBuilder fb;
    Dimension D;
    JLabel statusLabel;
    Thread process = null;


    public NetFlagViewer() {
    }

    public static void main(String[] args) {
        JFrame f = new JFrame();
        NetFlagViewer app = new NetFlagViewer();
        app.init();
        app.start();
        f.getContentPane().add("Center", app);
        f.setSize(Toolkit.getDefaultToolkit().getScreenSize());
        f.show();
    }


    public void init() {
        // settings
        setBackground(Color.darkGray);
        getContentPane().setLayout(null);

        // If applet has no dimensions, set to default size
        D = getSize();
        if (D.getWidth() <= 0 || D.getHeight() <= 0) {
            //setSize(700, 440);
            setSize((int)(700f*scale), (int)(440f*scale));
            D = this.getSize();
        }

        // create a flag factory for flag
        fb = new FlagBuilder(0, 0, D.width, D.height, this);
        flag = new Flag();
        flag.bgColor = Color.darkGray;
        flag.setBounds(0, 0, D.width, D.height);
        flag.rowsPerPage = 50;
        flag.setBorder(null);

        // make label for status messages
        statusLabel = new JLabel();
        statusLabel.setBounds(5, 5, 132, 14);
        statusLabel.setBackground(Color.darkGray);
        statusLabel.setForeground(Color.white);
        statusLabel.setFont(new Font("Arial", Font.PLAIN, 10));
        statusLabel.setText("Loading flag...");

        // add the components to applet
        this.getContentPane().add(statusLabel);
        this.getContentPane().add(flag);
    }


    /**
     * Set status message, refresh the screen, pause a moment so Java can draw.
     */
    public void setStatus(String t) {
        statusLabel.setText(t);
        repaint();
        try {
            Thread.sleep(100);
        }
        catch (InterruptedException e) {}
    }


    /**
     * Make a url pointing to the given filename, relative to the applet code base.
     * @param filename
     * @return a url
     */
    public URL makeURL(String filename) {
        URL appletBase = null;
        URL u = null;
        // Get the "root" folder for the applet
        try {
            appletBase = this.getCodeBase();
        }
        catch (Exception e) {
            // couldn't get codebase, must be an app, use current folder
            String currentDirectory = System.getProperty("user.dir");
            try {
                appletBase = new URL("file:" + currentDirectory + "/");
            }
            catch (Exception ee) {
                System.out.println("NetFlagLite.makeURL(): error getting codebase: " + ee);
            }
        }
        // Make a url to the given file
        try {
            u = new URL(appletBase, filename);
        }
        catch (Exception e) {
            System.out.println("NetFlagLite.makeURL(): error forming URL to " +filename+ ": " + e);
        }
        return u;
    }


    /**
     * Load flag parts database from gzip file (load only once).
     * @return
     */
    public boolean loadDatabase() {
        URL flagdataURL = null; // points to flagparts data file
        flagdataURL = makeURL("nf_shapedata.gzip");
        // debug
        System.out.println("NetFlagViewer: load flag database from file " + flagdataURL);
        // load the flagparts database
        setStatus("Load flag database");
        FlagDescription.preLoadFlags(flagdataURL);
        return true;
    }


    /**
     * Load list of flags to be included in slideshow
     */
    public boolean loadFlaglist() {
        try {
            // open the file
            URL url = makeURL(flagListFile);
            InputStream in = url.openStream();
            InputStreamReader inreader = new InputStreamReader(in);
            BufferedReader inbuffer = new BufferedReader(inreader);
            ArrayList names = new ArrayList();
            String line;
            // read the flag file names
            while ( (line = inbuffer.readLine()) != null) {
                //System.out.println("GOT LINE: " + line);
                names.add(line);
            }
            inbuffer.close();
            // convert to array
            flagFilenames = new String[ names.size() ];
            names.toArray(flagFilenames);
        }
        catch (IOException ioe) {
            System.out.println("NetFlagViewer.loadFlatlist(): exception when loading flag list: " + ioe);
            return false;
        }
        return true;
    }


    /**
     * Load a flag from .flag file and add elements to Flag object.
     *
     * If useDateFolders is true, then look for the .flag file in a folder
     * that has the year/month date of the flag. Example: a flag file named
     * "050219-105502.flag" was created on 19/02/05 or Feb 19 2005.  Look for
     * this file in the folder flags/0502/
     *
     * @return true if loaded successfully
     */
    public boolean loadFlag(String flagFilename) {
        // URL to the flag that we want to view
        URL flagfileURL;
        if (useDateFolders) {
            flagfileURL = makeURL(flagFoldername + "/" + flagFilename.substring(0,4) + "/" + flagFilename);
        }
        else {
            flagfileURL = makeURL(flagFoldername + "/" + flagFilename);
        }

        // Status message
        setStatus("Load flag from file "  + flagfileURL);
        //System.out.println("Load flag from file "  + flagfileURL);

        // Load the rows from flag file
        cgiDataPoster storage = new cgiDataPoster();
        String[] flagHistory = storage.readURL(flagfileURL);
        //System.out.println("loaded "  + flagHistory.length + " rows from file " + flagfileURL);

        // Status message
        setStatus("Load images");

        // clear the flag
        flag.reset();

        // Parse the last 30 rows of history only
        String[] tokens = new String[10];
        int line = 0;
        int x = 0, y = 0;
        for (line = 0; line < flagHistory.length && flagHistory[line] != null; line++) {
            // skip comment lines
            if (flagHistory[line].startsWith("#")) {
                continue;
            }
            // get position and type of flag element
            if (FlagDescription.parseRecord(flagHistory[line], tokens, ",")) {
                try {
                    x = Integer.parseInt(tokens[2]);
                    y = Integer.parseInt(tokens[3]);
                }
                catch (Exception e) {
                	// invalid xy values, skip this row
                    System.out.println("NetFlagViewer.loadFlag():" + e);
                    System.out.println("line: x=" + tokens[2] + " y=" + tokens[3]);
                    continue;
                }
                // add element to the flag
                //System.out.println("Make element " + tokens[0] + "," + tokens[1]);
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
        long showUntil;
        // load the flag descriptions
        loadDatabase();
        // load the list of flags to include in slideshow
        loadFlaglist();
        // show the flags
        for (int i=0; process != null; i++) {
            System.out.println("Processing flag " + i + " of " + flagFilenames.length);

            // get next flag
            loadFlag( flagFilenames[i % flagFilenames.length] );

            // show it for a few seconds
            showUntil = System.currentTimeMillis() + timeDelay;
            while (System.currentTimeMillis() < showUntil && process != null) {
            	flag.repaint();  //paint(getGraphics());
            	try {
            		Thread.sleep(200);
            	}
            	catch (InterruptedException e) {}
            }

            // save the flag image to a file
            if (saveImage) {
                try { // give it time to fully redraw
                    Thread.sleep(2000);
                }
                catch (InterruptedException e) {}
                saveImageAsPNG(flag.offscreen, makeImgFilename(flagFilenames[i % flagFilenames.length]));
                if (i >= flagFilenames.length-1) {
                	break;
                }
            }

        }
    }


    public String makeImgFilename(String flagFilename) {
        String fname = null;
        if (flagFilename != null) {
            fname = flagFilename.substring(0, flagFilename.indexOf("."));
        }
        return flagFoldername + "/" + fname + ".png";
    }


    // ==========================================================
    // To save flags as PNG
    // ==========================================================

    public int[] getPixels(Image img) {
        int[] pixels = null;
        if (img != null) {
            int w = img.getWidth(this);
            int h = img.getHeight(this);
            PixelGrabber pg;
            if (w > 0 && h > 0) {
                pixels = new int[w * h];
                pg = new PixelGrabber(img, 0, 0, w, h, pixels, 0, w);
                try {
                    pg.grabPixels();
                }
                catch (InterruptedException e) {
                    System.err.println("getPixels(): interrupted waiting for pixels!");
                    return null;
                }
                if ( (pg.getStatus() & ImageObserver.ABORT) != 0) {
                    System.err.println("getPixels(): image fetch aborted or errored");
                    return null;
                }
            }
        }
        return pixels;
    }

    public void savePixelsAsPNG(int[] pixels, int width, int height, String saveFilename) {
        try {
            // Create a BufferedImage with the RGB pixels then save as PNG
            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            image.setRGB(0, 0, width, height, pixels, 0, width);
            javax.imageio.ImageIO.write(image, "png", new File(saveFilename));
        }
        catch (Exception e) {
            System.out.println("GLApp.screenShot(): exception " + e);
        }
    }

    public void saveImageAsPNG(Image img, String filename) {
        if (img != null && filename != null) {
            int[] pixels = getPixels(img);
            int w = img.getWidth(this);
            int h = img.getHeight(this);
            System.out.println("Save PNG to file " + filename + " w=" + w + " h=" + h);
            if (w > 0 && h > 0 && pixels != null) {
                savePixelsAsPNG(pixels, w, h, filename);
            }
        }
    }

}