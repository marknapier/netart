import javax.swing.*;
import java.awt.*;

////////////////////////////////////////////////////

class Flag extends JPanel   // Component
{
    public final int MAXITEMS = 200;
    private Element[] F = new Element[MAXITEMS];
    private int[] zorder = new int[MAXITEMS];
    private int Fcount = 0;
    private int FZ = 0;   // Fader Z order (counter used by run())
    Graphics bgg = null;   //offscreen buffer
    boolean once = false;  // draw only once (for testing)
    public Element currF = null;   // mouse is over this element
    public int currX=0, currY=0;
    Image offscreen;
    Dimension d;
    int startMods = 0;
    Color bgColor = Color.gray;
    // to handle scrolling through flag history
    public int startShowRow = -1;
    int rowsPerPage = 30;
    String country="";
    String comment="";

    public Flag() {
        this.setBorder(null);
    }


    public Element find(String fname) {
      for (int i=0; i < Fcount; i++) {
        if (F[i].myName.equals(fname)) {
          return F[i];
        }
      }
      return (Element)null;
    }


    public boolean add(Element f) {
       // Each element is assigned an 'id', it's index into the F array
       // that index number is also stored int zorder[].  Zorder[]
       // can be rearranged to change order layers are displayed in (we
       // traverse zorder[] to get indexes into F[])
       //
       if (Fcount < F.length) {
          this.F[Fcount] = f;
          this.zorder[Fcount] = Fcount;   // default zorder is order added
          Fcount++;
          //f.active = false;  // force this off at first
          return true;
       }
       else {
          System.out.println("Cannot add element (limit of " +MAXITEMS+ " reached)");
          return false;
       }
    }


    public int getNumElements()
    {
      return Fcount;
    }


    public Element getElement(int n)
    {
      if (n < Fcount) {
        return F[n];
      }
      return null;
    }


    public Image getImage()
    {
      // return offscreen buffer image
      return offscreen;
    }


    public void update__(Graphics g)
    {
      paint(g);
    }


    public void paintComponent(Graphics g)
    {
      if (offscreen == null) {
        d = this.size();
        offscreen = this.createImage(d.width,d.height);
        bgg = offscreen.getGraphics();
      }
      // clear background
      bgg.setColor( bgColor );
      bgg.fillRect(0,0,d.width,d.height);
      // draw flag elements into buffer image
      drawElements(bgg);
      // draw buffer image to screen
      g.drawImage(offscreen,0,0,this);
    }


    public synchronized boolean shiftZ(Element f, int direction) {
      // Shift the given layer entry up or down by one
      Element foundF = null;
      int Z;             // the index in zorder array
      int faderID = -1;  // the fader index in F[]
      System.out.println("ShiftZ() " + f.myName + " Fcount=" + Fcount);
      // search flag elements
      int rowCount = 0;
      int numRows = rowsPerPage;
      if (Fcount < rowsPerPage) {
        numRows = Fcount;
      }
      if (startShowRow == -1) {
        // init start row
        startShowRow = Fcount - numRows;
      }
      for (Z=startShowRow; Z<Fcount && rowCount<numRows && foundF==null; Z++) {
        //System.out.println("  layer " +Z+ " is " + F[zorder[Z]].name);
        if (F[zorder[Z]] == f) {
          foundF = F[zorder[Z]];
          faderID = zorder[Z];
          break;
        }
      }
      // if found, shift element by one
      if (foundF == null) {
        System.out.println("foundf is null");
        return false;
      }
      else if (Z < startMods) {
        System.out.println("Element cannot be edited");
        return false;
      }
      else if (direction > 0) {
        System.out.println("  found fader " +f.myName+ " at layer=" +Z+ " name=" + F[zorder[Z]].myName);
        if (Z >= Fcount-1) {
          System.out.println("element is already at the top of stack");
          return false;
        }
        zorder[Z] = zorder[Z+1];
        zorder[Z+1] = faderID;
        return true;
      }
      else if (direction < 0) {
        System.out.println("  found fader " +f.myName+ " at layer=" +Z+ " name=" + F[zorder[Z]].myName);
        if (Z <= startMods) {
          System.out.println("element is already at the bottom of stack");
          return false;
        }
        zorder[Z] = zorder[Z-1];
        zorder[Z-1] = faderID;
        return true;
      }
      return false;
    }


