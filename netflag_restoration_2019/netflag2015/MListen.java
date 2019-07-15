import java.awt.event.*;
import java.net.*;
import java.awt.*;

//////////

// Listen for motion and drag events on LargeFlag

class MListener extends MouseMotionAdapter
{
  Flag f = null;
  Element el = null;
  NetFlagPanel nfp;
  public MListener(Flag f, NetFlagPanel nfp) {
       this.f = f;
       this.nfp = nfp;
  }
  public void mouseMoved(MouseEvent e) {
    //System.out.println("MMouse moved x=" +  e.getX());
    el = f.processMouseMove(e.getX(),e.getY());
    nfp.setStatus( (el==null)? "" : el.myName);
  }
  public void mouseDragged(MouseEvent e) {
    f.drag(e.getX(),e.getY());
    nfp.closePanels();
    nfp.repaint();
    //System.out.println("mouse dragged x=" +  e.getX());
  }
}


// Listen for clicks on LargeFlag

class NetFlagListener extends MouseAdapter
{
    Flag netflag = null;
    NetFlagPanel nfp = null;
    public NetFlagListener(Flag flag, NetFlagPanel nfp) {
       this.netflag = flag;
       this.nfp = nfp;
    }
  public void mousePressed(MouseEvent e) {
    netflag.startDrag(e.getX(),e.getY());
    //System.out.println("mouse pressed x=" +  e.getX());
  }
  public void mouseClicked(MouseEvent e) {
    //System.out.println("mouse click x=" +  e.getX());
    Element f = netflag.processMouseClick(e.getX(),e.getY());
    if (f != null) {
      nfp.openElementPanel(f,e.getX(),e.getY());
    }
  }
  public void mouseReleased(MouseEvent e) {
    netflag.stopDrag();
    nfp.repaint();
    //System.out.println("mouse release x=" +  e.getX());
  }
  public void mouseEntered(MouseEvent e) {
    //System.out.println("mouse release x=" +  e.getX());
  }
  public void mouseExited(MouseEvent e) {
    nfp.setStatus("");
  }
}


// Listen for scroll events on NetFlagPanel
/*
class flagScrollListener implements AdjustmentListener
{
  Scrollbar scroller;
  Flag f;
  public flagScrollListener(Flag f, Scrollbar scroller) {
       this.scroller = scroller;
       this.f = f;
  }
  public void adjustmentValueChanged(AdjustmentEvent e) {
    System.out.println("Scrolled to " + scroller.getValue());
//    f.startShowRow = scroller.getValue();
    // reverse direction of scrollbar
    f.startShowRow = f.getNumElements() - (scroller.getValue() + f.rowsPerPage);
    f.repaint();
  }
}
*/


/*
class KeyListen extends KeyAdapter
{
    Flag fm = null;
    public KeyListen(Flag fm) {
       this.fm = fm;
    }
  public void keyPressed(KeyEvent k) {
    System.out.println("key pressed k=" +  (char)k.getKeyCode() );
    fm.processKey((char)k.getKeyCode());
  }
  public void keyReleased(KeyEvent k) {
    System.out.println("key release k=" +  (char)k.getKeyCode() );
  }
  public void keyTyped(KeyEvent k) {
    System.out.println("key typed k=" +  (char)k.getKeyCode() );
  }
}
*/

//////////////////////////////////////////////////////
// Listen for clicks on Net Flag Panel (main screen)
// interface buttons
//

/*
class ButtonListener extends MouseAdapter
{
  NetFlagPanel nfp;
  public ButtonListener(NetFlagPanel nfp) {
    this.nfp = nfp;
  }
  public void mouseClicked(MouseEvent e) {
    nfp.handleClick(e);
  }
}

*/
