package threads;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Vector;

import msc_aueb_gr_pol_liosis.mobychord.DBManager;
import msc_aueb_gr_pol_liosis.mobychord.Node;

// import android.widget.Toast;

//********************************* Polyvios Liosis ************************************//
//********************************* Christos Kormaris **********************************//
//********************************* Dimitris Botonakis *********************************//

public class OfferServiceToConnectedUser extends Thread {
    //**********Networking Variables**********
    private ObjectInputStream inputStream;

    //    private ObjectOutputStream outputStream;
    private String myID;
    private String myIP;
    private String predecessorID;
    private String predecessorIP;
    private String successorID;
    private String successorIP;

    private String info;

    //**********Data Base Variables**********
    private DBManager db_helper;

    //**********GUI Variables**********
    private Context context;

    public OfferServiceToConnectedUser(Context context, Socket connection) {
        try {
            this.context = context;

            inputStream = new ObjectInputStream(connection.getInputStream());
            // outputStream = new ObjectOutputStream(connection.getOutputStream());

            this.myID = Node.memcached.getNodeID();
            this.myIP = Node.memcached.getNodeIP();
            this.predecessorID = Node.memcached.getPredecessorID();
            this.predecessorIP = Node.memcached.getPredecessorIP();
            this.successorID = Node.memcached.getSuccessorID();
            this.successorIP = Node.memcached.getSuccessorIP();

            db_helper = new DBManager(this.context);

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }


    public void run() {
        try {
            // The passed_data most of the times is a String with various forms.
            // The forms of a passed data can be the following ones:

            // code 0 ->   data :  info = "0#ID#IP"
            // Connected second Node to Chord
            // code 1 ->   data :  info = "1#ID#IP
            // Change my predecessor to the node with IP == myIP
            // && Update fingerTable && Update Chord db && Inform my successor
            // code 2 ->  data :  info = "2#ID#IP
            // Change my successor to the node with IP == myIP && Update fingerTable && Update Chord db
            // code 3 -> data :  info = "3#ID#IP
            // Update fingertable && Update Chord db && Inform my successor
            // code 4 ->  data :  info = "4#ID#IP
            // Recognize an already existed node.
            // code 5 ->  data :  info = "5#key
            // received Key
            // code 6 -> data :  info = "6#ID#IP
            // Make Node with ID and IP my predecessor
            // code 7 ->  data :  info = "7#ID#IP
            // Make Node with given ID and IP my successor
            // code 8 ->  data :  info = "8#leaving_node_id
            // Update other node about leaving node's departure

            // code 100 ->   data :  info = "100#srcPostalCode#dstPostalCod
            // #srcLatitude#srcLongitude#dstLatitude#dstLongitud
            // #clientIpAddress#initialAskNodeIP -------> .........Not implemented yet........

            // code 400 ->  data :  info = "400#filename  ------->  Retrieve the filenames for the files to be created
            // code 401 ->  data :  info = "401#filecontent  ------->  Retrieve the content for the files to be created
            // code 402 ->  data :  info = "402#WRITE  ------->  Start creating and writing to files

            String received_data = (String) inputStream.readObject();

            String[] split_data = received_data.split("#");

            if (split_data[0].equals("0")) {
                Log.d("OfferServiceToUser", "Second node connected to Chord's Ring");

                updateSuccessorAndPredecessor(split_data);
                updateFingerTable();

                sendKeysToPredecessor();
            }
            if (split_data[0].equals("1")) {

                Log.d("OfferServiceToUser", "Update my predecessor");
                //A new Node is already inserted before me so update my predecessor
                updatePredecessor(split_data);
                //Inform my successor for the new node so that the news be transmitted in a cycle
                informSuccessor(split_data);

                //Update my fingertable
                updateFingerTable();
                sendKeysToPredecessor();
            }
            if (split_data[0].equals("2")) {
                Log.d("OfferServiceToUser", "Update my successor ");
                //A new Inserted after me so update my successor
                updateSuccessor(split_data);

                //Update my FingerTable
                updateFingerTable();
            }
            if (split_data[0].equals("3")) {
                Log.d("OfferServiceToUser", "Inform New node for my Existence in Chord Ring");

                //Received info from my pred that a new node inserted, so I have to inform him for my existence
                informNewNodeForMyState(split_data);
                //Update my SQLite Database
                updateDB(split_data);
                //Update my data in memcached
                Node.memcached.updateNode(Integer.parseInt(split_data[1]), split_data[2]);
                //Update my fingerTable
                updateFingerTable();
            }
            if (split_data[0].equals("4")) {
                Log.d("OfferServiceToUser", "Node's existence message received ");
                //In this case i am the new inserted node

                //Update my SQLite Database
                updateDB(split_data);
                //Update my data in memcached
                Node.memcached.updateNode(Integer.parseInt(split_data[1]), split_data[2]);
                //Update my fingerTable
                updateFingerTable();
            }
            if (split_data[0].equals("5")) {
                int new_key = Integer.parseInt(split_data[1]);
                Node.keys.add(new_key);
                Log.d("OfferServiceToUser", "New key received :" + new_key);
            }
            if (split_data[0].equals("6")) {
                String leaving_node_ip = Node.memcached.getPredecessorIP();
                String leaving_node_id = Node.memcached.getPredecessorID();
                Node.memcached.updateNode(Integer.parseInt(leaving_node_id), "0.0.0.0");

                updatePredecessor(split_data);
                updateFingerTable();
                informSuccessorForLeavingNode(leaving_node_id, leaving_node_ip);

            }
            if (split_data[0].equals("7")) {
                // String leaving_node_ip = Node.memcached.getSuccIP();
                String leaving_node_id = Node.memcached.getSuccessorID();
                Node.memcached.updateNode(Integer.parseInt(leaving_node_id), "0.0.0.0");

                updateSuccessor(split_data);
                updateFingerTable();

            }
            if (split_data[0].equals("8")) {
                // String leaving_node_ip = split_data[2];
                String leaving_node_id = split_data[1];
                Node.memcached.updateNode(Integer.parseInt(leaving_node_id), "0.0.0.0");

                updateFingerTable();
            }
            if (split_data[0].equals("100")) {

                /*
                String srcPostalCode = split_data[1];
                String dstPostalCode = split_data[2];
                String srcLatitude = split_data[3];
                String srcLongitude = split_data[4];
                String dstLatitude = split_data[5];
                String dstLongitude = split_data[6];
                String clientIpAddress = split_data[7];
                String initialAskNodeIP = split_data[8];
                */

                // String passInfo = received_data.replace("100#", "");
                String passInfo = received_data;
                Log.d("passInfo", passInfo);

                LookUp lookUpThread = new LookUp(Node.memcached, Node.keys, passInfo);
                lookUpThread.start();

                try {
                    lookUpThread.join();
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }

                String routeInfo = lookUpThread.getRouteInfo();
                Log.d("lookUpThreadRouteInfo", routeInfo);

                if (!routeInfo.equals("")) {
                    // Send message back to Client.
                    // WORKS ONLY ON REAL DEVICES.
                    // DOES NOT WORK ON EMULATORS.
                    SendRouteToClient sendRouteToClientThread = new SendRouteToClient(myIP, passInfo, routeInfo);
                    sendRouteToClientThread.start();
                }
            }
            if (split_data[0].equals("400")) {
                Log.d("400", "Retrieved filename!");
                String filename = split_data[1];
                Node.filenameVector.add(filename);
            }
            if (split_data[0].equals("401")) {
                Log.d("401", "Retrieved filecontent!");
                String filecontent = split_data[1];
                Node.fileContentVector.add(filecontent);
            }
            if (split_data[0].equals("402")) {
                Log.d("402", "Retrieved all files and their contents!");
                for (int i = 0; i < Node.filenameVector.size(); i++) {
                    String filename = Node.filenameVector.get(i);
                    String filecontent = Node.fileContentVector.get(i);

                    Log.d("name_content", filename + "#" + filecontent);
                }

                for (int i = 0; i < Node.filenameVector.size(); i++) {
                    String filename = Node.filenameVector.get(i);
                    String filecontent = Node.fileContentVector.get(i);

                    writeContentToKeyFile(filename, filecontent);
                }
                Node.filenameVector.clear();
                Node.fileContentVector.clear();
            }

        } catch (IOException | ClassNotFoundException ex) {
            ex.printStackTrace();
        }
    }

    private void writeContentToKeyFile(String filename, String content) {
        DataOutputStream fos;
        try {
            fos = new DataOutputStream(new FileOutputStream(Environment.getExternalStorageDirectory()
                    + File.separator + "mobyChord/Node/Keys/"
                    + filename));

            fos.writeBytes(content);
            fos.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    // Inform successor for the new Node!!!
    private void informSuccessor(String[] data) {
        Log.d("OfferServiceToUser", "Informing successor " + this.successorIP + "For new Node " + data[2]);

        //Do not inform the new Node for his self obviously!!!
        if (!(this.successorID.equals(data[1]) && this.successorIP.equals(data[2]))) {
            Socket requestSocket = null;
            ObjectOutputStream out;

            this.info = "3" + "#" + data[1] + "#" + data[2];

            try {
                requestSocket = new Socket(this.successorIP, 3300);
                out = new ObjectOutputStream(requestSocket.getOutputStream());

                out.writeObject(this.info);
                out.flush();

                Log.d("OfferServiceToUser", "Info sent to succ ------> " + this.info);
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

    private void informSuccessorForLeavingNode(String leaving_node_id, String leaving_node_ip) {
        Log.d("OfferServiceToUser", "Informing successor " + this.successorIP
                + "For leaving Node " + leaving_node_id);

        //Do not inform the new Node for his self obviously!!!
        if (!(this.successorID.equals(Node.memcached.getNodeID())
                && this.successorIP.equals(Node.memcached.getNodeIP()))) {
            Socket requestSocket = null;
            ObjectOutputStream out;

            this.info = "8" + "#" + leaving_node_id + "#" + leaving_node_ip;

            try {
                requestSocket = new Socket(this.successorIP, 3300);
                out = new ObjectOutputStream(requestSocket.getOutputStream());

                out.writeObject(this.info);
                out.flush();

                Log.d("OfferServiceToUser", "Info sent to succ ------> " + this.info);
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

    // Update my FingerTable
    private void updateFingerTable() {
        Node.memcached.computeFingerTableValues();
        Node.memcached.printFingerTable();
    }

    private void updateSuccessorAndPredecessor(String[] data) {
        // Update local storage file which is referred to Node's predecessor!!!
        DataOutputStream fos;
        try {
            fos = new DataOutputStream(new FileOutputStream(
                    Environment.getExternalStorageDirectory() + "/mobyChord/Node/Net Architecture/predecessorInfo.txt"));
            fos.writeBytes(data[1] + ":" + data[2]);

            // Update local db
            updateDB(data);

            // Update memcached
            Node.memcached.updateNode(Integer.parseInt(data[1]), data[2]);
            Node.memcached.setPredecessorID(data[1]);
            Node.memcached.setPredecessorIP(data[2]);

        } catch (IOException ex) {
            ex.printStackTrace();
        }

        // Update local storage file which is referred to Node's successor!!!
        DataOutputStream fos2;
        try {
            fos2 = new DataOutputStream(new FileOutputStream(
                    Environment.getExternalStorageDirectory() + "/mobyChord/Node/Net Architecture/successorInfo.txt"));
            fos2.writeBytes(data[1] + ":" + data[2]);

            // Update local db
            updateDB(data);

            // Update memcached
            Node.memcached.updateNode(Integer.parseInt(data[1]), data[2]);
            Node.memcached.setSuccessorID(data[1]);
            Node.memcached.setSuccessorIP(data[2]);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }


    private void updatePredecessor(String[] data) {
        Log.d("OfferServiceToUser", "New Predecessor " + data[1] + " with ip " + data[2]);

        // If predecessors is the same, I do nothing (meaning it has already been updated by my predecessor's request)
        if (!(predecessorID.equals(data[1]) && predecessorIP.equals(data[2]))) {
            //Update local storage file which is referred to Node's predecessor!!!
            DataOutputStream fos;
            try {
                fos = new DataOutputStream(new FileOutputStream(
                        Environment.getExternalStorageDirectory() + "/mobyChord/Node/Net Architecture/predecessorInfo.txt"));
                fos.writeBytes(data[1] + ":" + data[2]);

                //Update local db
                updateDB(data);

                //Update memcached
                Node.memcached.updateNode(Integer.parseInt(data[1]), data[2]);
                Node.memcached.setPredecessorID(data[1]);
                Node.memcached.setPredecessorIP(data[2]);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }


    private void updateSuccessor(String[] data) {
        Log.d("OfferServiceToUser", "New successor " + data[1] + " with ip " + data[2]);

        // If successor is the same, I do nothing (meaning it has already been updated by my successor's request)
        if (!(successorID.equals(data[1]) && successorIP.equals(data[2]))) {
            // Update local storage file which is referred to Node's successor!!!
            DataOutputStream fos;
            try {
                fos = new DataOutputStream(new FileOutputStream(
                        Environment.getExternalStorageDirectory() + "/mobyChord/Node/Net Architecture/successorInfo.txt"));
                fos.writeBytes(data[1] + ":" + data[2]);

                // Update local db
                updateDB(data);

                // Update memcached
                Node.memcached.updateNode(Integer.parseInt(data[1]), data[2]);
                Node.memcached.setSuccessorID(data[1]);
                Node.memcached.setSuccessorIP(data[2]);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    private void updateDB(String[] data) {
        // Update the database Chord
        db_helper.updateNode(data[1], data[2]);
        // Toast.makeText(this.context, "Node " + data[1] + " connected to ring!!!", Toast.LENGTH_SHORT).show();
    }

    // Inform new inserted Node for my existence in order to update his Data Base and FingerTable
    private void informNewNodeForMyState(String[] data) {
        Socket requestSocket = null;
        ObjectOutputStream out;

        this.info = "4" + "#" + this.myID + "#" + this.myIP;

        try {
            requestSocket = new Socket(data[2], 3300);
            out = new ObjectOutputStream(requestSocket.getOutputStream());

            out.writeObject(this.info);
            out.flush();

            Log.d("OfferServiceToUser", "Info sent to New Inserted Node ------> " + this.info);
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

    private void sendKeysToPredecessor() {
        Log.d("offerService", "sendKeysToPredecessor...");

        Socket requestSocket = null;
        ObjectOutputStream out;

        Vector<String> filenames = readKeyFilenamesIntoVector();

        int pred_node_id = Integer.parseInt(Node.memcached.getPredecessorID());
        String pred_node_ip = Node.memcached.getPredecessorIP();
        int this_node_id = Integer.parseInt(Node.memcached.getNodeID());
        String retrieved_key;

        for (int i = 0; i < Node.keys.size(); i++) {

            int key_id = (int) Node.keys.toArray()[i];

            // IT WORKS!!
            if (isBetween(pred_node_id, key_id, this_node_id)
                    || isBetween(this_node_id, pred_node_id, key_id)
                    || isBetween(key_id, this_node_id, pred_node_id)
                    || key_id == pred_node_id) {

                Node.keys.remove(key_id);
                i--;

                Vector<String> filenamesToSend = new Vector<>();
                Vector<String> fileContentToSend = new Vector<>();
                for (String filename : filenames) {
                    if (filename.startsWith(String.valueOf(key_id))) {
                        filenamesToSend.add(filename);
                        fileContentToSend.add(getKeyFileContentByFileName(filename));
                    }
                }

                retrieved_key = key_id + "";
                this.info = "5" + "#" + retrieved_key;

                try {
                    requestSocket = new Socket(pred_node_ip, 3300);
                    out = new ObjectOutputStream(requestSocket.getOutputStream());

                    out.writeObject(this.info);
                    out.flush();
                    requestSocket.close();

                    Log.d("OfferServiceToUser", "Sending retrieved Key to new Node -----> " + this.info);

                    for (String filename : filenamesToSend) {
                        requestSocket = new Socket(pred_node_ip, 3300);
                        out = new ObjectOutputStream(requestSocket.getOutputStream());
                        out.writeObject("400#" + filename);
                        out.flush();
                        requestSocket.close();

                        Log.d("OfferServiceToUser", "Sending filename to new Node -----> " + filename);
                    }

                    for (String filecontent : fileContentToSend) {
                        requestSocket = new Socket(pred_node_ip, 3300);
                        out = new ObjectOutputStream(requestSocket.getOutputStream());
                        out.writeObject("401#" + filecontent);
                        out.flush();
                        requestSocket.close();

                        Log.d("OfferServiceToUser", "Sending filecontent to new Node -----> " + filecontent);
                    }

                    // delete the files that were sent to the predecessor
                    deleteKeyFiles(filenamesToSend);

                } catch (IOException ex) {
                    ex.printStackTrace();
                }

            }
        }

        try {
            requestSocket = new Socket(pred_node_ip, 3300);
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

        } catch (FileNotFoundException ex) {
            Log.e("KEY_FILE_NOT_FOUND", "Key file with this name does not exist!");
            retrievedText = "";
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        return retrievedText;
    }

    private void deleteKeyFiles(Vector<String> filenames) {
        String pathToKeys = Environment.getExternalStorageDirectory()
                + File.separator + "mobyChord/Node/Keys/";

        for (String filename : filenames) {
            File file = new File(pathToKeys + "/" + filename);
            file.delete();
        }
    }

    // a helper function that determines if the given id is between the range (low, high)
    private boolean isBetween(int id, int low, int high) {
        return (id > low && id < high);
    }

}
