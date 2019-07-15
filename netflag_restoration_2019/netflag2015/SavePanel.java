import javax.swing.*;

/**
 * Show the save dialog, prompt the user to enter a flag name and
 * comment then save changes.
 *
 * Calls NetFlagPanel.saveFlagHistory() to save flag changes in a
 * separate thread, then waits a few seconds for a response from that thread.
 */
////////////////////////////////////////////////////

class SavePanel
    extends ScrollablePanel {
    JButton okButton;
    JButton cancelButton;
    JLabel question;
    NetFlagPanel nfp;
    String flagFileID = "";
    JTextField domainTextbox;
    JTextField commentTextbox;
    JLabel domainL;
    JLabel commentL;

    public SavePanel(NetFlagPanel nfp) {
        this.nfp = nfp;
        this.setVisible(false);
        this.setBounds(150, 80, 470, 160);

        question = new JLabel("Save your changes?");
        question.setBounds(20, 12, 200, 20);
        this.add(question);

        okButton = new JButton("Ok");
        okButton.setBounds(140, 120, 50, 20);
        this.addClickable(okButton);

        cancelButton = new JButton("Cancel");
        cancelButton.setBounds(220, 120, 90, 20);
        this.addClickable(cancelButton);

        domainL = new JLabel("Title:");
        domainL.setBounds(20, 40, 60, 20);
        this.add(domainL);

        commentL = new JLabel("Comment:");
        commentL.setBounds(20, 70, 60, 20);
        this.add(commentL);

        domainTextbox = new JTextField("");
        domainTextbox.setVisible(true);
        domainTextbox.setBounds(90, 40, 160, 20);
        domainTextbox.setColumns(26);
        this.add(domainTextbox);

        commentTextbox = new JTextField("");
        commentTextbox.setVisible(true);
        commentTextbox.setBounds(90, 70, 340, 20);
        commentTextbox.setColumns(200);
        this.add(commentTextbox);
    }

    public void open() {
        commentTextbox.setEnabled(true);
        domainTextbox.setEnabled(true);
        okButton.setEnabled(true);
        okButton.setVisible(true);
        cancelButton.setVisible(true);
        question.setText("Save your changes?");
        domainTextbox.requestFocus();
        super.open();
    }

    public void handleClick(JComponent c, int x, int y) {
        if (c == okButton) {
            // Start background thread to watch nfp progress
            startIt();
            // disable form to prevent double click
            commentTextbox.setEnabled(false);
            domainTextbox.setEnabled(false);
            okButton.setEnabled(false);
        }
        else if (c == cancelButton) {
            this.setVisible(false);
        }
        nfp.largeFlag.repaint();
    }

    // Save flag changes to server.
    // Check for new progress message every 1/4 second
    // and display on panel.
    //
    // After we save successfully, CGI returns an ID value
    // which we will use for rest of this session.  Future
    // saves in this sesssion will pass this ID to CGI, so
    // we save to same file.

    public synchronized void run() {
        String mesg = "";
        boolean saved = false;
        // Timeout in 10 seconds (5 loops per second)
        int counter = 50;
        // start to save flag changes
        System.out.println("SAVE FLAG WITH domain=" + domainTextbox.getText() + " comment=" + commentTextbox.getText());
        cgiDataPoster saverThread = nfp.saveFlagHistory(flagFileID, domainTextbox.getText(), commentTextbox.getText());
        // check messages
        while (counter > 0) {
            mesg = saverThread.getMessage();
            if (mesg != null && mesg.startsWith("Saved")) {
                System.out.println("  GOT SAVED MESSAGE");
                flagFileID = mesg.substring(mesg.indexOf("ID=") + 3);
                mesg = mesg.substring(0, mesg.indexOf("ID="));
                saved = true;
                counter = 0;
            }
            if (mesg != null && mesg.startsWith("Failed")) {
                System.out.println("  GOT FAILED MESSAGE");
                counter = 0;
            }
            question.setText(mesg);
            try {
                Thread.sleep(200);
            }
            catch (Exception e) {
                System.out.println(e);
            }
            counter--;
        }
        // close the panel whether it saved or not
        System.out.println("SAVED success=" + saved);
        repaint();
        // wait 1.5 sec then hide panel
        try {
            Thread.sleep(1500);
        }
        catch (Exception e) {
            System.out.println(e);
        }
        this.close();
    }

}