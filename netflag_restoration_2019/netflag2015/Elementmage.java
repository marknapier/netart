import java.awt.image.*;
import java.awt.*;


////////////////////////////////////////////////////

class ElementImage extends Element
{
  private Image img = null;     // test image
  int counter=2;
  int imgWidth = 0;
  double hRatio = 0.0;

  public ElementImage(String name, int x, int y, int w, int h, Image img)
	{
    super(name,x,y,w,h);
    this.img = img;
        int imgW=-1, imgH=-1;
        while ( (imgW=img.getWidth(this)) < 0 )
    	    try { Thread.sleep(100); } catch( InterruptedException e ) {System.out.println(e);}
        while ( (imgH=img.getHeight(this)) < 0 )
    	    try { Thread.sleep(100); } catch( InterruptedException e ) {System.out.println(e);}
        //System.out.println("TOOLBAR IMGW=" + imgW + " IMGH=" + imgH );
        imgWidth = imgW;
        hRatio = (double)imgH/(double)imgW;
	}

  public void setimage(Image img)
	{
    this.img = img;
	}

  public void draw(Graphics g)           // fade one step
  {
    if ( active ) {
//      g.drawImage(img,myX,myY,myW,myH,this);
      g.drawImage(img,myX,myY,myW,(int)(myW*hRatio),this);
    }
  }
}

