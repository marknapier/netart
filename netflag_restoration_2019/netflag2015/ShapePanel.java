import javax.swing.*;
import java.awt.*;

//
// Panel displays flag shapes (star, bar, stripe, etc)
// Listener at bottom handles clicks
//

class ShapePanel extends ScrollablePanel
{
  // flag information and imagemap panel
  private NetFlagPanel nfp;
  private FlagBuilder fd;
  Image smallFlagBuffer = null;
  Flag flag = null;
  JApplet parapp;
  Element starV, starZ, star1, star7, star14, circle, circle1;
  Element cross, cross1, cross2, cross3, arc, arc1, triangle;
  Element rect, bar, bar1, bar2, stripe, stripe1, stripe2, stripe3, stripe4, coat;
  Element field, x1, x2;
  boolean inited = false;


  public ShapePanel(NetFlagPanel nfp, JApplet parapp)
  {
    this.nfp = nfp;
    this.parapp = parapp;
    this.setVisible(false);
    this.setBounds(50,50,300,400);
  }


  public void init()
  {
    // create a flag factory for small flag
    this.fd = new FlagBuilder(0,0,90,60,parapp);

    // Don't add() elements to panel
    // and don't call super.paint() in paint() BECAUSE
    // we're manually painting the elements into the panel
    // BECAUSE elements x,y coords are relative to flag area,
    // not relative to the element upper left corner
    // as expected by Container paint() method.

    // Stars

    star7 = fd.makeFlagElement("Australia","bigstar");
    star7.myName = "7 point star";
    star7.moveTo(20,20);

    star14 = fd.makeFlagElement("Malaysia","star");
    star14.myName = "14 point star";
    star14.moveTo(40,20);

    star1 = fd.makeFlagElement("Senegal","star");
    star1.myName = "medium star";
    star1.moveTo(60,20);

    starZ = fd.makeFlagElement("Zimbabwe","star");
    starZ.myName = "medium star fat";
    starZ.moveTo(90,20);

    starV = fd.makeFlagElement("Vietnam","star");
    starV.myName = "large star";
    starV.moveTo(120,20);

    // vertical stripes

    bar2 = fd.makeFlagElement("Algeria","greenstripe");
    bar2.myName = "1/2 bar";
    bar2.moveTo(20,70);

    bar = fd.makeFlagElement("Italy","greenstripe");
    bar.myName = "1/3 bar";
    bar.moveTo(80,70);

    bar1 = fd.makeFlagElement("Canada","redstripe1");
    bar1.myName = "1/4 bar";
    bar1.moveTo(130,70);

    // horizontal stripes

//    stripe = fd.makeFlagElement("United States","redstripe1");
    stripe = fd.makeFlagElement("Greece","bluestripe1");
    stripe.myName = "thin stripe";
    stripe.moveTo(180,20);

//    stripe3 = fd.makeFlagElement("Puerto Rico","redstripe1");
    stripe3 = fd.makeFlagElement("Uganda","blackstripe1");
    stripe3.myName = "1/5 stripe";
    stripe3.moveTo(180,38);

    stripe4 = fd.makeFlagElement("Spain","redstripe1");
    stripe4.myName = "1/4 stripe";
    stripe4.moveTo(180,60);

    stripe1 = fd.makeFlagElement("Estonia","bluestripe");
    stripe1.myName = "1/3 stripe";
    stripe1.moveTo(180,90);

    stripe2 = fd.makeFlagElement("Greenland","whitefield");
    stripe2.myName = "1/2 stripe";
    stripe2.moveTo(180,130);

    // full field

    field = fd.makeFlagElement("Somalia","bluefield");
    field.myName = "full flag";
    field.moveTo(180,180);

    // Right-angle crosses

    cross = fd.makeFlagElement("Norway","bluecross");
    cross.myName = "crosses";
    cross.moveTo(20,150);

    cross1 = fd.makeFlagElement("Norway","whitecross");
    cross1.myName = "crosses";
    cross1.moveTo(30,160);

//    cross3 = fd.makeFlagElement("United Kingdom","redcross");
//    cross3.myName = "center cross thin";
//    cross3.moveTo(120,180);

//    cross2 = fd.makeFlagElement("United Kingdom","whitecross");
//    cross2.myName = "center cross fat";
//    cross2.moveTo(130,190);

    // diagonal cross and polygons

    x1 = fd.makeFlagElement("United Kingdom","whitex");
    x1.myName = "diagonals";
    x1.moveTo(20,230);

//    x2 = fd.makeFlagElement("United Kingdom","redx");
//    x2.myName = "thin X";
//    x2.moveTo(120,100);

    // Circles

    circle = fd.makeFlagElement("Japan","circle");
    circle.myName = "circle";
    circle.moveTo(20,310);

//    circle1 = fd.makeFlagElement("Laos","circle");
//    circle1.myName = "medium circle";
//    circle1.moveTo(60,310);

    // Arcs

    arc = fd.makeFlagElement("Greenland","redarc");
    arc.myName = "arc (upper)";
    arc.moveTo(80,310);

    arc1 = fd.makeFlagElement("Greenland","whitearc");
    arc1.myName = "arc (lower)";
    arc1.moveTo(110,310);

    // Misc. rectangles

    rect = fd.makeFlagElement("United States","bluefield");
    rect.myName = "misc rectangles";
    rect.moveTo(230,260);

    // Coats of arms, images

    coat = fd.makeFlagElement("Croatia","coatofarms");
    coat.myName = "coat of arms";
    coat.moveTo(180,290);

    // Trianlges

    triangle = fd.makeFlagElement("Puerto Rico","bluefield");
    triangle.myName = "triangle";
    triangle.moveTo(220,310);

//    this.setVisible(true);
//    D = this.getSize();

    flag = new Flag(/*nfp*/);
    flag.bgColor = Color.lightGray;
    flag.setBounds(1,1,(int)D.width-2,(int)D.height-2);
    // listen for clicks on flag
//  flag.addMouseListener(new ShapeListener(flag,nfp));
//    flag.addMouseListener(new ShapeListener(this,flag));
    flag.addMouseMotionListener(new MListener(flag,nfp));
    flag.setCursor(new Cursor(Cursor.HAND_CURSOR));
    flag.rowsPerPage = 50;
    flag.add(starV);
    flag.add(starZ);
    flag.add(star1);
    flag.add(star7);
    flag.add(star14);
    flag.add(circle);
//    flag.add(circle1);
    flag.add(bar);
    flag.add(bar1);
    flag.add(bar2);
    flag.add(stripe);
    flag.add(stripe1);
    flag.add(stripe2);
    flag.add(stripe3);
    flag.add(stripe4);
    flag.add(field);
    flag.add(rect);
    flag.add(cross1);
    flag.add(cross);
//    flag.add(cross2);
//    flag.add(cross3);
    flag.add(x1);
//    flag.add(x2);
    flag.add(coat);
    flag.add(arc);
    flag.add(arc1);
    flag.add(triangle);
//    this.add(flag);
    this.addClickable(flag);

    // repaint flag or panel does not repaint fully
    flag.repaint();

    inited = true;
  }


  public void open()
  {
    this.setVisible(true);
    repaint();
    if (!inited) {
      init();
    }
    repaint();
  }


  public void showThumbnails(int[] flagIdList)
  {
    nfp.openList(flagIdList);
  }


  public void handleClick(JComponent c, int x, int y)
  {
    if (c == flag) {
      Element f = flag.processMouseClick(x,y);
      if (f != null) {
        int[] flagIds = FlagDescription.findShape(f.type);
        showThumbnails(flagIds);
      }
    }
    else {
      System.out.println("Flaginfo click on nada");
    }
  }


}




