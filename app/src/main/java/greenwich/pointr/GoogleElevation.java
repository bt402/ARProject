package greenwich.pointr;

import com.google.api.client.googleapis.GoogleHeaders;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.http.json.JsonHttpParser;
import com.google.api.client.json.jackson.JacksonFactory;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.ArrayList;

public class GoogleElevation {
    private static final String ELEVATION_TAG = "elevation";

    private static final String API_KEY = "AIzaSyDGeooGmYLYSSX9P9zl9dWEXnUC2Dkpj9U";
    private static final String ELEVATION_SEARCH_URL = "https://maps.googleapis.com/maps/api/elevation/json?";
    private static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
    static String elevationStr = "";
    public static ArrayList<Double> elevationList;


    public static double getElevation(double longitude, double latitude){

        try {

            HttpRequestFactory httpRequestFactory = createRequestFactory(HTTP_TRANSPORT);
            HttpRequest request = httpRequestFactory
                    .buildGetRequest(new GenericUrl(ELEVATION_SEARCH_URL));
            request.getUrl().put("locations", longitude + "," + latitude);
            request.getUrl().put("key", API_KEY);
            String URL = ""+request.getUrl();

            HttpClient client = new DefaultHttpClient();
            HttpGet req = new HttpGet();
            req.setURI(new URI(URL));
            HttpResponse response = client.execute(req);
            BufferedReader in = new BufferedReader(new InputStreamReader(response
                    .getEntity().getContent()));
            String line = "";

            while ((line = in.readLine()) != null) {
                if (line.contains(ELEVATION_TAG)){
                    String[] arr = line.split(" ");
                    elevationStr = arr[11].substring(0,arr[11].length()-1);
                }

            }
        }
        catch (Exception e){
            System.out.println(e);
        }
            return Double.parseDouble(elevationStr);
    }


    public static void getElevation(ArrayList<String> positions){

        String coordinatesBuilder = "";
        elevationList = new ArrayList<>();

        for (int i = 0; i < positions.size(); i++){
            // dis[0] = lat, dis[1] = long
            String[] dis = positions.get(i).split(" ");
            String poiLat = dis[0];
            String poiLon = dis[1];
            coordinatesBuilder += poiLat + "," + poiLon + "|";
        }

        // remove last pipe
        if (coordinatesBuilder != null && coordinatesBuilder.length() > 0 && coordinatesBuilder.charAt(coordinatesBuilder.length()-1)=='|') {
            coordinatesBuilder = coordinatesBuilder.substring(0, coordinatesBuilder.length()-1);
        }

                try {

                    HttpRequestFactory httpRequestFactory = createRequestFactory(HTTP_TRANSPORT);
                    HttpRequest request = httpRequestFactory
                            .buildGetRequest(new GenericUrl(ELEVATION_SEARCH_URL));
                    request.getUrl().put("locations", coordinatesBuilder);
                    request.getUrl().put("key", API_KEY);
                    String URL = ""+request.getUrl();

                    HttpClient client = new DefaultHttpClient();
                    HttpGet req = new HttpGet();
                    req.setURI(new URI(URL));
                    HttpResponse response = client.execute(req);
                    BufferedReader in = new BufferedReader(new InputStreamReader(response
                            .getEntity().getContent()));
                    String line = "";

                    while ((line = in.readLine()) != null) {
                        if (line.contains(ELEVATION_TAG)){
                            String[] arr = line.split(" ");
                            elevationStr = arr[11].substring(0,arr[11].length()-1);
                            elevationList.add(Double.parseDouble(elevationStr));
                        }

                    }
                }
                catch (Exception e){
                    System.out.println(e);
                }

    }

    public static HttpRequestFactory createRequestFactory(
            final HttpTransport transport) {
        return transport.createRequestFactory(new HttpRequestInitializer() {
            public void initialize(HttpRequest request) {
                GoogleHeaders headers = new GoogleHeaders();
                headers.setApplicationName("Test");
                request.setHeaders(headers);
                JsonHttpParser parser = new JsonHttpParser(new JacksonFactory());
                request.addParser(parser);
            }
        });
    }
}
