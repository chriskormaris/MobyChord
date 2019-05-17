package threads;

import android.util.Log;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;

//********************************* Polyvios Liosis ************************************//
//********************************* Christos Kormaris **********************************//
//********************************* Dimitris Botonakis *********************************//


public class RequestRouteFromNode extends Thread {

    private String passInfo;

    private String myIPAddress;
    private String askNodeIP;

    public RequestRouteFromNode(String myIPAddress, String askNodeIP,
                                String passInfo) {
            // this node makes the request
            this.myIPAddress = myIPAddress;

            // this nodes will run lookup next
            this.askNodeIP = askNodeIP;

            this.passInfo = passInfo;

    }

    @Override
    public void run() {
        Log.d("Request", "Node at IP: " + myIPAddress + " requesting route from Node at IP: " + askNodeIP);

        Socket requestSocket = null;
        ObjectOutputStream out;

        try {

            requestSocket = new Socket(this.askNodeIP, 3300);

            out = new ObjectOutputStream(requestSocket.getOutputStream());

            out.writeObject(this.passInfo);
            out.flush();

            out.writeObject(this.myIPAddress);
            out.flush();

            Log.d("Request", "Info sent to Ask Node ------> " + this.passInfo);

        } catch (IOException ioException) {
            ioException.printStackTrace();
        } finally {
            try {
                if (requestSocket != null) {
                    requestSocket.close();
                }
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }

}
