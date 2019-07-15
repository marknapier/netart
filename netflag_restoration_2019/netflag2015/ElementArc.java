import java.awt.*;

class ElementArc extends Element
{
  int d1, d2, arcDiam;
  int startangle;
  int endangle;

  public ElementArc(String name, int x, int y, int d1, int d2, int startangle, int endangle)
	{
    super(name,x,y,d1,d2);
    // assumes arc of 180 degrees or -180 (see Greenland flag)
    // negative angle means counter-clockwise arc, so shift
    // y value up by diameter
    this.d1 = d1;
    this.d2 = d2;
    this.startangle = startangle;
    this.endangle = endangle;
    arcDiam = d1/2;
	}


  public boolean containsPoint(int x, int y)
  {
    if (endangle > 0) {
      if (x > myX && x < myX+d1 && y > myY && y < myY+arcDiam) {
        return true;
      }
    }
    else {
      if (x > myX && x < myX+d1 && y > myY+arcDiam && y < myY+d1) {
        return true;
      }
    }
    return false;
  }


  public void draw(Graphics g)
  {
    if (outline == true) {
      g.drawArc( myX+1, myY+1, d1-1,d2-1, startangle, endangle);
      g.drawArc( myX, myY, d1,d2, startangle, endangle);
    } else {
      g.fillArc( myX, myY, d1,d2, startangle, endangle);
    }
  }

}

