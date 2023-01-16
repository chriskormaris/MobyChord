package grad.cs.aueb.gr.mobychord.route;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DirectionsJSONParser {

    List<List<Map<String, String>>> routes = null;
    private JSONArray jRoutes = null;

    /**
     * Receives a JSONObject and returns a list of lists containing latitude and longitude
     */
    public List<List<Map<String, String>>> parse(JSONObject jObject) {

        routes = new ArrayList<>();
        JSONArray jLegs;
        JSONArray jSteps;

        try {

            jRoutes = jObject.getJSONArray("routes");
            Log.d("JSON", jRoutes.get(0).toString());
            // Log.d("JSON", jRoutes.toString());

            /** Traversing all routes */
            for (int i = 0; i < jRoutes.length(); i++) {
                jLegs = ((JSONObject) jRoutes.get(i)).getJSONArray("legs");
                List<Map<String, String>> path = new ArrayList<>();

                /** Traversing all legs */
                for (int j = 0; j < jLegs.length(); j++) {
                    jSteps = ((JSONObject) jLegs.get(j)).getJSONArray("steps");

                    /** Traversing all steps */
                    for (int k = 0; k < jSteps.length(); k++) {
                        String polyline = "";
                        polyline = (String) ((JSONObject) ((JSONObject) jSteps.get(k)).get("polyline")).get("points");
                        List<LatLng> list = decodePoly(polyline);

                        /** Traversing all points */
                        for (int l = 0; l < list.size(); l++) {
                            Map<String, String> hashMap = new HashMap<>();
                            hashMap.put("lat", Double.toString(list.get(l).latitude));
                            hashMap.put("lng", Double.toString(list.get(l).longitude));
                            path.add(hashMap);
                        }
                    }
                    routes.add(path);
                }
            }

        } catch (JSONException ex) {
            ex.printStackTrace();
        }

        Log.d("ROUTES SIZE", String.valueOf(routes.size()));
        return routes;
    }

    /**
     * Method to decode polyline points
     * Courtesy : http://jeffreysambells.com/2010/05/27/decoding-polylines-from-google-maps-direction-api-with-java
     */
    private List<LatLng> decodePoly(String encoded) {
        List<LatLng> poly = new ArrayList<>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng((((double) lat / 1E5)),
                    (((double) lng / 1E5)));
            poly.add(p);
        }

        return poly;
    }

    public List<Map<String, String>> getFirstRoute() {
        return routes.get(0);
    }

    public JSONArray getJRoutes() {
        return jRoutes;
    }

}
