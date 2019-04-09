package lowlevel;

import gamelogic.Board;
import java.io.BufferedReader;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
//import java.net.UnknownHostException;
import java.net.MalformedURLException;
import java.io.IOException;
import java.io.DataInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.util.Vector;


public class NetworkChatClient implements Runnable {

    GameNetworkServer ss;
    String IP;
    public static String news="No news downloaded.";

    public static String topic ="Welcome to the Backgammon chat lobby! Click a user to play.";
    private static final String SERVLET_PATH = "/servlet/GammonChatServlet"; //"/servlet/sunexamples.#lobbyServlet";

    public static String nick="";
    public static Vector userList = new Vector();

    //    Label userInfo;
    //    Label messageInfo;
    //    Label listInfo;
    //    Button sendButton;
    //    TextField userText;
    //    TextArea  messageText;
    //    List userList;
    URL chatURL;
    URL servletURL;
    URLConnection connect;
    volatile private boolean loggedin = false;
    String username="ENTERANAME";
    Thread pollingThread = null;
    public static Vector messageText = new Vector();
    public static String theURL = "http://localhost:8080/chat2/"; // http://114.45.186.132:8080/chat2/GammonChatApplet.html";;//http://114.45.186.132/chat2/";//http://localhost:8080/chat2/";
    public static String localURL = "http://localhost:8080/chat2/";

    private CustomCanvas customCanvas;

    public NetworkChatClient(CustomCanvas customCanvas_) {
        System.out.println("NetworkChatClient born.");
        customCanvas = customCanvas_;
        for(int i=0; i < 16; i++) {
            messageText.add("A");
        }
        IP = "127.0.0.1";
        init();
        start();
        ss = new GameNetworkServer(customCanvas);
        Thread t = new Thread(ss);
        t.start();
    }

    public void log(String s) {
        System.out.println(s);
    }

    /**
     *  Return list of applet parameters (currently null).
     */
    public String[][] getParameterInfo() {
        return null;
    }

    /**
     *  Return string describing applet.
     */
    public String getAppletInfo() {
            return "----";
    }

    public synchronized void init() {
        if (pollingThread != null)
            return;  //Netscape calls init on any excuse
        try {
            // Set base URL
            chatURL = new URL(theURL);//getCodeBase();
        } catch (Exception e) {
            System.out.println("ERROR MAKING URL. " + e.getMessage());
        }

        // Setup and start polling thread.
        int currPriority = Thread.currentThread().getPriority();
        int newPriority = currPriority == Thread.MIN_PRIORITY ? Thread.MIN_PRIORITY : currPriority - 1;
        pollingThread = new Thread(this);//,"#lobbyPolling");
        pollingThread.setDaemon(true);
        pollingThread.setPriority(newPriority);
        pollingThread.start();
        p("starting poll");

    }

    /**
     *  Start polling thread if not already running.
     */
    public synchronized void start() {
        if (!isLoggedin() && username != null) {
            login();
        }
        if (pollingThread != null && pollingThread.isAlive()) {
            pollingThread.resume();
            p("#lobby: resuming poll");
        } else {
            p1("#lobby: No polling thread!");
            pollingThread = new Thread(this);//,"#lobbyPolling");
            pollingThread.setDaemon(true);
            pollingThread.start();
            p("#lobby: starting poll");
        }
    }

    /**
     *
     */
    public synchronized void stop() {
        if (pollingThread.isAlive()) {
            pollingThread.suspend();
            p("#lobby: suspending poll");
        } else {
            p1("Easy Chat: Dead polling thread in stop()!");
        }
        logout();
    }

    public synchronized void destroy() {
        if (pollingThread != null && pollingThread.isAlive()) {
            pollingThread.stop();
            pollingThread = null; //just in case we come back
            p("#lobby: stopping poll");
        }
        logout();
    }

    public static boolean KEEP_LOBBY_GOING=true;//lobby gets killed once sockets connect for private game
    public void run() {

        p("#lobby:  starting the polling run");
        while (KEEP_LOBBY_GOING){//!Thread.interrupted()) {
            if (isLoggedin()) {
                pollList();
                poll();
                p("#lobby: polling");
            } else {
                pollList();
                p("#lobby: not loggedin for poll");
            }
            try {
                Thread.sleep(1000 * 3); // Sleep for 3 seconds
            } catch (InterruptedException e) {}
        }
        p("#lobby: exiting run()");
    }


