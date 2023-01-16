package grad.cs.aueb.gr.mobychord;

import static grad.cs.aueb.gr.mobychord.ChordSize.M;

import android.util.Log;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

/**
 * Created by polyvios on 30/11/2016.
 */

//********************************* Dimitris Botonakis *********************************//
//********************************* Chris kormaris *************************************//
//********************************* Polyvios Liosis ************************************//

public class Memcached {

    //**********Memcached parameters**********
    private static final int MEMCACHED_SIZE = 3;
    private final String[] chordNodes = new String[(int) Math.pow(2, M)];
    private final int[][] fingerTable = new int[M][2];
    private final Vector<String> cachedFiles = new Vector<>();
    private final Vector<String> cachedInfo = new Vector<>();
    public Map<String, Integer> fileFrequencies = new HashMap<>();
    //**********Chord Ring variables**********
    private int nodeID;
    private String nodeIP;
    private int predecessorID;
    private String predecessorIP;
    private int successorID;
    private String successorIP;

    //**********Constructors**********
    public Memcached() {
        this.initializeChordRing();
    }

    // if file was just downloaded
    public void addFile(String filename, String routeinfo) {

        Log.d("Memcached", "addFile function started");

        // Store up to 3 routes inside the memcached.
        // If a 4th route is about to be added,
        // remove the least frequently used (LFU) route.
        // If there are more than one LFU routes,
        // remove the oldest one.
        if (fileFrequencies.size() <= MEMCACHED_SIZE) {
            Log.d("addFile", "memcached has less than 3 routes");

            cachedFiles.add(filename);
            cachedInfo.add(routeinfo);
            fileFrequencies.put(filename, 1);
        } else {
            Log.d("addFile", "memcached has more than 3 routes. Removing oldest route...");

            String minFilename = "";
            int minFrequency = 1000;
            int minPosition = -1;
            int i = 0;
            for (Map.Entry<String, Integer> entry : fileFrequencies.entrySet()) {
                if (entry.getValue() < minFrequency) {
                    minFrequency = entry.getValue();
                    minFilename = entry.getKey();
                    minPosition = i;
                }
                i++;
            }

            cachedFiles.remove(minPosition);
            cachedInfo.remove(minPosition);
            fileFrequencies.remove(minFilename);

            fileFrequencies.put(filename, 1);
        }

        printMemcached();
    }

    public void printMemcached() {
        for (Map.Entry<String, Integer> entry : fileFrequencies.entrySet()) {
            Log.d("Memcached", "filename: " + entry.getKey() + ", frequency: " + entry.getValue());
        }
    }

    public String requestFile(String filename) {
        Log.d("Memcached", "requestFile function started");

        int i = 0;
        for (Map.Entry<String, Integer> entry : fileFrequencies.entrySet()) {
            if (entry.getKey().equals(filename)) {
                Log.d("requestFile", "Route found in memcached.");
                entry.setValue(entry.getValue() + 1);
                printMemcached();
                return cachedInfo.get(i);
            }
            i++;
        }
        return "";
    }

    //Initialize the IPs of chord Ring's Nodes
    public void initializeChordRing() {
        Arrays.fill(this.chordNodes, "0.0.0.0");
    }

    public void computeFingerTable() {
        for (int i = 0; i < M; i++) {
            Log.d("Memcached ", " " + i);
            fingerTable[i][0] = (int) (this.getNodeID() + (Math.pow(2, i))) % (int) Math.pow(2, M);
            double x = Math.pow(2, i);
            Log.d("Memcached ", Integer.valueOf(this.getNodeID()) + " " + x + "  :  " + fingerTable[i][0]);
        }
    }

    public void computeFingerTableValues() {
        boolean found = false;

        // We set the node variable to be the ID of current Device as a node in Chord Ring...
        int node = this.nodeID;

        // The 3 iterations for the initial column.
        // For i = {1, 2, 3} in paper or in our case i = {0, 1, 2}.
        for (int i = 0; i < M; i++) {

            for (int j = fingerTable[i][0]; j < this.chordNodes.length; j++) {
                if (!(this.chordNodes[j].equals("0.0.0.0"))) {
                    node = j;
                    found = true;
                    break;
                }
            }

            if (!found) {
                Log.d("Memcached ", " Never found in one ring pass!!!");
                for (int y = 0; y < node; y++) {
                    if (!(this.chordNodes[y].equals("0.0.0.0"))) {
                        node = y;
                        break;
                    }
                }
            }

            this.fingerTable[i][1] = node;
            found = false;
        }
    }

    public void printFingerTable() {
        for (int i = 0; i < M; i++) {
            for (int j = 0; j < M - 1; j++) {
                Log.d("Memcached ", " i = " + i + " j = " + j + " : " + this.fingerTable[i][j]);
            }
        }
    }

    // Update ip of a specific Node
    public void updateNode(int nodeId, String nodeIp) {
        Log.d("Memcached: ", "   " + nodeId);
        Log.d("Memcached:  ", "   " + nodeIp);
        this.chordNodes[nodeId] = nodeIp;
    }

    // Print the ip for each Node ------> For testing reasons
    public void printRingData() {
        for (int i = 0; i < this.chordNodes.length; i++) {
            Log.d("Memcached: ", "Node " + i + " : " + this.chordNodes[i]);
        }
    }

    public int getNodeID() {
        return this.nodeID;
    }

    //**********Set & Get functions**********
    public void setNodeID(int id) {
        this.nodeID = id;
    }

    public String getNodeIP() {
        return this.nodeIP;
    }

    public void setNodeIP(String ip) {
        this.nodeIP = ip;
    }

    public int getPredecessorID() {
        return this.predecessorID;
    }

    public void setPredecessorID(int id) {
        this.predecessorID = id;
    }

    public String getPredecessorIP() {
        return this.predecessorIP;
    }

    public void setPredecessorIP(String ip) {
        this.predecessorIP = ip;
    }

    public int getSuccessorID() {
        return this.successorID;
    }

    public void setSuccessorID(int id) {
        this.successorID = id;
    }

    public String getSuccessorIP() {
        return this.successorIP;
    }

    public void setSuccessorIP(String ip) {
        this.successorIP = ip;
    }

    public int[][] getFingerTable() {
        return this.fingerTable;
    }

    public String[] getRingData() {
        return this.chordNodes;
    }

}
