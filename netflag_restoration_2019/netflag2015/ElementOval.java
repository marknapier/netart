import java.awt.*;

////////////////////////////////////////////////////

class ElementOval extends Element
{
  int d;

  public ElementOval(String name, int x, int y, int d)
	{
            super(name,x,y,d,d);
            this.d = d;
	}


  public void draw(Graphics g)
  {
              if (outline == true) {
                g.drawOval( myX+1, myY+1, d-1, d-1);
                g.drawOval( myX, myY, d, d);
              } else {
                g.fillOval( myX, myY, d, d);
              }
  }

}

