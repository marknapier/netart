import java.awt.*;
import java.applet.*;
import java.util.*;

/**
 *
 * This is a minimal applet that loads in another (presumably larger)
 * applet while displaying a loading message and progress bar.
 * <P>
 * The applet takes two params. One is "main class" and tells the
 * loader the class name of the applet to be displayed.
 * This param typically looks like:
 *      <param name="applet class" value="MyBigApplet">
 * <P>
 * The second param, "classes", should be a comma-delimited
 * list of the classes (including the one specified in "main class")
 * that make up the applet.
 * The classes will be loaded in the order specified,
 * and it's best if no class references a class later in the list.
 * For instance, it might look like:
 *      <param name="classes" value="HelperClass1, HelperClass2, MyBigApplet">
 * <P>
 * As an example of how to use this class, suppose you already have
 * an applet that looks like this:
 *      <applet code="MyBigApplet.class" height=400 width=400>
 *          <param name="color" value="red">
 *          <param name="speed" value="2">
 *      </applet>
 *
 * When you use the loader class you'd write something like:
 *      <applet code="AppletLoader.class" height=400 width=400>
 *          <param name="applet class" value="MyBigApplet">
 *          <param name="classes" value="HelperClass1, HelperClass2, MyBigApplet">
 *          <param name="color" value="red">
 *          <param name="speed" value="2">
 *      </applet>
 *
 */
public class AppletLoader
    extends Applet
    implements Runnable, AppletStub {
    /** Total number of classes to load. */
    int numOfClasses;

    /** Number of classes loaded so far. */
    int classesLoaded;

    /** Applet that is to be loaded and shown. */
    // The generic Applet works fine, BUT I call a function on
    // the applet from javascript (setMode()), so I have to
    // use a specific type here.
    Applet applet;
    Dimension D;

    /**
     *
     * This Hashtable is used to keep track of which classes
     * have been loaded so far. The keys are class names;
     * if there is a value for a key, that means the class
     * has been loaded. We need the hashtable for consistent
     * behavior when people reload the page, which is why it
     * is declared static.
     *
     */
    static Hashtable classes;

    /** Thread in which to do the loading. */
    Thread loading;

    /**
     *
     * Do minimal initialization. Actual class loading
     * happens in a separate (non-event) thread which is
     * created in the start() method.
     *
     */
    public void init() {
        setLayout(null);
        //setBackground(new Color(0x9999aa));
        setBackground(new Color(0x000000));
        if (classes == null)
            classes = new Hashtable();
        setFont(new Font("Helvetica", Font.PLAIN, 12));
    }

    /**
     *
     * If the applet to be displayed
     * has been loaded, call its start() method.
     * Otherwise, start a class-loading thread.
     *
     */
    public void start() {
        if (applet != null)
            applet.start();
        else {
            if (loading == null) {
                loading = new Thread(this);
                loading.start();
            }
        }
    }

    /**
     *
     * If the applet to be displayed has loaded,
     * call its stop() method.
     *
     */
    public void stop() {
        if (applet != null)
            applet.stop();
    }

    /**
     *
     * If the applet to be displayed has loaded,
     * call its destroy() method.
     *
     */
    public void destroy() {
        if (applet != null)
            applet.destroy();
    }

    /**
     *
     * Wait for an image to load.
     *
     */
    Image splashImage;
    public void loadImage(String s) {
        Image tmpImage;
        tmpImage = this.getImage(this.getDocumentBase(), s);
        int imgW = -1, imgH = -1;
        while ( (imgW = tmpImage.getWidth(this)) < 0) {
            try {
                Thread.sleep(100);
            }
            catch (InterruptedException e) {
                System.out.println(e);
            }
        }
        while ( (imgH = tmpImage.getHeight(this)) < 0) {
            try {
                Thread.sleep(100);
            }
            catch (InterruptedException e) {
                System.out.println(e);
            }
        }
        // first image found is splash
        if (splashImage == null) {
            splashImage = tmpImage;
        }
    }

    /**
     *
     * The class loading takes place in the run() method,
     * so it doesn't interfere with the event thread.
     *
     */
    public void run() {
        D = this.getSize();
        // If the applet has already loaded, don't bother continuing.
        if (applet != null)
            return;

        try {
            String appletName = getParameter("applet class");
            if (classes.get(appletName) == null) { // Check whether this has already loaded.
                // Get list of classes from params.
                StringTokenizer t = new StringTokenizer(getParameter("classes"), ",");
                numOfClasses = t.countTokens();

                // Call repaint for initial (0%) loading message.
                repaint();

                // Yield some time so event thread can repaint the applet.
                Thread.sleep(50);

                // Go through the class list and load each class in turn.
                for (int i = 0; i < numOfClasses; i++) {
                    String s = t.nextToken().trim();
                    //System.out.println("loading " + s);
                    if (s.toUpperCase().indexOf(".GIF") > 0
                        || s.toUpperCase().indexOf(".JPG") > 0) {
                        //System.out.println("Load Image " + s);
                        loadImage(s);
                    }
                    else {
                        //System.out.println("Load Class " + s);
                        Class c = Class.forName(s);
                    }
                    classesLoaded++;
                    repaint();
                    Thread.sleep(100);
                }
            }

            // Create applet to be displayed, and set its stub to this object.
            if (applet == null) {
                applet = (Applet) (Class.forName(appletName).newInstance());
            }
            applet.setStub(this);

            // Add the applet to the screen, and shape it appropriately.
            add(applet);
            applet.reshape(0, 0, size().width, size().height);

            // Call the applet's init() method before calling anything else.
            applet.init();

            // The next three calls seem to be necessary for the applet
            // to paint properly on all systems.
            applet.validate();
            applet.show();
            applet.repaint();

            // Everything's ready, so we call the applet's start method.
            applet.start();
        }
        catch (Exception e) {
            System.out.println("Loader exception= " + e);
        }
    }

    /**
     *
     * We override this method just to class paint, so
     * that there is no flicker when the screen updates.
     *
     */
    public void update(Graphics g) {
        paint(g);
    }

    /**
     *
     * Paint the loading message onto the screen.
     *
     */
    public void paint(Graphics g) {
        // Draw loading message.
        g.setColor(this.getBackground());
        g.fillRect(10, 8, 200, 15);
        g.setColor(Color.white);
        //g.drawString("Loading...", 10, 20);

        // Draw a progress bar.
        int progressBarWidth = 100;
        int filledWidth = numOfClasses == 0 ?
            0 : (progressBarWidth * classesLoaded) / numOfClasses;
        g.drawRect(10, 25, progressBarWidth, 10);
        g.fillRect(10, 25, filledWidth, 10);

        // Draw splash image (if loaded)
        if (splashImage != null) {
            int iw = splashImage.getWidth(this);
            int ih = splashImage.getHeight(this);
            g.drawImage(splashImage, (D.width - iw) / 2, (D.height - ih) / 2, this);
        }
    }

    /**
     *
     * Resize the applet to the given width and height.
     * This method must be implemented so that this
     * class conforms to the AppletStub interface.
     *
     *
     * @param   width    the new requested width for the applet.
     * @param   height   the new requested height for the applet.
     *
     */
    public void appletResize(int width, int height) {
        if (applet != null)
            applet.reshape(0, 0, width, height);
    }

}