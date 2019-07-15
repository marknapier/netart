import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

// Base class for all scrolling panels

class ScrollablePanel extends JPanel implements Runnable {
    Cursor handcurs;
    MouseClickListener mcl;
    JScrollBar scroller;
    Dimension D;
    // to manage scrolling lists
    int numItems = 0;
    int startShowRow = 0;
    int rowsPerPage = 10;
    // for background thread
    Thread bgProcess;
    boolean runIt = false;
    // flag for border on/off
    boolean drawBorder = true;

    public ScrollablePanel() {
        handcurs = new Cursor(Cursor.HAND_CURSOR);
        mcl = new MouseClickListener(this);
        setLayout(null);
        setBackground(Color.lightGray);
    }

    // add a clickable component to the panel
    public void addClickable(JComponent c) {
        super.add(c);
        c.addMouseListener(mcl);
        c.setCursor(handcurs);
    }

    // add a default vertical scrollbar to panel
    public void addScrollbar() {
        scroller = new JScrollBar(JScrollBar.VERTICAL, 0, rowsPerPage, 0, numItems);
        scroller.setBounds( (int) D.width - 15, 1, 14, (int) D.height - 2);
        scroller.addAdjustmentListener(new ScrollListener(this, scroller));
        this.addClickable(scroller);
    }

    public void setBounds(int x, int y, int w, int h) {
        super.setBounds(x, y, w, h);
        D = this.getSize();
    }

    public void setBorder(boolean flag) {
        drawBorder = flag;
    }

    // adjust panel to display a new page of list
    // called by Scroller
    public void shiftTo(int row) {
    }

    // Open the panel (make it visible, with new data)
    public void open() {
        this.setVisible(true);
        repaint();
    }

    public void close() {
        this.setVisible(false);
        repaint();
    }

    // Synchronize scroller to new items list
    public void refreshScroller() {
        if (scroller != null) {
            scroller.setMaximum(numItems);
            scroller.setValue(0);
            scroller.setVisibleAmount(rowsPerPage);
            scroller.setBlockIncrement(rowsPerPage - 1);
            if (numItems < rowsPerPage) {
                scroller.disable();
            }
            else {
                scroller.enable();
            }
        }
    }

    // respond to a keypress
    public void handleKey(String keyHit) {
    }

    // respond to a click on any panel component
    // NOTE: scrollbar is also a "clickable" component
    // so clicks on scrollbar will trigger handleClick()
    // Be sure to check type of component when responding
    // ie:
    //   String objClass = c.getClass().getName();
    //   if (objClass.indexOf("Label") >= 0) {

    public void handleClick(JComponent c, int x, int y) {
    }

    // respond to a mouse over any panel component
    public void handleMouseEnter(JComponent c) {
    }

    // respond to a mouse exit from any panel component
    public void handleMouseExit(JComponent c) {
    }

    public void paintComponent(Graphics g) { //mjn
        // call super to paint opaque background
        super.paintComponent(g);
        // background
        //g.setColor(getBackground());
        //g.fillRect(0, 0, (int) D.width - 1, (int) D.height - 1);
        // border
        if (drawBorder) {
            g.setColor(Color.darkGray);
            g.drawRect(0, 0, (int) D.width - 1, (int) D.height - 1);
        }
    }

    // Run a background process for this panel (ie. load images
    // or data after panel opens)

    public synchronized void run() {
    }

    public void startIt() {
        // if thread is running, set the 'runIt' flag to false
        // wait 1/4 sec for thread to stop
        if (bgProcess != null && bgProcess.isAlive()) {
            this.stopIt();
            try {
                Thread.sleep(300);
            }
            catch (Exception e) {}
        }
        // set runIt flag and start thread
        runIt = true;
        bgProcess = new Thread(this);
        bgProcess.start();
    }

    public void stopIt() {
        runIt = false;
        bgProcess = null;
    }

}

/////////////////////////////////////////////////////////
// Listener for scrollbar

class ScrollListener
    implements AdjustmentListener {
    JScrollBar scroller;
    ScrollablePanel p;
    public ScrollListener(ScrollablePanel p, JScrollBar scroller) {
        this.scroller = scroller;
        this.p = p;
    }

    public void adjustmentValueChanged(AdjustmentEvent e) {
        p.shiftTo(scroller.getValue());
    }
}

/////////////////////////////////////////////////////////
// Listener for label clicks

class MouseClickListener
    extends MouseAdapter {
    JComponent bttn;
    ScrollablePanel sp;

    public MouseClickListener(ScrollablePanel sp) {
        this.sp = sp;
    }

    //public void mouseClicked(MouseEvent e) {
    //  sp.handleClick(e.getComponent(), e.getX(), e.getY());
    //}
    public void mouseReleased(MouseEvent e) {
        sp.handleClick( (JComponent) e.getComponent(), e.getX(), e.getY());
    }

    public void mouseEntered(MouseEvent e) {
        sp.handleMouseEnter( (JComponent) e.getComponent());
    }

    public void mouseExited(MouseEvent e) {
        sp.handleMouseExit( (JComponent) e.getComponent());
    }
}

/////////////////////////////////////////////////////////
// Listener for keyboard

class KeyPressListener
    extends KeyAdapter {
    ScrollablePanel sp;
    public KeyPressListener(ScrollablePanel sp) {
        this.sp = sp;
    }

    public void keyPressed(KeyEvent k) {
        System.out.println("key pressed k=" + (char) k.getKeyCode());
        if (k.getKeyCode() == KeyEvent.VK_ESCAPE) {
            System.out.println("Got escape!");
            sp.handleKey("esc");
        }
        else {
            //sp.handleKey(k.getKeyChar());
        }
        //sp.processKey((char)k.getKeyCode());
    }
    /*
      public void keyReleased(KeyEvent k) {
        System.out.println("key release k=" +  (char)k.getKeyCode() );
      }
      public void keyTyped(KeyEvent k) {
        System.out.println("key typed k=" +  (char)k.getKeyCode() );
      }
     */
}