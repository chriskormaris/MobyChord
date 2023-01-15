package msc_aueb_gr_pol_liosis.mobychord;

import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.text.format.Formatter;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;

//********************************* Polyvios Liosis ************************************//
//********************************* Christos Kormaris **********************************//
//********************************* Dimitris Botonakis *********************************//

public class NewNodeInfoActivity extends AppCompatActivity {

    private EditText nodeID;
    private EditText nodeIP;
    private EditText predecessorID;
    private EditText predecessorIP;
    private EditText successorID;
    private EditText successorIP;
    private Button startButton;

    private String myID;
    private String ipAddress = null;

    private DBManager dbHelper;

    private boolean firstConnectedNode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_node_info);

        startButton = (Button) this.findViewById(R.id.startButton);

        startButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                InsertNewChordNode();
            }
        });

        dbHelper = new DBManager(this);

    }

    // Get the IP of the device/node
    private String getLocalIP() {
        /*
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces();
                 en.hasMoreElements(); ) {
                NetworkInterface networkInterface = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = networkInterface.getInetAddresses();
                     enumIpAddr.hasMoreElements(); ) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()) {
                        return inetAddress.getHostAddress();
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        */

        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        int ip = wifiInfo.getIpAddress();
        return Formatter.formatIpAddress(ip);
    }

    //Create text file in local storage of the device and update the DB
    private void createNetworkingFiles() {
        // Insert in database all nodes from 0 to 7 with their ips......by default id=0 and ip = 0.0.0.0
        dbHelper.addNodes();

        DataOutputStream fos;

        // Create file with the new Node's Info.
        try {
            fos = new DataOutputStream(new FileOutputStream(Environment.getExternalStorageDirectory() 
                    + "/mobyChord/Node/Net Architecture/myInfo.txt"));
            fos.writeBytes(myID + ":" + ipAddress);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Create file with predecessor's info.
        try {
            fos = new DataOutputStream(new FileOutputStream(Environment.getExternalStorageDirectory() 
                    + "/mobyChord/Node/Net Architecture/predInfo.txt"));

            if (predecessorID.getText().toString().equals("") && predecessorIP.getText().toString().equals("")) {
                fos.writeBytes(myID + ":" + ipAddress);
                firstConnectedNode = true;
            } else {
                fos.writeBytes(predecessorID.getText().toString() + ":" + predecessorIP.getText().toString());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Create file with successor's info.
        try {
            fos = new DataOutputStream(new FileOutputStream(Environment.getExternalStorageDirectory() 
                    + "/mobyChord/Node/Net Architecture/succInfo.txt"));

            if (successorID.getText().toString().equals("") && successorIP.getText().toString().equals("")) {
                fos.writeBytes(myID + ":" + ipAddress);
                firstConnectedNode = true;
            } else {
                fos.writeBytes(successorID.getText().toString() + ":" + successorIP.getText().toString());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        //Update the three Nodes . A node can actually see only his successor and his predecessor
        if (firstConnectedNode) {
            dbHelper.updateNode(myID, ipAddress);
        } else {
            dbHelper.updateNode(myID, ipAddress);
            dbHelper.updateNode(this.predecessorID.getText().toString(), this.predecessorIP.getText().toString());
            dbHelper.updateNode(this.successorID.getText().toString(), this.successorIP.getText().toString());
        }

    }

    private void readInput() {
        nodeID = (EditText) this.findViewById(R.id.nodeID);
        nodeIP = (EditText) this.findViewById(R.id.nodeIP);
        predecessorID = (EditText) this.findViewById(R.id.predID);
        predecessorIP = (EditText) this.findViewById(R.id.predIP);
        successorID = (EditText) this.findViewById(R.id.succID);
        successorIP = (EditText) this.findViewById(R.id.succIP);

        myID = nodeID.getText().toString();
        ipAddress = nodeIP.getText().toString();

        if (ipAddress.equals("")) {
            ipAddress = getLocalIP();
        }

        if (myID.equals("")) {
            myID = "0";
        }
    }

    //After creating the basic files will be trasferred in a new Activity in order to get to miniServer mode!!!
    private void InsertNewChordNode() {
        readInput();

        createNetworkingFiles();

        Intent intent = new Intent(this, Node.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

}
