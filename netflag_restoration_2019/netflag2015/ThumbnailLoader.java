import java.awt.*;
import java.applet.*;

//////////

class ThumbnailLoader extends Thread
{
    ThumbnailPanel tp = null;
    Applet parapp = null;
    boolean runIt = true;
    int[] flagsToShow = null;

    // Pass the list of flag ids that we will load
    // thumbnail images for.
    
    public ThumbnailLoader( ThumbnailPanel tp, int[] ids )
    {
      this.tp = tp;
      this.parapp = tp.nfp.parapp;
      this.flagsToShow = ids;
    }


    // Load thumbnail images for a list of flag ids.
    // Presumably these ids are the flags on the
    // current page of ThumbnailPanel, so we call
    // tp.fillOneImage() to refresh the image on the panel.

    public synchronized void run()
    {
      System.out.println("START Loader Thread ");
      // load all thumbnails
      String imageName = "";
      Image tinyflag = null;
      int flagId = 0;
      for (int i=0; i < flagsToShow.length && runIt; i++) {
        // load image if not already loaded
        flagId = flagsToShow[i];
        if (tp.thumbnails[flagId] == null) {
          imageName = tp.countries[flagId].toLowerCase();
          imageName = imageName.replace(' ','_');   // kill spaces
          imageName = imageName.replace('\'','_');  // kill '
          imageName = "thumbnails/" + imageName + "_tn.gif";
          tinyflag = parapp.getImage(parapp.getDocumentBase(),imageName);
          System.out.println("loading thumbnail image " + imageName);
          if (tp.nfp.FD.waitForImage(tinyflag,tp)) {
            tp.thumbnails[flagId] = tinyflag;
            tp.fillOneImage(flagId);   // refresh image if visible
          }
        }
      }
      System.out.println("EXIT Loader Thread ");
    }


    public void stopIt() {
      runIt = false;
    }
}
