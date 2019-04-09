package lowlevel;
import java.io.*;
import java.net.*;

public class GameNetworkServer implements Runnable {

    private CustomCanvas canvas;
    private static int port = 4444, maxConnections=0;

    public GameNetworkServer(CustomCanvas canvas_)
    {
        canvas=canvas_;
    }

    // Listen for incoming connections and handle them
    public void run() {
        int i = 0;
        try {
            ServerSocket listener = new ServerSocket(port);
            Socket server;

            log("waiting for connections..");
            while ((i++ < maxConnections) || (maxConnections == 0)) {
                server = listener.accept();
                log("connection accepted!");
                Main.setTitle(Main.getTitle() + " Online game in progress. (You are server, client connected to you on port " + port + ")");
                doComms conn_c = new doComms(server, canvas);
                Thread t = new Thread(conn_c);
                t.start();
                this.canvas.startNetworkGame();
            }
        } catch (IOException ioe) {
            System.out.println("IOException on socket listen: " + ioe);
            ioe.printStackTrace();
            System.out.println("THIS INSTANCE WILL NOT ACCEPT CONNECTIONS NOW.");
        }
    }

    /// HAND GENERATED STUFF BELOW HERE:
    public static void log(String s) {System.out.println(s);}
    private void _E(Exception e) {System.out.println("ERROR::"+e.getMessage());}
}

class doComms implements Runnable {
    private Socket server;
    private String line,input;

    CustomCanvas customCanvas;
    doComms(Socket server,CustomCanvas customCanvas_) {
        this.server=server;
        customCanvas=customCanvas_;
    }
    public static boolean click, updateDieRollRemotely;
    public static String D1remoteDieRoll,D2remoteDieRoll;

    public void run () {
        input = "";
        try {
            // Get input from the client
            DataInputStream in = new DataInputStream(server.getInputStream());
            PrintStream out = new PrintStream(server.getOutputStream());

            while ((line = in.readLine()) != null && !line.equals(".")) {
                input = input + line;
                out.println("I got:" + line);
                System.out.println("SERVER: received this from client:" + line);
                D1remoteDieRoll = null;
                D2remoteDieRoll = null;
                if (line.contains("DIE1")) {
                    //MEANS THEY GOT SENT: "CLICK AND DIE1 @"+CustomCanvas.D1lastDieRoll_toSendOverNetwork+" X:"+CustomCanvas.pointerX+" Y:"+CustomCanvas.pointerY);//userInput);
                    D1remoteDieRoll = line.substring(line.indexOf("@") + 1, line.indexOf("@") + 2);
                    System.out.println("D1remoteDieRoll received as: " + D1remoteDieRoll);
                    line = line.substring(17, line.length());
                    click = true;
                    updateDieRollRemotely = true;
                } else if (line.contains("DIE2")) {
                    //MEANS THEY GOT SENT: "CLICK AND DIE2 @"+CustomCanvas.D1lastDieRoll_toSendOverNetwork+" X:"+CustomCanvas.pointerX+" Y:"+CustomCanvas.pointerY);//userInput);
                    D2remoteDieRoll = line.substring(line.indexOf("@") + 1, line.indexOf("@") + 2);
                    System.out.println("D2remoteDieRoll received as: " + D2remoteDieRoll);
                    line = line.substring(17, line.length());
                    click = true;
                    updateDieRollRemotely = true;
                } else if (line.contains("CLICK")) {
                    //MEANS CLIENT HAS SENT "CLICK X:"+CustomCanvas.pointerX+" Y:"+CustomCanvas.pointerY)
                    line = line.substring(6, line.length());
                    click = true;
                }
                line = line.trim();
                System.out.println("parse coords from " + line);
                String X = line.substring(line.indexOf("X:") + 2, line.indexOf(" ")).trim();
                String Y = line.substring(line.indexOf("Y:") + 2, line.length()).trim();
                CustomCanvas.pointerX = Integer.parseInt(X);
                CustomCanvas.pointerY = Integer.parseInt(Y);
                if (click) {
                    customCanvas.mouseClickedX(CustomCanvas.pointerX,
                        CustomCanvas.pointerY, CustomCanvas.LEFT_MOUSE_BUTTON);
                    click = false;
                }
                try {
                    Thread.sleep(10);
                } catch (Exception e){
                    e.printStackTrace();
                }
            }
            server.close();
        } catch (IOException ioe) {
            System.out.println("IOException on socket listen (already a server running on this ip and port???) : " + ioe);//HAPPENS WHEN YOU TRY TO RUN 2 SERVERS ON ONE MACHINE.
        } catch (Exception exception) {
            Utils._E(String.format("GameNetworkServer exception %s", exception));
        }
    }
}
