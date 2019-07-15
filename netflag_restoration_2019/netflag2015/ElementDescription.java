

public class ElementDescription
{
  public int countryId = 0;
  public int elementId = 0;
  public String countryName = "";
  public String elementName = "";     // redstripe1, whitecircle, etc
  public String elementNameNew = "";    // stripe1, circle, etc
  public String abbrev = "";
  public int x;
  public int y;
  public int w;
  public int h;
  public String color;   // "red" "green" etc.
  public String RGBstring;  // "120,60,0"
  public String type;
  public int r;
  public int g;
  public int b;
  public int var1;
  public int var2;
  public int var3;
  public String keywords;
  public String meaning;
  public int[] polyPoints;
  public int numPoints;

  public ElementDescription(
                  String countryName,
                  String abbrev,
                  String elementName,
                  int x, int y, int w, int h,
                  String type, String color,
                  int r, int g, int b,
                  int var1, int var2, int var3,
                  String keywords)
  {
    this.countryName = countryName;
    this.abbrev = abbrev;
//    this.elementName = (elementName.equals("coatofarms"))? elementName : (color + elementName);
    this.elementName = elementName;
//    this.elementNameNew = elementName;
    this.x = x;
    this.y = y;
    this.w = w;
    this.h = h;
    this.type = type;
    this.color = color;      // index into colors array
    this.r = r;
    this.g = g;
    this.b = b;
    this.var1 = var1;
    this.var2 = var2;
    this.var3 = var3;
    this.keywords = keywords;
    this.meaning = "Loading info ...";
  }

  public void setPolyPoints(int[] polyPoints, int numPoints)
  {
    this.polyPoints = new int[numPoints*2];
//    System.out.println("ED set polypoints()");
    for (int i=0; i<numPoints*2; i+=2) {
      this.polyPoints[i] = polyPoints[i];
      this.polyPoints[i+1] = polyPoints[i+1];
//      System.out.println("\t x=" + polyPoints[i] + " y="+polyPoints[i+1]);
    }
//    System.out.println("\t numpoints=" + numPoints);
    this.numPoints = numPoints;
  }
}

