package gr.aueb.cs.grad.mobychord.route;

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

    private final String routeFilename;
    private final LatLng sourceLocation;
    private final LatLng destinationLocation;
    private final String googleMapsKey;
    private String jsonRoute = "";

    public DownloadRoute(
            String routeFilename,
            LatLng sourceLocation,
            LatLng destinationLocation,
            String googleMapsKey
    ) {
        this.routeFilename = routeFilename;
        this.sourceLocation = sourceLocation;
        this.destinationLocation = destinationLocation;
        this.googleMapsKey = googleMapsKey;

        Log.d("SRC_LAT", String.valueOf(sourceLocation.latitude));
        Log.d("SRC_LONG", String.valueOf(sourceLocation.longitude));
        Log.d("DST_LAT", String.valueOf(destinationLocation.latitude));
        Log.d("DST_LONG", String.valueOf(destinationLocation.longitude));
    }

    @Override
    public void run() {
        System.out.println("Inside download route class.");

        Log.d("SRC_LOCATION", sourceLocation.toString());
        Log.d("DST_LOCATION", destinationLocation.toString());

        // Getting URL to the Google Directions API
        // This url should be obtained from one of the nodes.
        String url = getDirectionsUrl();
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

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private String getDirectionsUrl() {
        // Origin of route
        String str_origin = "origin=" + sourceLocation.latitude + "," + sourceLocation.longitude;

        // Destination of route
        String str_dest = "destination=" + destinationLocation.latitude + "," + destinationLocation.longitude;

        // Sensor enabled
        String sensor = "sensor=false";

        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + sensor;
        parameters += "&key=" + googleMapsKey;

        // Output format
        String output = "json";

        // Building the url to the web service
        return "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters;
    }


    /**
     * A method to download json data from url
     */
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

            StringBuffer stringBuffer = new StringBuffer();

            String line;
            while ((line = br.readLine()) != null) {
                stringBuffer.append(line);
            }

            data = stringBuffer.toString();

            br.close();
        } catch (Exception ex) {
            // Exception while downloading url
            Log.d("Exception", ex.toString());
        } finally {
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }

    // Fetches data from url passed
    private void downloadRouteData(String url) {

        // System.out.println("Download route function started");
        Log.d("DOWNLOAD_ROUTE", "Downloading route from Google...");

        // For storing data from web service.
        String data = "";

        try {
            // Fetching the data from web service.
            data = downloadUrl(url);
        } catch (Exception ex) {
            Log.d("Background Task", ex.toString());
        }

        jsonRoute = data;

        // Replace whitespaces to minimize String length.
        jsonRoute = jsonRoute.replaceAll("\\s+", "");

        Log.d("jsonRoute", jsonRoute);

        // Create file with route info.
        saveToFile(routeFilename + ".json", jsonRoute);

    }

    public String getJsonRoute() {
        return jsonRoute;
    }

}
