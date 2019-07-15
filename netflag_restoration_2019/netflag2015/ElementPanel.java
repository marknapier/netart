import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

////////////////////////////////////////////////////

class ElementPanel
    extends ScrollablePanel {
    JButton okButton = new JButton("Ok");
    JLabel killBox = new JLabel("hide");
    JLabel back = new JLabel("push back");
    JLabel forward = new JLabel("pull forward");
    JLabel moreInfo = new JLabel("view flag");
    JLabel bigger = new JLabel("+");
    JLabel smaller = new JLabel("-");
    Element originalFlagPiece;
    private NetFlagPanel nfp;
    JLabel elementName;

    public ElementPanel(NetFlagPanel nfp, JApplet parapp) {
        this.nfp = nfp;
        this.setVisible(false);
        this.setBounds(200, 80, 130, 200);

        elementName = new JLabel("Flag element:");
        elementName.setBounds(5, 8, 120, 20);
        this.add(elementName);

        back.reshape(10, 40, 80, 20);
        this.addClickable(back);

        forward.reshape(10, 70, 80, 20);
        this.addClickable(forward);

        killBox.reshape(10, 100, 80, 20);
        this.addClickable(killBox);

        moreInfo.reshape(10, 130, 80, 20);
        this.addClickable(moreInfo);

        okButton.reshape(40, 160, 50, 20);
        this.addClickable(okButton);

        /*
              bigger.reshape(100,180,15,15);
              this.addClickable(bigger);
              smaller.reshape(120,180,15,15);
              this.addClickable(smaller);
         */
    }

    public void open(Element f, int x, int y) {
        // hold onto the flag piece we are editing
        originalFlagPiece = f;
        if (x > 600)
            x = 600;
        if (y > 300)
            y = 300;
        this.setLocation(x, y);
        this.setVisible(true);
        elementName.setText(f.myName);
        if (f.canEdit) {
            killBox.enable();
            back.enable();
            forward.enable();
            bigger.enable();
            smaller.enable();
        }
        else {
            killBox.disable();
            back.disable();
            forward.disable();
            bigger.disable();
            smaller.disable();
        }
        repaint();
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        // background
        g.setColor(Color.lightGray);
        g.fillRect(0, 0, (int) D.width - 1, (int) D.height - 1);
        // border
        g.setColor(Color.darkGray);
        g.fillRect(4, 32, 122, 2);
        // line below chokes netscape (getWidth() not found)
        //g.drawRect(0,0,(int)D.getWidth()-1,(int)D.getHeight()-1);
        g.drawRect(0, 0, (int) D.width - 1, (int) D.height - 1);
    }

    public void handleClick(JComponent c, int x, int y) {
        if (c == okButton) {
            this.setVisible(false);
        }
        else if (c == killBox) {
            originalFlagPiece.active = !originalFlagPiece.active;
        }
        else if (c == back) {
            nfp.largeFlag.shiftZ(originalFlagPiece, -1);
        }
        else if (c == forward) {
            nfp.largeFlag.shiftZ(originalFlagPiece, 1);
        }
        else if (c == bigger) {
            originalFlagPiece.myW += 10;
        }
        else if (c == smaller) {
            originalFlagPiece.myW -= 10;
        }
        else if (c == moreInfo) {
            nfp.openFlagInfo(originalFlagPiece.country);
            this.setVisible(false);
        }
        nfp.largeFlag.repaint();
    }
}