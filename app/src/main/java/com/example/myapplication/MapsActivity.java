package com.example.myapplication;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.FragmentActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.example.myapplication.databinding.ActivityMapsBinding;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {
    private GoogleMap mMap;
    private ActivityMapsBinding binding;
    private List<Event> events = new ArrayList<>();

    private RequestQueue requestQueue;
    private Gson gson;

    private View categoryLayout; // Add this line

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        gson = new GsonBuilder().create();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        requestQueue = Volley.newRequestQueue(this);

        categoryLayout = findViewById(R.id.categoryLayout); // Initialize categoryLayout

        fetchEventsFromAPI();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.setIndoorEnabled(true);
        mMap.setTrafficEnabled(true);

        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setZoomGesturesEnabled(true);

        LatLng uitmArauLocation = new LatLng(6.517154, 100.214170);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(uitmArauLocation, 8));

        mMap.setInfoWindowAdapter(new CustomInfoWindowAdapter());

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                categoryLayout.setVisibility(View.GONE); // Hide category layout
                return false;
            }
        });

        mMap.setOnInfoWindowCloseListener(new GoogleMap.OnInfoWindowCloseListener() {
            @Override
            public void onInfoWindowClose(Marker marker) {
                categoryLayout.setVisibility(View.VISIBLE); // Show category layout
            }
        });
    }

    private void fetchEventsFromAPI() {
        String url = "http://192.168.0.240/EventFinder/all.php";

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        try {
                            for (int i = 0; i < response.length(); i++) {
                                JSONObject jsonObject = response.getJSONObject(i);
                                String title = jsonObject.getString("title");
                                String description = jsonObject.getString("description");
                                String eventDate = jsonObject.getString("event_date");
                                String categoryName = jsonObject.getString("category_name");
                                String ticketInfo = jsonObject.getString("ticket_info");
                                double latitude = jsonObject.getDouble("latitude");
                                double longitude = jsonObject.getDouble("longitude");
                                // Create Event object and add to list
                                Event event = new Event(title, description, eventDate, categoryName, ticketInfo, latitude, longitude);
                                events.add(event);
                            }

                            addMarkersAndAdjustCamera(events);

                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(MapsActivity.this, "Error parsing JSON: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(MapsActivity.this, "Error fetching events: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

        requestQueue.add(jsonArrayRequest);
    }

    private void addMarkersAndAdjustCamera(List<Event> events) {
        if (mMap == null) return;

        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (Event event : events) {
            LatLng eventLocation = new LatLng(event.getLatitude(), event.getLongitude());

            // Determine marker color based on category_name
            float markerColor = BitmapDescriptorFactory.HUE_RED;
            switch (event.getCategoryName()) {
                case "Festival":
                    markerColor = BitmapDescriptorFactory.HUE_RED;
                    break;
                case "Sport":
                    markerColor = BitmapDescriptorFactory.HUE_BLUE;
                    break;
                case "Music & Concert":
                    markerColor = BitmapDescriptorFactory.HUE_GREEN;
                    break;
                // Add more cases for additional categories if needed
            }

            mMap.addMarker(new MarkerOptions()
                    .position(eventLocation)
                    .title(event.getTitle())
                    .snippet(event.getDescription() + "\n" + event.getEventDate() + "\n" + event.getCategoryName() + "\n" + event.getTicketInfo())
                    .icon(BitmapDescriptorFactory.defaultMarker(markerColor))); // Set marker color

            builder.include(eventLocation); // Include marker in bounds calculation
        }

        int padding = 200; // Adjust padding as needed
        LatLngBounds bounds = builder.build();
        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
        mMap.moveCamera(cu); // Move camera to include all markers
    }

    private class CustomInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {
        private final View infoWindow;

        CustomInfoWindowAdapter() {
            infoWindow = getLayoutInflater().inflate(R.layout.custom_info_window, null);
        }

        @Override
        public View getInfoWindow(Marker marker) {
            return null;
        }

        @Override
        public View getInfoContents(Marker marker) {
            TextView title = infoWindow.findViewById(R.id.title);
            TextView description = infoWindow.findViewById(R.id.description);
            TextView eventDate = infoWindow.findViewById(R.id.event_date);
            TextView categoryName = infoWindow.findViewById(R.id.category_name);
            TextView ticketInfo = infoWindow.findViewById(R.id.ticket_info);

            title.setText(marker.getTitle());
            String[] snippet = marker.getSnippet().split("\n");
            description.setText(snippet[0]);
            eventDate.setText(snippet[1]);
            categoryName.setText(snippet[2]);
            ticketInfo.setText(snippet[3]);

            return infoWindow;
        }
    }

    private static class Event {
        private final String title;
        private final String description;
        private final String eventDate;
        private final String categoryName;
        private final String ticketInfo; // Add this line
        private final double latitude;
        private final double longitude;

        public Event(String title, String description, String eventDate, String categoryName, String ticketInfo, double latitude, double longitude) {
            this.title = title;
            this.description = description;
            this.eventDate = eventDate;
            this.categoryName = categoryName;
            this.ticketInfo = ticketInfo;
            this.latitude = latitude;
            this.longitude = longitude;
        }

        public String getTitle() {
            return title;
        }

        public String getDescription() {
            return description;
        }

        public String getEventDate() {
            return eventDate;
        }

        public String getCategoryName() {
            return categoryName;
        }

        public String getTicketInfo() {
            return ticketInfo;
        }

        public double getLatitude() {
            return latitude;
        }

        public double getLongitude() {
            return longitude;
        }
    }
}
