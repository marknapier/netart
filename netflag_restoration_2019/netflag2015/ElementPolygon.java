import java.awt.*;

////////////////////////////////////////////////////

class ElementPolygon extends Element
{
   Polygon p;
   Polygon p1;
   Rectangle r;

  // make new constructor (Polygon p)
  public ElementPolygon(String name, Polygon poly, Polygon poly1)
	{
            super(name,  0, 0, 0, 0);
            r = poly.getBounds();
/* Works in 1.2
            x =  (int)r.getX();
            y =  (int)r.getY();
            h =  (int)r.getHeight();
            w =  (int)r.getWidth();
*/
/* this works in 1.1 and 1.2 */
            myX =  (int)r.x;
            myY =  (int)r.y;
            myH =  (int)r.height;
            myW =  (int)r.width;
            this.p = poly;
            this.p1 = poly1;
            //System.out.println("POLY:name=" + name + " x=" + myX + " y="+myY+ " w="+myW+ " h="+myH);
	}

  public void draw(Graphics g)
  {
              if (outline == true) {
                g.drawPolygon(p);
                g.drawPolygon(p1);
              } else {
                g.fillPolygon(p);
              }
  }

  public void moveTo(int newx, int newy)
  {
    p.translate(newx-myX,newy-myY);
    // Poly below is to make outline appear two pixels thick
    // but causes trouble, so nixing it.
    //p1.translate(newx-myX,newy-myY);
    super.moveTo(newx,newy);
  }

  public boolean containsPoint(int x, int y)
  {
    return (p.contains(x,y));
  }

}

