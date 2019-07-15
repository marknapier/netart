import java.awt.*;

////////////////////////////////////////////////////

class Element extends Component
{
  int myX=0, myY=0, myW=0, myH=0;
  float xPercent=0F, yPercent=0F;
  protected Color currColor;
  public String myName = "";
  public String country = "";
  public String element = "";
  public String type = "";
  int newx, newy;
  int countryId = 0;
  int elementId = 0;
  boolean canEdit = true;
  boolean active = true;
  boolean outline = false;

  public Element(String name, int x, int y, int w, int h)
	{
    //this.setBounds(x,y,w,h);
    this.myName = name;
    this.myX = x;
    this.myY = y;
    this.myW = w;
    this.myH = h;
    setColor(255,150,50);
	}


  public void setColor(int bR, int bG, int bB)
  {
    this.currColor = new Color(bR,bG,bB);
  }


  public void setXYpercents(float xPercent, float yPercent)
  {
    this.xPercent = xPercent;
    this.yPercent = yPercent;
  }


	public void paint(Graphics g)
  {
    //System.out.println("paint Element/fader");
    g.setColor(currColor);
    draw(g);
  }


  public void draw(Graphics g)
  {
    if (outline == true) {
      g.drawRect(myX+1,myY+1,myW+1,myH+1);
      g.drawRect(myX,myY,myW,myH);
    }
    else {
      //System.out.println("DRAW ELEMENT: x="+myX + " y=" + myY);
      g.fillRect(myX,myY,myW,myH);
      //g.fillRect(0,0,myW,myH);
    }
  }


  public void moveTo(int newx, int newy)
  {
    myX = newx;
    myY = newy;
    //setBounds(myX,myY,myW,myH);
    repaint();
  }


  public void setActive(boolean onoff)
  {
           this.active = onoff;
  }


  public boolean containsPoint(int x, int y)
  {
    if (x > myX && x < myX + myW && y > myY && y < myY + myH) {
      return true;
    }
    return false;
  }

}

