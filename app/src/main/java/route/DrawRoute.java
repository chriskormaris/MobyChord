package route;

import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import msc_aueb_gr_pol_liosis.mobychord.R;


//********************************* Polyvios Liosis ************************************//
//********************************* Christos Kormaris **********************************//
//********************************* Dimitris Botonakis *********************************//


public class DrawRoute extends FragmentActivity implements OnMapReadyCallback  {

    private GoogleMap mMap;
    private MarkerOptions options = new MarkerOptions();
    private ArrayList<LatLng> latlngs = new ArrayList<>();
    private ArrayList<Marker> markers = new ArrayList<>();

    private String srcPostalCode = "";
    private String dstPostalCode = "";
    private LatLng srcLocation;
    private LatLng dstLocation;

    private String jsonRoute = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route_map);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }


    // get location from a given location name
    public LatLng getLatLngFromLocationName(String locationName) {
        Geocoder geocoder = new Geocoder(this);
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

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        Bundle extras = getIntent().getExtras();
        if (extras.size() >= 3) {
            srcPostalCode = extras.getString("srcPostalCode");
            dstPostalCode = extras.getString("dstPostalCode");
            srcLocation = extras.getParcelable("srcLocation");
            dstLocation = extras.getParcelable("dstLocation");
            jsonRoute = extras.getString("routeInfo");

            Log.d("SRC_POSTAL_CODE", srcPostalCode);
            Log.d("DST_POSTAL_CODE", dstPostalCode);
            Log.d("jsonRoute", jsonRoute);
        }

//        srcLocation = getLatLngFromLocationName("GREECE " + srcPostalCode);
//        dstLocation = getLatLngFromLocationName("GREECE " + dstPostalCode);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);

        Log.d("SRC_LOCATION", srcLocation.toString());
        Log.d("DST_LOCATION", dstLocation.toString());

        // ADD MARKERS ON MAP
        latlngs.add(srcLocation);
        latlngs.add(dstLocation);

        options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN));
        options.position(srcLocation);
        options.title("POI" + 1);
        options.snippet(srcPostalCode);
        markers.add(mMap.addMarker(options));

        options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE));
        options.position(dstLocation);
        options.title("POI" + 2);
        options.snippet(dstPostalCode);
        markers.add(mMap.addMarker(options));

        ParserTask parserTask = new ParserTask();

        if (jsonRoute.startsWith("[")) {
            jsonRoute = jsonRoute.substring(1);
        }
        if (jsonRoute.endsWith("]")) {
            jsonRoute = jsonRoute.substring(0, jsonRoute.length()-2);
        }

        // Invokes the thread for parsing the JSON data
        parserTask.execute(jsonRoute);

    }

    /** A class to parse the Google Places in JSON format */
    private class ParserTask extends AsyncTask<String, Integer, List<HashMap<String, String>> > {

        // Parsing the data in non-ui thread
        @Override
        protected List<HashMap<String, String>> doInBackground(String... jsonRoute) {

            JSONObject jObject;
            List<HashMap<String, String>> route = null;

            try {
                jObject = new JSONObject(jsonRoute[0]);
                Log.d("jsonRoute", jsonRoute[0]);
                DirectionsJSONParser parser = new DirectionsJSONParser();

                // Starts parsing data
                route = parser.parse(jObject).get(0);

            } catch (Exception e) {
                e.printStackTrace();
            }
            return route;
        }

        // Executes in UI thread, after the parsing process
        @Override
        protected void onPostExecute(List<HashMap<String, String>> result) {
            ArrayList<LatLng> points;
            PolylineOptions lineOptions = null;
//            MarkerOptions markerOptions = new MarkerOptions();

            // Traversing through all the routes
            for (int i = 0; i < result.size(); i++) {
                points = new ArrayList<>();
                lineOptions = new PolylineOptions();

                // Fetching i-th route
                List<HashMap<String, String>> path = result;

                // Fetching all the points in i-th route
                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);
                }

                // Adding all the points in the route to LineOptions
                lineOptions.addAll(points);
                lineOptions.width(15);
                lineOptions.color(Color.RED);
            }

            // Drawing polyline in the Google Map for the i-th route
            mMap.addPolyline(lineOptions);

            // Move the camera to SRC Location
            //mMap.addMarker(new MarkerOptions().position(athens).title("AUEB").snippet("test"));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(srcLocation));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(12), 4000, null);
            mMap.setTrafficEnabled(true);

        }

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        // clear map
        mMap.clear();
    }

}
