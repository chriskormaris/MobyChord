package route;

import android.os.Environment;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

//********************************* Polyvios Liosis ************************************//
//********************************* Christos Kormaris **********************************//
//********************************* Dimitris Botonakis *********************************//


public class DownloadRoute extends Thread {

    private String routeFilename = "";
    private LatLng srcLocation;
    private LatLng dstLocation;

    private String jsonRoute = "";


    public DownloadRoute(String routeFilename,
                         LatLng srcLocation, LatLng dstLocation) {
        this.routeFilename = routeFilename;
        this.srcLocation = srcLocation;
        this.dstLocation = dstLocation;

        Log.d("srcLocation latitude", srcLocation.latitude + "");
        Log.d("srcLocation longitude", srcLocation.longitude + "");
        Log.d("dstLocation latitude", dstLocation.latitude + "");
        Log.d("dstLocation longitude", dstLocation.longitude + "");

    }

    @Override
    public void run() {

        System.out.println("Inside download route class.");

        Log.d("SRC_LOCATION", srcLocation.toString());
        Log.d("DST_LOCATION", dstLocation.toString());

        // Getting URL to the Google Directions API
        // This url should be obtained from one of the nodes.
        String url = getDirectionsUrl(srcLocation, dstLocation);
        Log.d("URL", url);

        // Get the route in JSON format and
        // start downloading json data from Google Directions API.
        downloadRouteData(url);
    }

    private void saveToFile(String filename, String data) {
        try {

            File file = new File(Environment.getExternalStorageDirectory()
                    + "/mobyChord/Node/", "Keys");
            // if dirPath exists and folder with name Keys
            if (!file.exists()) {
                file.mkdirs(); // this will create folder.
            }
            File filepath = new File(file, filename);  // file path to save
            FileWriter writer = new FileWriter(filepath);
            writer.write(data);
            writer.flush();
            writer.close();
            Log.d("FILE_CREATED", "File generated with name \"" + filename + "\"");

        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    private String getDirectionsUrl(LatLng origin, LatLng dest){


        // Origin of route
        String str_origin = "origin="+origin.latitude+","+origin.longitude;

        // Destination of route
        String str_dest = "destination="+dest.latitude+","+dest.longitude;

        // Sensor enabled
        String sensor = "sensor=false";

        // Building the parameters to the web service
        String parameters = str_origin+"&"+str_dest+"&"+sensor;

        // Output format
        String output = "json";

        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/"+output+"?"+parameters;

        return url;
    }


    /** A method to download json data from url */
    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(strUrl);

            // Creating an http connection to communicate with url
            urlConnection = (HttpURLConnection) url.openConnection();

            // Connecting to url
            urlConnection.connect();

            // Reading data from url
            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuffer sb = new StringBuffer();

            String line = "";
            while( ( line = br.readLine()) != null){
                sb.append(line);
            }

            data = sb.toString();

            br.close();

        } catch(Exception e){
            // Exception while downloading url
            Log.d("Exception", e.toString());
        } finally{
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }

    // Fetches data from url passed
    private void downloadRouteData(String url) {

//        System.out.println("Download route function started");
        Log.d("DOWNLOAD_ROUTE", "Downloading route from Google...");

        // For storing data from web service
        String data = "";

        try{
            // Fetching the data from web service
            data = downloadUrl(url);
        } catch(Exception e){
            Log.d("Background Task",e.toString());
        }

        jsonRoute = data;

        // replace whitespaces to minimize String length
        jsonRoute = jsonRoute.replaceAll("\\s+", "");

        Log.d("jsonRoute", jsonRoute);

        //create file with route info
        saveToFile(routeFilename + ".json", jsonRoute);

    }

    public String getJsonRoute() {
        return jsonRoute;
    }

}
