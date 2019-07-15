import javax.swing.*;
import java.applet.*;
import java.awt.*;
import java.net.*; // for URL

//============================================
// Display flags in a grid
// click a flag to open it in editor
//============================================

public class NetFlagGrid extends JApplet {
    NetFlagGridPanel netFlagGridPanel;

    public NetFlagGrid() {
    }

    public void init() {
        this.setBackground(Color.darkGray);
        this.getContentPane().setLayout(null);
        netFlagGridPanel = new NetFlagGridPanel(this, 700, 500);
        this.getContentPane().add(netFlagGridPanel);
        netFlagGridPanel.init();
    }

    public void destroy() {
    }

    public void start() {
    }

    public void stop() {
    }

}