    private void login() {
        nick=nick.trim();
        nick=nick.replace("?","");
        nick+="@"+IP;
        username=nick.trim();
        System.out.println("login()"+username);
        if (username == null) return;

        if (username.contains("Admin") || username.contains("admin") )//no one can enter their name as Admin
        {
            System.out.println("dont copy me twat");
            username="noobstick";
            return;
        }
        // this is a password
        if (username.equals("gammonosa")){// if they enter the password they begin Admin
            username="Admin";
        }

        String queryString = SERVLET_PATH + "?mode=newuser&user="+URLEncoder.encode(username)+"&ip="+URLEncoder.encode(IP);
        p("Attempting login as "+username+" with IP:"+IP);
        try {
            connect = (new URL(chatURL,queryString)).openConnection();
            connect.setDefaultUseCaches(false); //for future connections
            connect.setUseCaches(false); //for this one
            connect.setDoInput(true);
            connect.setDoOutput(false);
            connect.connect();
            p("Made connection to "+connect);
            DataInputStream in = new DataInputStream(connect.getInputStream());
            String response = in.readLine();
            if (response.startsWith("+")) {
                setLoggedin(true);
                showStatus("Logged into #lobby as user " + username);
                p("Logged in as "+username);
                /////userInfo.setText("Type message: ");
                /////repaint();
            } else {
                showStatus("Error logging in" + response);
                p("Could not log in as "+username);
                System.err.println("Error logging in" +response);
            }
        } catch (MalformedURLException e2) {
            System.err.println("MalformedURLException logging in!");
            e2.printStackTrace(System.err);
            showStatus("Error logging in");
        } catch (IOException e1) {
            System.err.println("IOException logging in!");
            e1.printStackTrace(System.err);
            showStatus("Error logging in");
        }
    }

    private void showStatus(String s)
    {
        System.out.println("showStatus: "+s);
        messageText.add("ChanServ: "+s);
    }

    private void logout() {
        if (!isLoggedin() || username == null) return;
        String queryString = SERVLET_PATH + "?mode=dropuser&user=" + URLEncoder.encode(username);
        try {
            connect = (new URL(chatURL, queryString)).openConnection();
            connect.setUseCaches(false); //for this one
            connect.setDoInput(true);
            connect.setDoOutput(false);
            connect.connect();
            DataInputStream in = new DataInputStream(connect.getInputStream());
            String response = in.readLine();
            if (response.startsWith("+")) {
                setLoggedin(false);
                showStatus("User " + username + " Logged out of #lobby");
            } else {
                showStatus("Error logging out" + response);
                System.err.println("Error logging out" + response);
            }
        } catch (MalformedURLException e2) {
            System.err.println("MalformedURLException logging in!");
            e2.printStackTrace(System.err);
            showStatus("Error logging out");
        } catch (IOException e1) {
            System.err.println("IOException logging in!");
            e1.printStackTrace(System.err);
            showStatus("Error logging out");
        }
    }

    public void send() {
        if (!isLoggedin()) {
            System.out.println("CANT SEND, NOT LOGGED IN.");
            return;
        }
        String message = CustomCanvas.chatText;//"ARSEEEEE";//userText.getText();
        message=message.trim();//message.replace("/n","?");
        System.out.println("send this:"+CustomCanvas.chatText);
        if (message.equals(""))
            return;
	    String queryString = SERVLET_PATH + "?mode=send&user="+URLEncoder.encode(username);
        queryString = queryString +"&message="+URLEncoder.encode(message);
        try {
            connect = (new URL(chatURL,queryString)).openConnection();
            connect.setUseCaches(false); //for this one
            connect.setDoInput(true);
            connect.setDoOutput(false);
            connect.connect();
            DataInputStream in = new DataInputStream(connect.getInputStream());
            String response = in.readLine();
            if (response.startsWith("+")) {
               // showStatus("Message sent");
            } else {
                showStatus("Error sending message" + response);
                System.err.println("Error sending message" + response);
            }
        } catch (MalformedURLException e2) {
            System.err.println("MalformedURLException logging in!");
            e2.printStackTrace(System.err);
            showStatus("Error logging out");
        } catch (IOException e1) {
            System.err.println("IOException logging in!");
            e1.printStackTrace(System.err);
            showStatus("Error logging out");
        }
    }


