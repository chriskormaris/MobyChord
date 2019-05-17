package msc_aueb_gr_pol_liosis.mobychord;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.format.Formatter;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Locale;

import route.DrawRoute;
import route.GPSTracker;
import threads.RequestRouteFromNode;

import static android.R.attr.data;

//********************************* Polyvios Liosis ************************************//
//********************************* Christos Kormaris **********************************//
//********************************* Dimitris Botonakis *********************************//

public class RequestClient extends AppCompatActivity {

    private ServerSocket miniServerSocket;

    private String srcLocationName = "";
    private String dstLocationName = "";
    private String srcPostalCode = "";
    private String dstPostalCode = "";
    private LatLng srcLatLng;
    private LatLng dstLatLng;

    private RadioButton buttonGPS;
    private RadioButton buttonManual;

    private String initialAskNodeIP = "";
    private String passInfo = "";
    private String clientIpAddress = "";

    private GPSTracker gps = null;
    private Location gpsLocation = null;

    public Context getContext() {
        return this;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request);

        final Button findRoute = (Button) findViewById(R.id.routeButton);
        buttonGPS = (RadioButton) findViewById(R.id.buttonGPS);
        buttonManual = (RadioButton) findViewById(R.id.buttonManual);
        final ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar);


        final EditText srcLocation = (EditText) findViewById(R.id.srcLocation);

        buttonGPS.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                srcLocation.setEnabled(false);

            }
        });

        buttonManual.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                srcLocation.setEnabled(true);

            }
        });

        findRoute.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                boolean correctInput = readInputAndCreatePassInfo();

                if (correctInput) {

                    progressBar.setVisibility(View.VISIBLE);

                    findRoute.setEnabled(false);

                    // send request to initialAskNodeIP
                    beginClientRequestRouteFromNode();

                    openRetrieveRouteServer();
                }

            }
        });

    }

    public String getLocalIP() {
        WifiManager wifiMgr = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        WifiInfo wifiInfo = wifiMgr.getConnectionInfo();
        int ip = wifiInfo.getIpAddress();
        return Formatter.formatIpAddress(ip);
    }

    private boolean readInputAndCreatePassInfo() {

        boolean correctInput = true;

        clientIpAddress = ((EditText) findViewById(R.id.clientIP)).getText().toString();

        if (clientIpAddress.equals("")) {
            clientIpAddress = getLocalIP();
        }

        if (buttonGPS.isChecked()) {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
            gps = new GPSTracker(this);
            gpsLocation = gps.getLastKnownLocation();
            Log.d("READY", "GPS READY!");

            srcLatLng = new LatLng(gpsLocation.getLatitude(), gpsLocation.getLongitude());
            Log.d("GPS_LATLNG", srcLatLng.toString());
            srcPostalCode = getPostalCodeFromLatLng(srcLatLng);
            Log.d("GPS_POSTAL_CODE", srcPostalCode);
        } else {
            srcLocationName = ((EditText) findViewById(R.id.srcLocation)).getText().toString();
            Log.d("SRC_LOCATION_NAME", srcPostalCode);
            srcPostalCode = getPostalCodeFromLocationName(srcLocationName);
            Log.d("SRC_POSTAL_CODE", srcPostalCode);

            if (srcPostalCode.equals("")) {
                Toast.makeText(getContext(), "The start location must be an existing address",
                        Toast.LENGTH_LONG).show();
                return false;
            }

//            srcLatLng = getLatLngFromPostalCode("GREECE " + srcPostalCode);
            srcLatLng = getLatLngFromLocationName(srcLocationName);
            Log.d("SRC_LATLNG", srcLatLng.toString());
        }
        
        dstLocationName = ((EditText) findViewById(R.id.dstLocation)).getText().toString();
        dstPostalCode = getPostalCodeFromLocationName(dstLocationName);

        if (dstPostalCode.equals("")) {
            Toast.makeText(getContext(), "The destination location must be an existing address",
                    Toast.LENGTH_LONG).show();
            return false;
        }

//        dstLatLng = getLatLngFromPostalCode("GREECE " + dstPostalCode);
        dstLatLng = getLatLngFromLocationName(dstLocationName);

        initialAskNodeIP = ((EditText) findViewById(R.id.askNodeIP)).getText().toString();

        double srcLatitude = srcLatLng.latitude;
        double srcLongitude = srcLatLng.longitude;
        double dstLatitude = dstLatLng.latitude;
        double dstLongitude = dstLatLng.longitude;

        passInfo = "100#" + srcPostalCode + "#" + dstPostalCode
                + "#" + srcLatitude + "#" + srcLongitude
                + "#" + dstLatitude + "#" + dstLongitude
                + "#" + clientIpAddress + "#" + initialAskNodeIP;

        return correctInput;
    }

    private String getPostalCodeFromLatLng(LatLng latlng) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        Log.d("GEO_IS_PRESENT", geocoder.isPresent() + "");

        Address address;
        String postalCode = "";
        try {
            address = geocoder.getFromLocation(latlng.latitude, latlng.longitude, 1).get(0);
            if (address != null) {
                postalCode = address.getPostalCode();
            }
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        postalCode = postalCode.replace(" ", "");
        return postalCode;
    }

    // get location from a given location name
    public String getPostalCodeFromLocationName(String location) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        Log.d("GEO_IS_PRESENT", geocoder.isPresent() + "");

        Address address;
        String postalCode = "";
        try {
            address = geocoder.getFromLocationName(location, 1).get(0);
            if (address != null) {
                postalCode = address.getPostalCode();
            }
        } catch (IOException e1) {
            e1.printStackTrace();
        }

        if (postalCode == null) {
            postalCode = "";
        } else {
            // remove the space between digits in the postal code
            postalCode = postalCode.replace(" ", "");
        }
        return postalCode;
    }

    private LatLng getLatLngFromLocationName(String locationName) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        Log.d("IS_PRESENT", geocoder.isPresent() + "");

        double latitude = 0;
        double longitude = 0;
        try {
            Address address = geocoder.getFromLocationName(locationName, 1).get(0);
            if (address != null) {
                // Use the address as needed
                latitude = address.getLatitude();
                longitude = address.getLongitude();
                String message = String.format("Latitude: %f, Longitude: %f",
                        address.getLatitude(), address.getLongitude());
                Log.d("COORDINATES", message);
            } else {
                // Display appropriate message when Geocoder services are not available
                Log.d("COORDINATES", "ERROR");
            }
        } catch (IOException e) {
            // handle exception
        }
        return new LatLng(latitude, longitude);
    }

    private void beginClientRequestRouteFromNode() {

        RequestRouteFromNode requestRouteFromNodeThread = new RequestRouteFromNode(clientIpAddress, initialAskNodeIP, passInfo);
        requestRouteFromNodeThread.start();

    }

    private void openRetrieveRouteServer()
    {

        System.out.println("Opening RetrieveRouteServer...");

        Thread miniServerThread = new RetrieveRouteThread(getContext());
        miniServerThread.start();

    }

    private class RetrieveRouteThread extends Thread {

//        ServerSocket miniServerSocket = null;
        ObjectInputStream in = null;
        final int miniServerPort = 6666;
        String routeInfo = "";

        Context context;

        RetrieveRouteThread(Context context) {
            this.context = context;
        }

        @Override
        public void run() {
            System.out.println("RetrieveRouteThread started!");

            try {
                miniServerSocket = new ServerSocket(miniServerPort);

//                while (true) {

                Socket connection = miniServerSocket.accept();
                System.out.println("Connection established!");

                in = new ObjectInputStream(connection.getInputStream());

                routeInfo = (String) in.readObject();
                Log.d("retrievedRouteInfo", routeInfo);

//                }
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }

            /* GET ROUTE FROM CHORD AND DRAW. */
            Intent intent = new Intent(context, DrawRoute.class);
            //intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.putExtra("srcPostalCode", srcPostalCode);
            intent.putExtra("dstPostalCode", dstPostalCode);
            intent.putExtra("srcLocation", srcLatLng);
            intent.putExtra("dstLocation", dstLatLng);
            intent.putExtra("routeInfo", routeInfo);
            startActivity(intent);
            finish();
        }

    }

    @Override
    public void onResume() {
        super.onResume();

        try {
            if (miniServerSocket != null) {
                miniServerSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // make progress bar invisible
        ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar.setVisibility(View.INVISIBLE);

        // enable "find route" button
        Button findRoute = (Button) findViewById(R.id.routeButton);
        findRoute.setEnabled(true);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        try {
            if (miniServerSocket != null) {
                miniServerSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
