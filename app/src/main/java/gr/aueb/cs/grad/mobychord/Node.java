package gr.aueb.cs.grad.mobychord;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

import gr.aueb.cs.grad.mobychord.threads.OfferServiceToConnectedUser;

//********************************* Polyvios Liosis ************************************//
//********************************* Christos Kormaris **********************************//
//********************************* Dimitris Botonakis *********************************//

public class Node extends AppCompatActivity {

    //**********Memory variables**********
    public static Memcached memcached;
    public static Set<Integer> keys = new TreeSet<>();
    public static Vector<String> filenameVector = new Vector<>();  // contains Memcached filenames
    public static Vector<String> fileContentVector = new Vector<>();  // contains Memcached filecontents
    int[][] fingerTable = new int[ChordSize.M][2];
    //**********GUI Variables**********
    private Button disconnectButton;
    private Button crdButton;
    private Button fingerTableButton;
    private Button keysButton;
    private Button filesButton;
    // private Button ipButton;
    private Button cacheButton;
    private TextView myIdTV;
    private TextView myIpTV;
    private EditText logArea;
    //**********Networking Variables**********
    private int myID = 0;
    private String myIP = "0.0.0.0";
    private int successorID = 0;
    private String successorIP = "0.0.0.0";
    private int predecessorID = 0;
    // String[] chordNodes = new String[(int) Math.pow(2, M)];
    private String predecessorIP = "0.0.0.0";
    private String info;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_node);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        disconnectButton = this.findViewById(R.id.disconnectButton);
        // ipButton = this.findViewById(R.id.ipButton);
        crdButton = this.findViewById(R.id.crdButton);
        fingerTableButton = this.findViewById(R.id.fingertableButton);
        keysButton = this.findViewById(R.id.keysButton);
        myIdTV = this.findViewById(R.id.myIdTV);
        myIpTV = this.findViewById(R.id.myIpTV);
        logArea = this.findViewById(R.id.logArea);
        filesButton = this.findViewById(R.id.filesButton);
        cacheButton = this.findViewById(R.id.cacheButton);

        logArea.setKeyListener(null); // disable user input
        // logArea.setEnabled(false); // make it not clickable

        keysButton.setOnClickListener(v -> {
            logArea.setText("");
            for (int i = 0; i < keys.size(); i++) {
                Log.d("Node", " key " + keys.toArray()[i]);
                logArea.append("key " + keys.toArray()[i]);
                logArea.append("\n");
            }
        });

        //Get data of Node's finbgertable
        fingerTableButton.setOnClickListener(v -> {
            // Print fingerTable in logs
            memcached.printFingerTable();

            // Print fingerTable inside LogArea
            fingerTable = memcached.getFingerTable();
            logArea.setText("");
            for (int i = 0; i < ChordSize.M; i++) {
                for (int j = 0; j < ChordSize.M - 1; j++) {
                    logArea.append(" i = " + i + " j = " + j + ": " + fingerTable[i][j]);
                    logArea.append("\n");
                }
            }
        });

        crdButton.setOnClickListener(v -> {
            Log.d("Node", "Node id: " + memcached.getNodeID());
            Log.d("Node", "Node ip: " + memcached.getNodeIP());
            Log.d("Node", "Predecessor id: " + memcached.getPredecessorID());
            Log.d("Node", "Predecessor ip: " + memcached.getPredecessorIP());
            Log.d("Node", "Successor id: " + memcached.getSuccessorID());
            Log.d("Node", "Successor ip: " + memcached.getSuccessorIP());

            logArea.setText("");

            logArea.append("Node id: " + memcached.getNodeID());
            logArea.append("\n");
            logArea.append("Node ip: " + memcached.getNodeIP());
            logArea.append("\n");
            logArea.append("Predecessor id: " + memcached.getPredecessorID());
            logArea.append("\n");
            logArea.append("Predecessor ip: " + memcached.getPredecessorIP());
            logArea.append("\n");
            logArea.append("Successor id: " + memcached.getSuccessorID());
            logArea.append("\n");
            logArea.append("Successor ip: " + memcached.getSuccessorIP());
            logArea.append("\n");
        });

        filesButton.setOnClickListener(v -> {
            logArea.setText("");
            printKeyFiles(logArea);
        });

        /*
        ipButton.setOnClickListener((View.OnClickListener) v -> {
            memcached.printRingData();
            chordNodes = memcached.getRingData();
            logArea.setText("");
            for(int i = 0; i < chordNodes.length; i++) {
                logArea.append("Node" + i + ": " + chordNodes[i]);
                logArea.append("\n");
            }
        });
        */

        cacheButton.setOnClickListener(v -> {
            logArea.setText("");
            printMemcachedFiles(logArea);
        });

        disconnectButton.setOnClickListener(v -> {
            Toast.makeText(Node.this, "Informing successor Node... ", Toast.LENGTH_SHORT).show();
            leaveChordRing();
            Toast.makeText(Node.this, "Device left ring!", Toast.LENGTH_SHORT).show();
            // clear keys
            keys.clear();
            goToMainActivity();
        });

        retrieveNodeCrucialData();

        // Create a memcached item -> useful to retrieve faster various info for the node's functionality!!!
        memcached = new Memcached();

        // Save crucial data to device memory
        memcached.setNodeID(this.myID);
        memcached.setNodeIP(this.myIP);
        memcached.setPredecessorID(this.predecessorID);
        memcached.setPredecessorIP(this.predecessorIP);
        memcached.setSuccessorID(this.successorID);
        memcached.setSuccessorIP(this.successorIP);

        // Update crucial info in device memory of predecessor & successor nodes.
        memcached.updateNode(myID, myIP);
        memcached.updateNode(predecessorID, predecessorIP);
        memcached.updateNode(successorID, successorIP);

        // Compute required node for fingerTable
        memcached.computeFingerTable();

        // Compute the values of required succ nodes
        memcached.computeFingerTableValues();

        // Print starting fingerTable
        memcached.printFingerTable();

        // Update Gui Environment
        this.myIdTV.setText("Node: " + this.myID);
        this.myIpTV.setText("IP: " + this.myIP);

        Toast.makeText(this, "Connected to Chord as Node: " + this.myID, Toast.LENGTH_SHORT).show();

        // If we are the first Node Entering the Chord, we create 8 keys (as Integers) from 0 to 7.
        // This specific functionality has been implemented for testing reasons for later phases of The project
        if (memcached.getNodeIP().equals(memcached.getPredecessorIP())
                && memcached.getNodeIP().equals(memcached.getSuccessorIP())) {
            Log.d("Node", "Creating the keys...");

            for (int i = 0; i < (int) Math.pow(2, ChordSize.M); i++) {
                keys.add(i);
            }
        }

        if (memcached.getPredecessorIP().equals(memcached.getSuccessorIP())
                && memcached.getPredecessorID() == memcached.getSuccessorID()) {
            informFirstNode();
        } else {
            informPredecessor();
            informSuccessor();
        }

        // Get in multiThreaded mini server mode
        openMiniServer();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


    private void goToMainActivity() {
        Intent intent = new Intent(this, ChooseMode.class);
        startActivity(intent);
        finish();
    }


    private void retrieveNodeCrucialData() {
        File file;

        //Retrieve info for the Node
        file = new File(
                Environment.getExternalStorageDirectory(),
                "/mobyChord/Node/Net Architecture/myInfo.txt"
        );

        String retrievedText;

        try {
            BufferedReader br = new BufferedReader(new FileReader(file));

            retrievedText = br.readLine();

            br.close();

            String[] splitted = retrievedText.split(":");

            setMyID(Integer.parseInt(splitted[0]));
            setMyIP(splitted[1]);

        } catch (IOException ex) {
            ex.printStackTrace();
        }

        // retrieve info for the predecessor
        file = new File(
                Environment.getExternalStorageDirectory(),
                "/mobyChord/Node/Net Architecture/predecessorInfo.txt"
        );

        try {
            BufferedReader br = new BufferedReader(new FileReader(file));

            retrievedText = br.readLine();

            br.close();

            String[] splitted = retrievedText.split(":");

            setPredecessorID(Integer.parseInt(splitted[0]));
            setPredecessorIP(splitted[1]);
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        //Retrieve info for successor
        file = new File(Environment.getExternalStorageDirectory(), "/mobyChord/Node/Net Architecture/successorInfo.txt");

        try {
            BufferedReader br = new BufferedReader(new FileReader(file));

            retrievedText = br.readLine();

            br.close();

            String[] splitted = retrievedText.split(":");

            setSuccessorID(Integer.parseInt(splitted[0]));
            setSuccessorIP(splitted[1]);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }


    private void informFirstNode() {
        Log.d("Node", " Informing the only existing node in Chord");

        // Obviously do not send to myself
        if (!predecessorIP.equals(this.myIP)) {
            Socket requestSocket = null;
            ObjectOutputStream out;

            this.info = 0 + "#" + this.myID + "#" + this.myIP;

            try {
                requestSocket = new Socket(this.predecessorIP, 3300);

                out = new ObjectOutputStream(requestSocket.getOutputStream());

                out.writeObject(this.info);
                out.flush();

                Log.d("Node", " Info sent to First Node ------> " + this.info);
            } catch (IOException ex) {
                ex.printStackTrace();
            } finally {
                try {
                    if (requestSocket != null) {
                        requestSocket.close();
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }


    private void informPredecessor() {

        // Obviously do not send to myself
        if (!predecessorIP.equals(this.myIP)) {
            Log.d("Node", "Informing Predecessor... ");

            // Inform predecessor Node to change his successor --------------->  passing data:  info = "2#myID#myIP

            Socket requestSocket = null;
            ObjectOutputStream out;

            this.info = 2 + "#" + this.myID + "#" + this.myIP;

            try {
                Log.d("Node", this.successorIP + " #### " + Integer.valueOf(this.successorID));
                requestSocket = new Socket(this.predecessorIP, 3300);
                Log.d("Node", "socket filled");

                out = new ObjectOutputStream(requestSocket.getOutputStream());

                out.writeObject(this.info);
                out.flush();

                Log.d("Node", " Info sent to pred ------> " + this.info);
            } catch (IOException ex) {
                ex.printStackTrace();
            } finally {
                try {
                    if (requestSocket != null) {
                        requestSocket.close();
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }

        }
    }

    private void informSuccessor() {
        //Obviously do not send to myself
        if (!successorIP.equals(this.myIP)) {
            // inform successor Node to change his predecessor --------------->  passing data:  info = "1#myID#myIP

            Log.d("Node", "Informing successor... ");

            Socket requestSocket = null;
            ObjectOutputStream out;

            this.info = 1 + "#" + this.myID + "#" + this.myIP;

            try {
                requestSocket = new Socket(this.successorIP, 3300);
                out = new ObjectOutputStream(requestSocket.getOutputStream());

                out.writeObject(this.info);
                out.flush();

                Log.d("Node", " Info sent to succ ------> " + this.info);
            } catch (IOException ex) {
                ex.printStackTrace();
            } finally {
                try {
                    if (requestSocket != null) {
                        requestSocket.close();
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    private void leaveChordRing() {
        //Inform my pred that i will leave the ring, so it will make its succ my succ!!!
        informPredecessorForLeavingRing();
        //Inform my succ that I will leave the ring, so it will make its pred my pred!!!
        informSuccessorForLeavingRing();
        //Pass my keys to my successor
        sendKeysToSuccessor();
        keys.clear();
    }

    private Vector<String> readKeyFilenamesIntoVector() {
        Vector<String> filenames = new Vector<>();

        String pathToKeys = Environment.getExternalStorageDirectory()
                + File.separator + "mobyChord/Node/Keys/";

        File folder = new File(pathToKeys);

        if (folder.listFiles() != null) {
            for (final File fileEntry : folder.listFiles()) {
                if (!fileEntry.isDirectory()) {
                    filenames.add(fileEntry.getName());
                }
            }
        }

        return filenames;
    }

    private void deleteKeyFiles(Vector<String> filenames) {
        String pathToKeys = Environment.getExternalStorageDirectory()
                + File.separator + "mobyChord/Node/Keys/";

        for (String filename : filenames) {
            File file = new File(pathToKeys + "/" + filename);
            file.delete();
        }
    }

    private void printKeyFiles(EditText text) {
        String pathToKeys = Environment.getExternalStorageDirectory()
                + File.separator + "mobyChord/Node/Keys/";

        File folder = new File(pathToKeys);

        if (folder.listFiles() != null) {
            for (File file : folder.listFiles()) {
                if (!file.isDirectory()) {
                    Log.d("KEY_FILE", file.getName());
                    text.append(file.getName());
                    text.append("\n");
                }
            }
        }
    }

    private void printMemcachedFiles(EditText text) {
        for (Map.Entry<String, Integer> entry : memcached.fileFrequencies.entrySet()) {
            Log.d("Memcached", "filename: " + entry.getKey() + ", frequency: " + entry.getValue());
            text.append(entry.getKey() + ", frequency: " + entry.getValue());
            text.append("\n");
        }
    }

    private String getKeyFileContentByFileName(String routeFilename) {
        String pathToKeys = Environment.getExternalStorageDirectory()
                + File.separator + "mobyChord/Node/Keys/"
                + routeFilename;

        File file = new File(pathToKeys);

        StringBuilder retrievedText = new StringBuilder();

        try {
            BufferedReader br = new BufferedReader(new FileReader(file));

            String line;
            while ((line = br.readLine()) != null) {
                retrievedText.append(line);
            }
            br.close();

        } catch (FileNotFoundException ex) {
            Log.e("KEY_FILE_NOT_FOUND", "Key file with this name does not exist!");
            retrievedText = new StringBuilder();
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        return retrievedText.toString();
    }

    private void sendKeysToSuccessor() {
        Log.d("Node", "sendKeysToSuccessor...");

        Socket requestSocket = null;
        ObjectOutputStream out;

        Vector<String> filenames = readKeyFilenamesIntoVector();

        int successor_node_id = memcached.getSuccessorID();
        String successor_node_ip = memcached.getSuccessorIP();
        String retrieved_key;

        if (!(successor_node_id == memcached.getNodeID() && successor_node_ip.equals(memcached.getNodeIP()))) {
            for (int i = 0; i < Node.keys.size(); i++) {

                int key_id = (int) Node.keys.toArray()[i];

                retrieved_key = String.valueOf(keys.toArray()[i]);
                this.info = 5 + "#" + retrieved_key;

                Vector<String> filenames_to_send = new Vector<>();
                Vector<String> filecontent_to_send = new Vector<>();
                for (String filename : filenames) {
                    if (filename.startsWith(String.valueOf(key_id))) {
                        filenames_to_send.add(filename);
                        filecontent_to_send.add(getKeyFileContentByFileName(filename));
                    }
                }

                try {

                    requestSocket = new Socket(successor_node_ip, 3300);
                    out = new ObjectOutputStream(requestSocket.getOutputStream());

                    out.writeObject(this.info);
                    out.flush();
                    requestSocket.close();

                    Log.d("Node", "Sending retrieved Key to successor -----> " + this.info);

                    for (String filename : filenames_to_send) {
                        requestSocket = new Socket(successor_node_ip, 3300);
                        out = new ObjectOutputStream(requestSocket.getOutputStream());
                        out.writeObject("400#" + filename);
                        out.flush();
                        requestSocket.close();

                        Log.d("Node", "Sending filename to successor -----> " + filename);
                    }

                    for (String filecontent : filecontent_to_send) {
                        requestSocket = new Socket(successor_node_ip, 3300);
                        out = new ObjectOutputStream(requestSocket.getOutputStream());
                        out.writeObject("401#" + filecontent);
                        out.flush();
                        requestSocket.close();

                        Log.d("Node", "Sending filecontent to successor -----> " + filecontent);
                    }

                    // delete the files that were sent to the predecessor
                    deleteKeyFiles(filenames_to_send);


                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }

            try {
                requestSocket = new Socket(successor_node_ip, 3300);
                out = new ObjectOutputStream(requestSocket.getOutputStream());
                out.writeObject("402#WRITE");
                out.flush();
            } catch (IOException ex) {
                ex.printStackTrace();
            } finally {
                try {
                    if (requestSocket != null) {
                        requestSocket.close();
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }

    }

    private void informSuccessorForLeavingRing() {
        // Obviously do not send to myself
        if (!successorIP.equals(this.myIP)) {
            Log.d("Node", "Informing successor for leaving ring... ");

            Socket requestSocket = null;
            ObjectOutputStream out;

            // I pass to my successor the info of my predecessor
            this.info = 6 + "#" + memcached.getPredecessorID() + "#" + memcached.getPredecessorIP();

            try {

                requestSocket = new Socket(this.successorIP, 3300);
                out = new ObjectOutputStream(requestSocket.getOutputStream());

                out.writeObject(this.info);
                out.flush();

                Log.d("Node", "Info sent to succ ------> " + this.info);

            } catch (IOException ex) {
                ex.printStackTrace();
            } finally {
                try {
                    if (requestSocket != null) {
                        requestSocket.close();
                    }

                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    private void informPredecessorForLeavingRing() {
        // Obviously do not send to myself
        if (!predecessorIP.equals(this.myIP)) {
            Log.d("Node", "Informing predecessor for leaving ring...");

            Socket requestSocket = null;
            ObjectOutputStream out;

            //I pass to my predecessor the info of my successor
            this.info = 7 + "#" + memcached.getSuccessorID() + "#" + memcached.getSuccessorIP();

            try {

                requestSocket = new Socket(this.predecessorIP, 3300);
                out = new ObjectOutputStream(requestSocket.getOutputStream());

                out.writeObject(this.info);
                out.flush();

                Log.d("Node", " Info sent to succ ------> " + this.info);

            } catch (IOException ex) {
                ex.printStackTrace();
            } finally {
                try {
                    if (requestSocket != null) {
                        requestSocket.close();
                    }

                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    private void openMiniServer() {
        Thread socketServerThread = new Thread(new SocketServerThread());
        socketServerThread.start();
    }

    public String getMyIP() {
        return this.myIP;
    }

    public void setMyIP(String ip) {
        this.myIP = ip;
    }

    public int getMyID() {
        return this.myID;
    }

    public void setMyID(int id) {
        this.myID = id;
    }

    public Context getContext() {
        return this;
    }

    public void setSuccessorID(int id) {
        this.successorID = id;
    }

    public void setSuccessorIP(String ip) {
        this.successorIP = ip;
    }

    public void setPredecessorID(int id) {
        this.predecessorID = id;
    }

    public void setPredecessorIP(String ip) {
        this.predecessorIP = ip;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        Toast.makeText(
                Node.this,
                "Node is still connected to ring. Server is still running!",
                Toast.LENGTH_SHORT
        ).show();

        // go back to main activity, while still running the server
        goToMainActivity();
    }

    private class SocketServerThread extends Thread {
        final int miniServerPort = 3300;
        ServerSocket miniServerSocket = null;

        @Override
        public void run() {
            try {
                miniServerSocket = new ServerSocket(miniServerPort);
                Node.this.runOnUiThread(() -> {
                    Log.d("Node", "MiniServer is listening to port: " + miniServerPort + " and IP:" + getMyIP());
                    Toast.makeText(getContext(), "MiniServer Mode: On", Toast.LENGTH_SHORT).show();
                });

                while (true) {
                    Socket connection = miniServerSocket.accept();

                    // retrieveNodeCrucialData();

                    /*
                    Thread t = new OfferServiceToConnectedUser(
                            getContext(),
                            connection,
                            myID,
                            myIP,
                            predecessorID,
                            predecessorIP,
                            successorID,
                            successorIP
                    );
                    */

                    Thread thread = new OfferServiceToConnectedUser(getContext(), connection);
                    thread.start();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            } finally {
                try {
                    if (miniServerSocket != null) {
                        miniServerSocket.close();
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }

    }

}
