import java.net.*;
import java.io.*;
import java.awt.Label;

public class cgiDataPoster    extends Thread {
    String postToURL = "http://www.potatoland.org/cgi-bin/nf_history.pl";
    String postContent = "This is some dummy content\nThis is some dummy content\nThis is some dummy content\nThis is some dummy content\nThis is some dummy content\nThis is some dummy content\nThis is some dummy content\nThis is some dummy content\nThis is some dummy content\nThis is some dummy content\nThis is some dummy content\nThis is some dummy content\nThis is some dummy content\nThis is some dummy content\nThis is some dummy content\nThis is some dummy content\nThis is some dummy content\nThis is some dummy content\nThis is some dummy content\nThis is some dummy content\n";
    Label messageOutputLabel = null;
    String lastMessage = "";

    public cgiDataPoster() {
    }

    public cgiDataPoster(String URL, String pData) {
        setURL(URL);
        postContent = pData;
    }

    public void setURL(String URL) {
        postToURL = URL;
    }

    public void setOutput(Label messageOutputLabel) {
        this.messageOutputLabel = messageOutputLabel;
    }

    public String getMessage() {
        return lastMessage;
    }

    public void showMsg(String message) {
        if (messageOutputLabel == null) {
            System.out.println(message);
        }
        else {
            messageOutputLabel.setText(message);
        }
        lastMessage = message;
    }

    public void setPostData(String data) {
        this.postContent = data;
    }

    public void run() {
        URL u = null;
        StringBuffer CGIresult = new StringBuffer();
        String lastMessage = "";

        try {
            u = new URL(postToURL);
        }
        catch (MalformedURLException me) {
            System.err.println("MalformedURLException: " + me);
        }

        try {
            URLConnection urlConn;
            DataOutputStream dos;
            DataInputStream dis;

            showMsg("Connecting to server...");

            // Establish the URL connection
            urlConn = u.openConnection();
            // Get output from the CGI script
            urlConn.setDoInput(true); // so we can get response from server
            urlConn.setDoOutput(true); // so we can post to server
            // Turn off caching
            urlConn.setUseCaches(false);
            // Specify the content type
            urlConn.setRequestProperty("Content-type", "application/x-www-form-urlencoded");

            System.out.println("Opening url " + u);
            System.out.println("Posting DAta " + postContent);

            showMsg("Sending data to server...");

            // Send the POST data
            dos = new DataOutputStream(urlConn.getOutputStream());
            dos.writeBytes(postContent);
            dos.flush();
            dos.close();

            showMsg("Reading response from server...");

            // Read the results
            String tmpstr;
            dis = new DataInputStream(urlConn.getInputStream());
            while (null != ( (tmpstr = dis.readLine()))) {
                System.out.println("\t" + tmpstr);
                CGIresult.append(tmpstr + "\n");
                lastMessage = tmpstr;
            }
            dis.close();

            // Hold onto last message
            showMsg(lastMessage);
        }
        catch (MalformedURLException me) {
            System.err.println("MalformedURLException: " + me);
        }
        catch (IOException ioe) {
            System.err.println("IOException: " + ioe.getMessage());
        }
        //showMsg(CGIresult.toString());
    }


    public String[] readURL(String urlStr) {
        try {
            URL u = new URL(urlStr);
            return readURL(u);
        }
        catch (MalformedURLException me) {
            System.err.println("MalformedURLException: " + me);
        }
        return null;
    }


    public String[] readURL(URL url) {
        System.out.println("cgidataposter.readURL(): " + url);
        int MAXLINES = 800;
        int counter = 0;
        String[] result = new String[MAXLINES];
        String[] retResult;
        try {
            DataInputStream dis;
            URLConnection uc = url.openConnection();
            uc.connect();
            // Read the results
            dis = new DataInputStream(uc.getInputStream());
            String str;
            while ( (str = dis.readLine()) != null && counter < MAXLINES) {
                result[counter] = str;
                counter++;
            }
            dis.close();
        }
        catch (MalformedURLException me) {
            System.err.println("MalformedURLException: " + me);
        }
        catch (EOFException eofe) {
            System.err.println("EOFException: " + eofe.getMessage());
        }
        catch (IOException ioe) {
            System.err.println("IOException: " + ioe.getMessage());
        }
        // return array of result strings
        System.out.println("return " + counter + " rows");
        retResult = new String[counter];
        System.arraycopy(result, 0, retResult, 0, counter);
        return retResult;
    }

}