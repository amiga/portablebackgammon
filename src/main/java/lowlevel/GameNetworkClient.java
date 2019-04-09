package lowlevel;
import java.io.*;
import java.net.*;


public class GameNetworkClient implements Runnable {

    public static boolean SENDCLICK_AND_DIEVALUE1 = false;
    public  static boolean SENDCLICK_AND_DIEVALUE2 = false;

    private static boolean SENDCLICK = false;
    private final CustomCanvas canvas;
    private final String ipAddress;

    GameNetworkClient(CustomCanvas canvas, String ipAddress) {
        this.canvas = canvas;
        this.ipAddress =ipAddress;
    }

    public void run() {
        System.out.println("GameNetworkClient thread kicked off.");
        connectToSocket();
    }

    private void connectToSocket() {
        Socket echoSocket = null;
        PrintWriter out = null;
        BufferedReader in = null;
        final int port = 4444;
        log(String.format("connecting to socket: %s:%d", this.ipAddress, port));
        try {
            echoSocket = new Socket(this.ipAddress, port);
            out = new PrintWriter(echoSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(
                echoSocket.getInputStream()));
        } catch (UnknownHostException e) {
            System.err.println("Don't know about host: " + this.ipAddress);
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for "
                + "the connection to: " + this.ipAddress);
            System.exit(1);
        }

        BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
        try {
            canvas.onHumanPlayerConnectedToServer();
            boolean go = true;
            while (go) {
                if (SENDCLICK && CustomCanvas.pointerX != 0 && CustomCanvas.pointerY != 0) {
                    // this tells us to send the click the client just made
                    out.println("CLICK X:" + CustomCanvas.pointerX + " Y:" + CustomCanvas.pointerY);//userInput);
                    System.out.println("CLIENT: receiving echo from server: " + in.readLine());
                    SENDCLICK = false;
                }
                if (SENDCLICK_AND_DIEVALUE1 && CustomCanvas.pointerX != 0 && CustomCanvas.pointerY != 0) {
                    // this tells us to send the click the client just made AND the result of the local die roll
                    out.println("CLICK AND DIE1 @" + CustomCanvas.D1lastDieRoll_toSendOverNetwork + " X:" + CustomCanvas.pointerX + " Y:" + CustomCanvas.pointerY);//userInput);
                    System.out.println("CLIENT: receiving echo from server: " + in.readLine());
                    SENDCLICK_AND_DIEVALUE1 = false;
                } else if (SENDCLICK_AND_DIEVALUE2 && CustomCanvas.pointerX != 0 && CustomCanvas.pointerY != 0) {
                    // this tells us to send the click the client just made AND the result of the local die roll
                    out.println("CLICK AND DIE2 @" + CustomCanvas.D2lastDieRoll_toSendOverNetwork + " X:" + CustomCanvas.pointerX + " Y:" + CustomCanvas.pointerY);//userInput);
                    System.out.println("CLIENT: receiving echo from server: " + in.readLine());
                    SENDCLICK_AND_DIEVALUE2 = false;
                } else {
                    out.println("X:" + CustomCanvas.pointerX + " Y:" + CustomCanvas.pointerY);//userInput);
                    System.out.println("CLIENT: receiving echo from server: " + in.readLine());
                }
                try {
                    Thread.sleep(10);
                } catch (Exception e) {
                }
            }
            out.close();
            in.close();
            stdIn.close();
            echoSocket.close();
            System.out.println("disconnected");
        } catch (Exception e) {
            log("error in socket connection: " + e.getMessage());
        }
    }

    /// HAND GENERATED STUFF BELOW HERE:
    public static void log(String s) {
        System.out.println(s);
    }

    private void _E(Exception e) {
        System.out.println("ERROR::" + e.getMessage());
    }
}


  



