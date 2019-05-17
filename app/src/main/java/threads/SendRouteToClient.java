package threads;

import android.util.Log;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;

//********************************* Polyvios Liosis ************************************//
//********************************* Christos Kormaris **********************************//
//********************************* Dimitris Botonakis *********************************//


public class SendRouteToClient extends Thread {

    private String myIPAddress;
    private String clientIP;

    private String routeInfo;

    public SendRouteToClient(String myIPAddress,
                                     String passInfo, String routeInfo) {
            this.myIPAddress = myIPAddress; // this node sends the route the request

            this.routeInfo = routeInfo;

            this.clientIP = passInfo.split("#")[7];

    }

    @Override
    public void run() {
        Log.d("Request", "Node at IP: " + myIPAddress + " sending route to client Node at IP: " + clientIP);

        Socket requestSocket = null;
        ObjectOutputStream out;
        final int clientPort = 6666;

        try {

            requestSocket = new Socket(this.clientIP, clientPort);

            out = new ObjectOutputStream(requestSocket.getOutputStream());

            out.writeObject(this.routeInfo);
            out.flush();

            Log.d("Request", "Route sent to Client ------> " + this.routeInfo);

        } catch (IOException ioException) {
            ioException.printStackTrace();
        } finally {
            try {
                if (requestSocket != null) {
//                    out.close();
                    requestSocket.close();
                }
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }

}
