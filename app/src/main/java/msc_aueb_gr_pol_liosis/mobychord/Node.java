package msc_aueb_gr_pol_liosis.mobychord;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
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

import threads.OfferServiceToConnectedUser;

//********************************* Polyvios Liosis ************************************//
//********************************* Christos Kormaris **********************************//
//********************************* Dimitris Botonakis *********************************//

public class Node extends AppCompatActivity {

    private final int m = ChordSize.m;

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
    private String myID;
    private String myIP;
    private String succID;
    private String succIP;
    private String predID;
    private String predIP;
    private String info;

    //**********Memory variables**********
    public static Memcached memcached;
    public static Set<Integer> keys = new TreeSet<Integer>();
    int [][] ftable = new int [m][2];
    String [] chordNodes = new String[(int) Math.pow(2, m)];

    public static Vector<String> filenameVector = new Vector<>(); // contains Memcached filenames
    public static Vector<String> filecontentVector = new Vector<>(); // contains Memcached filecontents


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_node);

        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        disconnectButton = (Button) this.findViewById(R.id.disconnectButton);
        // ipButton = (Button) this.findViewById(R.id.ipButton);
        crdButton = (Button) this.findViewById(R.id.crdButton);
        fingerTableButton = (Button) this.findViewById(R.id.fingertableButton);
        keysButton = (Button) this.findViewById(R.id.keysButton);
        myIdTV =  (TextView) this.findViewById(R.id.myIdTV);
        myIpTV =  (TextView) this.findViewById(R.id.myIpTV);
        logArea = (EditText) this.findViewById(R.id.logArea);
        filesButton = (Button) this.findViewById(R.id.filesButton);
        cacheButton = (Button) this.findViewById(R.id.cacheButton);

        logArea.setKeyListener(null); // disable user input
        // logArea.setEnabled(false); // make it not clickable

        keysButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick (View v) {

                logArea.setText("");
                for(int i = 0; i < keys.size(); i++) {
                    Log.d("Node", " key " + keys.toArray()[i]);
                    logArea.append("key " + keys.toArray()[i]);
                    logArea.append("\n");
                }


            }
        });

        //Get data of Node's finbgertable
        fingerTableButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick (View v) {
                //Print fingerTable in logs
                memcached.printFingertable();

                //Print fingertable inside LogArea
                ftable = memcached.getFingerTable();
                logArea.setText("");
                for(int i = 0 ; i < m ; i++) {
                    for(int j = 0; j < m-1; j++) {
                        logArea.append(" i = "+ i + " j = " +j + ": " + ftable[i][j]);
                        logArea.append("\n");
                    }
                }
            }
        });

        crdButton.setOnClickListener(new View.OnClickListener()
        {

            @Override
            public void onClick (View v)
            {
                Log.d("Node", "Node id: " + memcached.getNodeID());
                Log.d("Node", "Node ip: " + memcached.getNodeIP());
                Log.d("Node","Pred id: " + memcached.getPredID());
                Log.d("Node","Pred ip: " + memcached.getPredIP());
                Log.d("Node","Succ id: " + memcached.getSuccID());
                Log.d("Node","Succ ip: " + memcached.getSuccIP());

                logArea.setText("");

                logArea.append("Node id: " + memcached.getNodeID()); logArea.append("\n");
                logArea.append("Node ip: " + memcached.getNodeIP()); logArea.append("\n");
                logArea.append("Pred id: " + memcached.getPredID()); logArea.append("\n");
                logArea.append("Pred ip: " + memcached.getPredIP()); logArea.append("\n");
                logArea.append("Succ id: " + memcached.getSuccID()); logArea.append("\n");
                logArea.append("Succ ip: " + memcached.getSuccIP()); logArea.append("\n");
            }
        });

        filesButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick (View v) {
                logArea.setText("");

                printKeyFiles(logArea);
            }

        });

        /*
        ipButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick (View v) {

                memcached.printRingData();

                chordNodes = memcached.getRingData();
                logArea.setText("");
                for(int i = 0; i < chordNodes.length; i++) {
                    logArea.append("Node" + i + ": " + chordNodes[i]);
                    logArea.append("\n");
                }
            }
        });
        */

        cacheButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick (View v) {
                logArea.setText("");

                printMemcachedFiles(logArea);
            }

        });


        disconnectButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick (View v) {
                Toast.makeText(Node.this,"Informing successor Node... ",Toast.LENGTH_SHORT).show();

                leaveChordRing();

                Toast.makeText(Node.this, "Device left ring!" , Toast.LENGTH_SHORT).show();

                // clear keys
                keys.clear();

                goToMainActivity();

            }

        });

        retrieveNodeCrucialData();

        // Create a memcached item -> useful to retrieve faster various info for the node's functionality!!!
        memcached = new Memcached();

        // Save crucial data to device memory
        memcached.setNodeID(this.myID);
        memcached.setNodeIP(this.myIP);
        memcached.setPredID(this.predID);
        memcached.setPredIP(this.predIP);
        memcached.setSuccID(this.succID);
        memcached.setSuccIP(this.succIP);

        // Update crucial info in device memory of pred & succ nodes.
        memcached.updateNode(Integer.valueOf(myID), myIP);
        memcached.updateNode(Integer.valueOf(predID), predIP);
        memcached.updateNode(Integer.valueOf(succID) , succIP);

        // Compute required node for fingerTbale
        memcached.computeFingerTable();

        // Compute the values of required succ nodes
        memcached.computeFingerTableValues();

        // Print starting fingertable
        memcached.printFingertable();

        // Update Gui Environment
        this.myIdTV.setText("Node: " + this.myID);
        this.myIpTV.setText("IP: "   +this.myIP);

        Toast.makeText(this, "Connected to Chord as Node: " + this.myID, Toast.LENGTH_SHORT).show();

        // If we are the first Node Entering the Chord, we create 8 keys (as Integers) from 0 to 7.
        // This specific functionality has been implemented for testing reasons for later phases of The project
        if(memcached.getNodeIP().equals(memcached.getPredIP()) && memcached.getNodeIP().equals(memcached.getSuccIP())) {
            Log.d("Node", "Creating the keys...");

            for(int i = 0; i < (int) Math.pow(2,m); i++) {
                keys.add(i);
            }

        }

        if (memcached.getPredIP().equals(memcached.getSuccIP()) && memcached.getPredID().equals(memcached.getSuccID())) {
            informFirstNode();
        }
        else {
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
        file = new File(Environment.getExternalStorageDirectory() , "/mobyChord/Node/Net Architecture/myInfo.txt");

        String retrievedText = "";

        try {
            BufferedReader br = new BufferedReader(new FileReader(file));

            retrievedText = br.readLine();

            br.close();

            String[] splitted = retrievedText.split(":");

            setMyID(splitted[0]);
            setMyIP(splitted[1]);

        } catch (IOException e) {
            e.printStackTrace();
        }

        //retrieve info for the predecessor
        file = new File(Environment.getExternalStorageDirectory() , "/mobyChord/Node/Net Architecture/predInfo.txt");

        try {
            BufferedReader br = new BufferedReader(new FileReader(file));

            retrievedText = br.readLine();

            br.close();

            String[] splitted = retrievedText.split(":");

            setPredID(splitted[0]);
            setPredIP(splitted[1]);

        } catch (IOException e) {
            e.printStackTrace();
        }

        //Retrieve info for successor
        file = new File(Environment.getExternalStorageDirectory() , "/mobyChord/Node/Net Architecture/succInfo.txt");

        try {
            BufferedReader br = new BufferedReader(new FileReader(file));

            retrievedText = br.readLine();

            br.close();

            String[] splitted = retrievedText.split(":");

            setSuccID(splitted[0]);
            setSuccIP(splitted[1]);


        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    private void informFirstNode() {
        Log.d("Node", " Informing the only existing node in Chord");

        //Obviously do not send to myself
        if (!predIP.equals(this.myIP))
        {
        Socket requestSocket = null;
        ObjectOutputStream out = null;

        this.info = "0" + "#" + this.myID + "#" + this.myIP;

        try {

            requestSocket = new Socket(this.predIP, 3300);

            out = new ObjectOutputStream(requestSocket.getOutputStream());

            out.writeObject(this.info);
            out.flush();

            Log.d("Node", " Info sent to First Node ------> " + this.info);

        } catch (IOException ioException) {
            ioException.printStackTrace();
        } finally {
            try {
                if(requestSocket != null) {
                    requestSocket.close();
                }
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }

        }

    }


    private void informPredecessor() {

        // Obviously do not send to myself
        if (!predIP.equals(this.myIP)) {
            Log.d("Node", "Informing Predecessor... ");

            // Inform predecessor Node to change his successor --------------->  passing data:  info = "2#myID#myIP

            Socket requestSocket = null;
            ObjectOutputStream out;

            this.info = "2" + "#" + this.myID + "#" + this.myIP;

            try {

                Log.d("Node", this.succIP + " #### " + Integer.valueOf(this.succID));
                requestSocket = new Socket(this.predIP, 3300);
                Log.d("Node", "socket filled");

                out = new ObjectOutputStream(requestSocket.getOutputStream());

                out.writeObject(this.info);
                out.flush();

                Log.d("Node", " Info sent to pred ------> " + this.info);

            } catch (IOException ioException) {
                ioException.printStackTrace();
            } finally {
                try {
                    if(requestSocket != null) {
                        requestSocket.close();
                    }
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }

        }
    }

    private void informSuccessor() {

        //Obviously do not send to myself
        if (!succIP.equals(this.myIP)) {
            //inform successor Node to change his predecessor --------------->  passing data:  info = "1#myID#myIP

            Log.d("Node", "Informing successor... ");

            Socket requestSocket = null;
            ObjectOutputStream out;

            this.info = "1" + "#" + this.myID + "#" + this.myIP;

            try {

                requestSocket = new Socket(this.succIP, 3300);
                out = new ObjectOutputStream(requestSocket.getOutputStream());

                out.writeObject(this.info);
                out.flush();

                Log.d("Node", " Info sent to succ ------> " + this.info);

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

        for (final File fileEntry : folder.listFiles()) {
            if (!fileEntry.isDirectory()) {
                filenames.add(fileEntry.getName());
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

        for (File file : folder.listFiles()) {
            if (!file.isDirectory()) {
                Log.d("KEY_FILE", file.getName());
                text.append(file.getName());
                text.append("\n");
            }
        }
    }

    private void printMemcachedFiles(EditText text) {
        for (Map.Entry<String, Integer> entry : memcached.files_frequencies.entrySet()) {
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

        String retrievedText = "";

        try {
            BufferedReader br = new BufferedReader(new FileReader(file));

            String line = "";
            while ((line = br.readLine()) != null) {
                retrievedText = retrievedText + line;
            }
            br.close();

        } catch (FileNotFoundException e) {
            Log.e("KEY_FILE_NOT_FOUND", "Key file with this name does not exist!");            retrievedText = "";
        } catch (IOException e) {
            e.printStackTrace();
        }

        return retrievedText;
    }

    private void sendKeysToSuccessor() {
        Log.d("Node", "sendKeysToSuccessor...");

        Socket requestSocket = null;
        ObjectOutputStream out;

        Vector<String> filenames = readKeyFilenamesIntoVector();

        String successor_node_id = memcached.getSuccID();
        String successor_node_ip = memcached.getSuccIP();
        String retrieved_key;

        if(!(successor_node_id.equals(memcached.getNodeID()) && successor_node_ip.equals(this.memcached.getNodeIP()))) {
            for(int i = 0; i < Node.keys.size(); i++) {

                int key_id = (int) Node.keys.toArray()[i];

                retrieved_key = String.valueOf(keys.toArray()[i]);
                this.info = "5" + "#" + retrieved_key;

                Vector<String> filenames_to_send = new Vector<String>();
                Vector<String> filecontent_to_send = new Vector<String>();
                for(String filename: filenames) {
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

                    Log.d("Node","Sending retrieved Key to successor -----> " + this.info);

                    for (String filename: filenames_to_send) {
                        requestSocket = new Socket(successor_node_ip, 3300);
                        out = new ObjectOutputStream(requestSocket.getOutputStream());
                        out.writeObject("400#" + filename);
                        out.flush();
                        requestSocket.close();

                        Log.d("Node", "Sending filename to successor -----> " + filename);
                    }

                    for (String filecontent: filecontent_to_send) {
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
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    requestSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    private void informSuccessorForLeavingRing() {

        //Obviously do not send to myself
        if (!succIP.equals(this.myIP)) {
            Log.d("Node", "Informing successor for leaving ring... ");

            Socket requestSocket = null;
            ObjectOutputStream out;

            //I pass to my successor the info of my predecessor
            this.info = "6" + "#" + memcached.getPredID() + "#" + memcached.getPredIP();

            try {

                requestSocket = new Socket(this.succIP, 3300);
                out = new ObjectOutputStream(requestSocket.getOutputStream());

                out.writeObject(this.info);
                out.flush();

                Log.d("Node", "Info sent to succ ------> " + this.info);

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

    private void informPredecessorForLeavingRing() {

        //Obviously do not send to myself
        if (!predIP.equals(this.myIP)) {
            Log.d("Node", "Informing predecessor for leaving ring... ");

            Socket requestSocket = null;
            ObjectOutputStream out;

            //I pass to my predecessor the info of my successor
            this.info = "7" + "#" + memcached.getSuccID() + "#" + memcached.getSuccIP();

            try {

                requestSocket = new Socket(this.predIP, 3300);
                out = new ObjectOutputStream(requestSocket.getOutputStream());

                out.writeObject(this.info);
                out.flush();

                Log.d("Node", " Info sent to succ ------> " + this.info);

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

    private void openMiniServer() {

        Thread socketServerThread = new Thread(new SocketServerThread());
        socketServerThread.start();

    }

    public String getMyIP() {
        return this.myIP;
    }
    public String getMyID() {
        return this.myID;
    }
    public Context getContext() {
        return this;
    }

    private class SocketServerThread extends Thread {

        ServerSocket miniServerSocket = null;
        final int miniServerPort = 3300;

        @Override
        public void run() {
            try {
                miniServerSocket = new ServerSocket(miniServerPort);
                Node.this.runOnUiThread(new Runnable() {

                    @Override
                    public void run()
                    {
                        Log.d("Node", "MiniServer is listening to port: " + miniServerPort + " and IP:" + getMyIP());
                        Toast.makeText(getContext(), "MiniServer Mode: On", Toast.LENGTH_SHORT).show();
                    }
                });

                while (true) {

                    Socket connection = miniServerSocket.accept();

                    //retrieveNodeCrucialData();

                    //Thread t = new OfferServiceToConnectedUser(getContext(), connection, myID , myIP , predID, predIP, succID , succIP);

                    Thread t = new OfferServiceToConnectedUser(getContext(), connection);
                    t.start();

                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (miniServerSocket != null) {
                        miniServerSocket.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    public void setMyID(String id) {
        this.myID = id;
    }

    public void setMyIP(String ip) {
        this.myIP = ip;
    }

    public void setSuccID(String id) {
        this.succID = id;
    }

    public void setSuccIP(String ip) {
        this.succIP = ip;
    }

    public void setPredID(String id) {
        this.predID = id;
    }

    public void setPredIP(String ip) {
        this.predIP = ip;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        Toast.makeText(Node.this, "Node is still connected to ring. Server is still running!" , Toast.LENGTH_SHORT).show();

        // go back to main activity, while still running the server
        goToMainActivity();
    }

}