    private void poll() {
	    String queryString = SERVLET_PATH + "?mode=poll&user="+URLEncoder.encode(username);
        try {
            DataInputStream in = new DataInputStream(new URL(chatURL,queryString).openStream());
            String nextLine = in.readLine();
            if (!nextLine.startsWith("+")) {
                showStatus("Error getting messages from server");
                System.err.println("Error getting messages from server");
                return;
            }
            nextLine = in.readLine();
            while (nextLine != null && !nextLine.equals(".")) {
                nextLine=nextLine.trim();
                nick=nick.trim();
                System.err.println(nextLine);
                messageText.add(nextLine);
                System.err.println("Text found: "+nextLine);

                if (nextLine.contains("COMP VS COMP")) {
                    System.out.println("RECEIVED PLAY");
                    Bot.setFullAutoPlay(true);
                    Bot.dead=false;
                    this.customCanvas.startGame();

                }
               if (nextLine.contains("ZZZ "+nick)) {
                   System.out.println("RECEIVED PLAY");
                   //Bot.FULL_AUTO_PLAY=true;
                   //Board.NOT_A_BOT_BUT_A_NETWORKED_PLAYER=true;
                 //  Bot.NETWORK_OPPONENT=true;
                   Bot.dead=false;
                   this.customCanvas.startGame();
                   Board.setBotDestination(20,20,"TEST");

               }
               if (nextLine.contains("ZZZ ROLL")) {
                   System.out.println("RECEIVED ROLL");
                   //Bot.FULL_AUTO_PLAY=true;
                   //Board.NOT_A_BOT_BUT_A_NETWORKED_PLAYER=true;
                 //  Bot.NETWORK_OPPONENT=true;
                   Bot.dead=false;
                   this.customCanvas.startGame();
                   //Board.setBotDestination(20,20,"TEST");
               } else {
                   System.out.println("no nextline was ["+nextLine+"] NOT [PLAY YOU "+nick+"]");
               }

                nextLine = in.readLine();
            }
        } catch (IOException e) {
            System.err.println("IOException polling");
            e.printStackTrace(System.err);
            showStatus("Error Checking Messages");
        }

    }

    /**
     *  Poll servlet, asking for list of names, add names to gui list.
     */
    private void pollList() {
        String queryString = SERVLET_PATH + "?mode=list";
        Vector users = new Vector();

        try {
            System.out.println("chatURL: " + chatURL);
            System.out.println("queryString: " + queryString);
            URL listURL = new URL(chatURL, queryString);
            System.out.println("listURL: " + listURL);
            URLConnection listConn = listURL.openConnection();
            listConn.setDefaultUseCaches(false);
            listConn.setUseCaches(false);
            listConn.connect();
            DataInputStream in = new DataInputStream(listConn.getInputStream());
            String nextLine = in.readLine();
            if (!nextLine.startsWith("+")) {
                showStatus("Error getting userlist from server");
                p1("Error getting userlist from server");
                return;
            }
            nextLine = in.readLine();
            while (nextLine != null && !nextLine.equals(".")) {
                p("Read user: " + nextLine);

                users.addElement(nextLine);
                nextLine = in.readLine();
            }
            if (!users.isEmpty()) {
                userList = new Vector();//////userList.clear();
                userList.add("ChanServ");
                int size = users.size();
                for (int i = 0; i < size; i++) {
                    userList.add((String) users.elementAt(i));//userList.addItem((String)users.elementAt(i));
                }
            } else {
                userList = new Vector();/////userList.clear();
                userList.add("Empty");////userList.addItem("Noone logged in");
            }
            /////repaint();

        } catch (IOException e) {
            System.err.println("IOException polling");
            e.printStackTrace(System.err);
            showStatus("Error Checking Messages");
        }
    }

    /**
     * Check if user is logged in.
     */
    public boolean isLoggedin() {
        return loggedin;
    }

    /**
     * Set loggedin state.
     */
    protected void setLoggedin(boolean newval) {
        loggedin = newval;
    }

    private void p(String debug) {
        System.err.println("#lobby:"+debug);
    }

    private void p1(String debug) {
        System.err.println("#lobby:"+debug);
    }


    public String readStringFromWeb(String url) {
        String full = "";
        String inputLine = "";
        try {
            URL yahoo = new URL(url); //"http://www.alphasoftware.org/backgammon/news.txt");
            URLConnection yc = yahoo.openConnection();
            BufferedReader in = new BufferedReader(
                new InputStreamReader(
                    yc.getInputStream()));


            while ((inputLine = in.readLine()) != null) {
                System.out.println(inputLine);
                if (inputLine != null)
                    full += inputLine;
            }
            in.close();
        } catch (Exception e) {
            System.out.println("exception in getting news " + e.getMessage());
        }
        System.out.println("return news:" + full);
        return full;
    }
}
