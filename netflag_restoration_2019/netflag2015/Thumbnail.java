import javax.swing.*;
import java.awt.*;

/////////////////////////////////////////////////////////
// Class to display one flag thumbnail image
// It's a panel, so can have a custom cursor added to it.

class Thumbnail extends JPanel
{
  Image img = null;
  String text = "";
  int imgw, imgh;

  public Thumbnail(Image img, int x, int y, int imgw, int imgh)
  {
    this.img = img;
    this.imgw = imgw;
    this.imgh = imgh;
    this.setBounds(x,y,imgw,imgh);
  }

  public void setText(String txt) {
    text = txt;
  }

  public String getText() {
    return text;
  }

  public void paintComponent(Graphics g)
  {
    if (img != null) {
      g.drawImage( img, 0,0, imgw,imgh, this );
    }
    else {
      g.setColor(Color.green);
      g.drawRect(0,0, imgw-1,imgh-1);
    }
  }


  public void setImage(Image img)
  {
    this.img = img;
    // NOTE!!!!!
    // If I don't repaint below, the panel will not
    // refresh thumbnail images when scrolling down the list.
    // Seems like the panel thinks the image hasn't changed,
    // so doesn't bother to repaint it (if I hide and show the
    // applet then the thumbnails will repaint correctly).
    // If I call repaint, seems to tell the panel that this
    // component has changed, so will refresh correctly during
    // scroll.
    this.repaint();

  }
}




