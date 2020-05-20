package threads;

import android.os.Environment;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Set;

import hashing.Hashing;
import msc_aueb_gr_pol_liosis.mobychord.Memcached;
import route.DownloadRoute;


/**
 * Created by Chris on 1/26/2017.
 */

public class LookUp extends Thread {

    private String passInfo;

    // variables to store what passInfo contains
    private String srcPostalCode;
    private String dstPostalCode;

    private Memcached memcached;
    private int[][] fingertable;
    private int m;
    private int currentNodeID;
    private String currentNodeIP;
    private int forwardNodeID;
    private String forwardNodeIP;
    private Set<Integer> keys;

    private String contentToBeHashed;
    private String routeFilename;
    private String routeInfo = "";

    public LookUp(Memcached memcached, Set<Integer> keys, String passInfo) {

        this.memcached = memcached;
        this.keys = keys;

        this.fingertable = memcached.getFingerTable();
        this.m = memcached.getM();
        this.currentNodeID = Integer.parseInt(memcached.getNodeID());
        Log.d("current_node_ID", currentNodeID + "");
        this.currentNodeIP = memcached.getNodeIP();
        Log.d("current_node_IP", currentNodeIP);
        this.passInfo = passInfo;

    }

    @Override
    public void run() {

        srcPostalCode = passInfo.split("#")[1];
        dstPostalCode = passInfo.split("#")[2];

//        contentToBeHashed = srcPostalCode + "_" + dstPostalCode;
        contentToBeHashed = srcPostalCode + "_" + dstPostalCode;
        Log.d("CONTENT", contentToBeHashed);

        // hashId represents the node where we can find the requested route
        int hashId = Hashing.Hash(contentToBeHashed, (int) Math.pow(2,m));
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
            for (int i = fingertable.length - 1; i >= 0; i--) {

                // IT WORKS
                if(isBetween(currentNodeID, id, fingertable[i][1])) {
                    closest_preceding_finger = fingertable[i][1];
                    break;
                }
                if (isBetween(id, fingertable[i][1], currentNodeID)) {
                    closest_preceding_finger = fingertable[i][1];
                    break;
                }
                if (isBetween(fingertable[i][1], currentNodeID, id)) {
                    closest_preceding_finger = fingertable[i][1];
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
                closest_preceding_finger = fingertable[0][1];
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
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
//                routeInfo = getRouteContentByFileName(routeFilename + ".txt");
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

            String line = "";
            /*
            while ((line = br.readLine()) != null) {
                retrievedText = retrievedText + line;
            }
            */
            // ALTERNATIVE, more efficient
            StringBuilder sb = new StringBuilder(retrievedText);
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            retrievedText = sb.toString();

            br.close();

        } catch (FileNotFoundException e) {
            Log.d("ROUTE_NOT_FOUND", "Route file was not found. Download it from Google!");
        } catch (IOException e) {
            e.printStackTrace();
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
