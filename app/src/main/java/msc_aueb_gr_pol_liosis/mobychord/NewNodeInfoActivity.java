package msc_aueb_gr_pol_liosis.mobychord;

import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.text.format.Formatter;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;

//********************************* Polyvios Liosis ************************************//
//********************************* Christos Kormaris **********************************//
//********************************* Dimitris Botonakis *********************************//

public class NewNodeInfoActivity extends AppCompatActivity {

    private EditText nodeID;
    private EditText nodeIP;
    private EditText predID;
    private EditText predIP;
    private EditText succID;
    private EditText succIP;
    private Button startButton;

    private String myID;
    private String ipAddress = null;

    private DBManager db_helper;

    private boolean firstConnectedNode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_node_info);

        startButton = (Button) this.findViewById(R.id.startButton);

        startButton.setOnClickListener(new View.OnClickListener()
        {
            public void onClick (View v)
            {
                InsertNewChordNode();
            }
        });

        db_helper = new DBManager(this);

    }

    //Get the IP of the device/Node
    private String getLocalIP()
    {
//        try{
//            for(Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
//                NetworkInterface intf = en.nextElement();
//                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
//                    InetAddress inetAddress = enumIpAddr.nextElement();
//                    if (!inetAddress.isLoopbackAddress()) {
//                        return inetAddress.getHostAddress();
//                    }
//                }
//            }
//
//        }
//        catch (SocketException e)
//        {
//            e.printStackTrace();
//
//        }

        WifiManager wifiMgr = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        WifiInfo wifiInfo = wifiMgr.getConnectionInfo();
        int ip = wifiInfo.getIpAddress();
        return Formatter.formatIpAddress(ip);
    }

    //Create text file in local storage of the device and update the DB
    private void createNetworkingFiles()
    {
        //Insert in database all nodes from 0 to 7 with their ips......by default id=0 and ip = 0.0.0.0
        db_helper.addNodes();

        DataOutputStream fos;

        //create file with the new Node's Info
        try
        {
            fos  = new DataOutputStream( new FileOutputStream(Environment.getExternalStorageDirectory() + "/mobyChord/Node/Net Architecture/myInfo.txt"));

            fos.writeBytes(myID + ":" + ipAddress);
        }
        catch(IOException e)
        {
           e.printStackTrace();
        }

        //create file with predecessor's info
        try
        {

            fos  = new DataOutputStream( new FileOutputStream(Environment.getExternalStorageDirectory() + "/mobyChord/Node/Net Architecture/predInfo.txt"));

            if(predID.getText().toString().equals("") && predIP.getText().toString().equals("") )
            {

                fos.writeBytes(myID + ":" + ipAddress);

                firstConnectedNode = true;
            }
            else
            {
                fos.writeBytes(predID.getText().toString() + ":" + predIP.getText().toString());
            }

        }
        catch(IOException e)
        {
            e.printStackTrace();
        }

        //create file with successor's info
        try
        {

            fos  = new DataOutputStream( new FileOutputStream(Environment.getExternalStorageDirectory() + "/mobyChord/Node/Net Architecture/succInfo.txt"));

            if(succID.getText().toString().equals("") && succIP.getText().toString().equals("") )
            {
                fos.writeBytes(myID + ":" + ipAddress);

                firstConnectedNode = true;
            }
            else
            {
                fos.writeBytes(succID.getText().toString() + ":" + succIP.getText().toString());
            }

        }
        catch(IOException e)
        {
            e.printStackTrace();
        }

        //Update the three Nodes . A node can actually see only his successor and his predecessor
        if(firstConnectedNode)
        {
            db_helper.updateNode(myID, ipAddress);
        }
        else
        {
            db_helper.updateNode(myID, ipAddress);
            db_helper.updateNode(this.predID.getText().toString(), this.predIP.getText().toString());
            db_helper.updateNode(this.succID.getText().toString(), this.succIP.getText().toString());
        }

    }

    private void readInput() {
        nodeID = (EditText) this.findViewById(R.id.nodeID);
        nodeIP = (EditText) this.findViewById(R.id.nodeIP);
        predID = (EditText) this.findViewById(R.id.predID);
        predIP = (EditText) this.findViewById(R.id.predIP);
        succID = (EditText) this.findViewById(R.id.succID);
        succIP = (EditText) this.findViewById(R.id.succIP);

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
    private void InsertNewChordNode()
    {
        readInput();

        createNetworkingFiles();

        Intent intent = new Intent(this, Node.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

}
