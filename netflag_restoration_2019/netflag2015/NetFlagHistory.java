import javax.swing.*;
import java.applet.*;
import java.awt.*;

//============================================
//============================================

public class NetFlagHistory extends JApplet
{

  private NetFlagPanel netFlag;

	public  NetFlagHistory()
	{
	}

	public void init()
	{
    this.setBackground(Color.darkGray);
    this.getContentPane().setLayout(null);
    netFlag = new NetFlagPanel(this,750,500);
    this.getContentPane().add(netFlag);
    netFlag.setHistoryMode(true);
    netFlag.init();
  }

	public void destroy()
	{
	}

	public void start()
	{
    System.out.println("start()");
    //netFlag.startAnimation();
	}

	public void stop()
  {
    System.out.println("stop()");
    //netFlag.stopAnimation();
	}

/*
  // for javascript forms interface (see subdir: testelement)
  public void testElement(String country, String abbrev, String elname, int x, int y, int w, int h, String type, String colorname, int r, int g, int b, int var1, int var2, int var3, String meaning, String polypoints)
  {
    netFlag.testElement(country, abbrev, elname, x, y, w, h, type, colorname, r, g, b, var1, var2, var3, meaning, polypoints);
  }

  public void testClear()
  {
    netFlag.testClear();
  }
*/

}


