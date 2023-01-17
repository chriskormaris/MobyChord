package gr.aueb.cs.grad.mobychord.threads;

import android.util.Log;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;

//********************************* Polyvios Liosis ************************************//
//********************************* Christos Kormaris **********************************//
//********************************* Dimitris Botonakis *********************************//


public class SendRouteToClient extends Thread {

    private final String myIPAddress;
    private final String clientIP;

    private final String routeInfo;

    public SendRouteToClient(String myIPAddress, String passInfo, String routeInfo) {
        this.myIPAddress = myIPAddress;  // this node sends the route the request

        this.routeInfo = routeInfo;

        this.clientIP = passInfo.split("#")[7];
    }

    @Override
    public void run() {
        Log.d("Request", "Node at IP: " + myIPAddress + " sending route to client Node at IP: " + clientIP);

        Socket requestSocket = null;
        ObjectOutputStream out = null;
        final int clientPort = 6666;

        try {
            requestSocket = new Socket(this.clientIP, clientPort);

            out = new ObjectOutputStream(requestSocket.getOutputStream());

            out.writeObject(this.routeInfo);
            out.flush();

            Log.d("Request", "Route sent to Client ------> " + this.routeInfo);
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            try {
                if (requestSocket != null) {
                    if (out != null) {
                        out.close();
                    }
                    requestSocket.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

}