    public Element processMouseMove(int x, int y) {
      Element foundF = null;
      // Look through visible elements from top to bottom
      // See what element cursor is in
      int numRows = rowsPerPage;
      int i = 0;
      if (Fcount < rowsPerPage) {
        numRows = Fcount;
      }
      if (startShowRow == -1) {
        // init start row
        startShowRow = Fcount - numRows;
      }
      this.currX = x;
      this.currY = y;
      //System.out.println("Check elements " + startShowRow + " - " + (startShowRow+numRows-1) + " out of " + Fcount);
      for (i=startShowRow+numRows-1; i>=startShowRow && foundF==null; i--) {
        //System.out.println("Check row " + i + ":" + F[zorder[i]].name + " at " + F[zorder[i]].x + "," + + F[zorder[i]].y);
        if (F[zorder[i]].active && F[zorder[i]].containsPoint(x,y)) {
          //System.out.println("fader #" + zorder[i] + " in layer " + i + " contains " + x + "," + y);
          foundF = F[zorder[i]];
          break;
        }
      }
      if (foundF != null) {
        if (foundF != currF) {
          currF = foundF;
          currF.canEdit = (i>=this.startMods);
        }
      }
      else {
        currF = null;
      }
      return foundF;
    }


    public void reset() {
      for (int i=0; i < Fcount; i++) {
        F[i] = null;
      }
      Fcount = 0;
    }


    ///////////////////////////////////////////
    // Handle drag

    Element dragElement = null;
    int dragOffsetX = 0;
    int dragOffsetY = 0;

    public Element startDrag(int x, int y) {
      dragElement = processMouseClick(x, y);
      dragOffsetX = dragElement.myX - x;
      dragOffsetY = dragElement.myY - y;
      return dragElement;
    }

    public void stopDrag() {
      // KLUDGE: for fine tuning element positions
      // Show current element position in proportion
      // to flag size (hard coded for now)
      int elx = (int) (((float)dragElement.myX/(float)700) * (float)1000);
      int ely = (int) (((float)dragElement.myY/(float)440) * (float)1000);
      int elw = (int) (((float)dragElement.myW/(float)700) * (float)1000);
      int elh = (int) (((float)dragElement.myH/(float)440) * (float)1000);
      System.out.println("Current X,Y,W,H = " + elx + "\t" + ely + "\t" + elw + "\t" + elh );
      dragElement = null;
    }

    public void drag(int x, int y) {
      if (dragElement.canEdit) {
        dragElement.moveTo(x+dragOffsetX,y+dragOffsetY);
        repaint();
      }
    }

    public Element processMouseClick(int x, int y) {
      // find current element (currF)
      processMouseMove(x,y);
      if (currF != null) {
        //System.out.println("Clicked on "
        //      + " country:" + ((currF.country==null)? "null" : currF.country)
        //      + " element:" + ((currF.element==null)? "null" : currF.element) );
        return currF;
      }
      else {
        System.out.println("processMouseClick(): found null component");
        return null;
      }
    }


    public void setStartMods() {
      startMods = Fcount;
    }


    public void setstartShowRow(int row)
    {
      startShowRow = row;
      if (startShowRow < 0) {
        startShowRow = 0;
      }
    }

    public String getChanges() {
      Element fdr = null;
      String changes="";
      String tmp = "";
      int i = 0;
      for (i=startMods; i < Fcount; i++) {
        fdr = F[zorder[i]];
        if (fdr.active) {
          tmp = fdr.country + ","
              + fdr.element + ","
              + fdr.myX + ","
              + fdr.myY + "|";
          changes += tmp;
        }
      }
      System.out.println("Got " +(i-startMods)+ " changes to flag");
      // set mods pointer to top of stack
      setStartMods();
      return changes;
    }


    // draw flag elements into buffer
    // draw only current 'page' of elements

    public synchronized void drawElements(Graphics g) {
            // draw flag elements
           int rowCount = 0;
           int numRows = rowsPerPage;
           if (Fcount < rowsPerPage) {
              numRows = Fcount;
           }
           if (startShowRow == -1) {
              // init start row
              startShowRow = Fcount - numRows;
           }
           //System.out.println("drawElements: startrow=" + startShowRow + " numRows=" + numRows);
           for (FZ=startShowRow; rowCount < numRows && FZ < Fcount; FZ++,rowCount++) {
              // draw object into offscreen buffer
              if (F[zorder[FZ]].active) {
                F[zorder[FZ]].paint(g);
              }
           }
    }
}

