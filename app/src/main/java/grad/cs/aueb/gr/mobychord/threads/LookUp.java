package grad.cs.aueb.gr.mobychord.threads;

import static grad.cs.aueb.gr.mobychord.ChordSize.M;

import android.os.Environment;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Set;

import grad.cs.aueb.gr.mobychord.Memcached;
import grad.cs.aueb.gr.mobychord.hashing.Hashing;
import grad.cs.aueb.gr.mobychord.route.DownloadRoute;


/**
 * Created by Chris on 1/26/2017.
 */
public class LookUp extends Thread {

    private final String passInfo;
    private final Memcached memcached;
    private final int[][] fingerTable;
    private final int currentNodeID;
    private final String currentNodeIP;
    private final Set<Integer> keys;
    // variables to store what passInfo contains
    private String sourcePostalCode;
    private String destinationPostalCode;
    private int forwardNodeID;
    private String forwardNodeIP;
    private String contentToBeHashed;
    private String routeFilename;
    private String routeInfo = "";

    public LookUp(Memcached memcached, Set<Integer> keys, String passInfo) {
        this.memcached = memcached;
        this.keys = keys;

        this.fingerTable = memcached.getFingerTable();
        this.currentNodeID = Integer.parseInt(memcached.getNodeID());
        Log.d("current_node_ID", currentNodeID + "");
        this.currentNodeIP = memcached.getNodeIP();
        Log.d("current_node_IP", currentNodeIP);
        this.passInfo = passInfo;
    }

    @Override
    public void run() {
        sourcePostalCode = passInfo.split("#")[1];
        destinationPostalCode = passInfo.split("#")[2];

        // contentToBeHashed = srcPostalCode + "_" + dstPostalCode;
        contentToBeHashed = sourcePostalCode + "_" + destinationPostalCode;
        Log.d("CONTENT", contentToBeHashed);

        // hashId represents the node where we can find the requested route
        int hashId = Hashing.Hash(contentToBeHashed, (int) Math.pow(2, M));
        Log.d("HASH", hashId + "");

        routeFilename = hashId + "_" + contentToBeHashed;

        lookUp(hashId);
    }

    private String getIPFromID(int askNodeId) {
        String askNodeIP = memcached.getRingData()[askNodeId];
        Log.d("askNodeIP", "askNodeIP: " + askNodeIP);
        return askNodeIP;
    }

    private boolean isBetween(int id, int low, int high) {
        return (id > low && id < high);
    }

    // LookUp operation.
    // Returns the closest preceding finger to the key id.
    public void lookUp(int id) {
        int closest_preceding_finger = -1000;

        if (keys.contains(id)) {
            closest_preceding_finger = currentNodeID;
            Log.d("CPF", "closest_preceding_finger: " + closest_preceding_finger);
            Log.d("KEY_FOUND", "Node: " + currentNodeID + " contains the key that was asked.");

            // search for routeFilename in Memcached and then in the hard disk
            routeInfo = searchRouteFile(routeFilename);

            // If routeFilename does not reside in Memcached nor the hard disk
            // download it from Google.
            if (routeInfo.equals("")) {
                downloadRouteFile(routeFilename);
            }

            // Thread finished here. The job is done.

        } else {

            // Iterate the finger table from end to start.
            for (int i = fingerTable.length - 1; i >= 0; i--) {

                // IT WORKS
                if (isBetween(currentNodeID, id, fingerTable[i][1])) {
                    closest_preceding_finger = fingerTable[i][1];
                    break;
                }
                if (isBetween(id, fingerTable[i][1], currentNodeID)) {
                    closest_preceding_finger = fingerTable[i][1];
                    break;
                }
                if (isBetween(fingerTable[i][1], currentNodeID, id)) {
                    closest_preceding_finger = fingerTable[i][1];
                    break;
                }

            }

            // If there is no finger node with node id less than key id
            // then the closest preceding finger is the last finger node
            // in the finger table.
            if (closest_preceding_finger == -1000) {
                // If no closest preceding finger is found,
                // then set the successor as the closest preceding finger.
                // The successor of a Node is always the first Node
                // in its finger table.
                closest_preceding_finger = fingerTable[0][1];
            }

            forwardNodeID = closest_preceding_finger;
            forwardNodeIP = getIPFromID(forwardNodeID);
            Log.d("CPF", "closest_preceding_finger: " + closest_preceding_finger);
            Log.d("CPF_IP", "closest_preceding_finger IP: " + forwardNodeIP);

            forwardLookUp();
        }
    }

    private String searchRouteFile(String routeFilename) {
        // get file from Memcached if exists
        Log.d("searchRoute", "checking if file exists in memcached...");
        routeInfo = memcached.requestFile(routeFilename + ".json");

        // if route is not in memcached
        if (routeInfo.equals("")) {
            Log.d("searchRoute", "checking if file exists in disk...");

            // get file content by filename from disk
            routeInfo = getRouteContentByFileName(routeFilename + ".json");
            Log.d("routeInfo", routeInfo);

            // add routeInfo to cache
            memcached.addFile(routeFilename + ".json", routeInfo);
        }

        return routeInfo;
    }

    private void downloadRouteFile(String routeFilename) {
        // if file not in disk
        if (routeInfo.equals("")) {
            // download route file from Google
            double srcLatitude = Double.parseDouble(passInfo.split("#")[3]);
            double srcLongitude = Double.parseDouble(passInfo.split("#")[4]);
            LatLng srcLocation = new LatLng(srcLatitude, srcLongitude);

            double dstLatitude = Double.parseDouble(passInfo.split("#")[5]);
            double dstLongitude = Double.parseDouble(passInfo.split("#")[6]);
            LatLng dstLocation = new LatLng(dstLatitude, dstLongitude);

            // the new route is downloaded saved to file.
            DownloadRoute downloadNewRouteThread
                    = new DownloadRoute(routeFilename, srcLocation, dstLocation);
            downloadNewRouteThread.start();
            try {
                downloadNewRouteThread.join();
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
            // routeInfo = getRouteContentByFileName(routeFilename + ".txt");
            routeInfo = downloadNewRouteThread.getJsonRoute();
            Log.d("routeInfo", routeInfo);

            // add routeInfo to cache
            memcached.addFile(routeFilename + ".json", routeInfo);
        }
    }

    private String getRouteContentByFileName(String routeFilename) {
        String pathToRoute = Environment.getExternalStorageDirectory()
                + File.separator + "mobyChord/Node/Keys/"
                + routeFilename;

        File file = new File(pathToRoute);

        String retrievedText = "";

        try {
            BufferedReader br = new BufferedReader(new FileReader(file));

            String line;

            StringBuilder stringBuilder = new StringBuilder(retrievedText);
            while ((line = br.readLine()) != null) {
                stringBuilder.append(line);
            }
            retrievedText = stringBuilder.toString();

            br.close();
        } catch (FileNotFoundException ex) {
            Log.d("ROUTE_NOT_FOUND", "Route file was not found. Download it from Google!");
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        return retrievedText;
    }

    private void forwardLookUp() {
        Thread requestRouteFromNodeThread = new Thread(new RequestRouteFromNode(currentNodeIP, forwardNodeIP, passInfo));
        requestRouteFromNodeThread.start();
    }

    public String getRouteInfo() {
        return this.routeInfo;
    }

}
