package inlamning.bjosve.p2;

import android.Manifest;
import android.app.Dialog;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.support.annotation.MainThread;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;



public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private LocationManager mLocationManager;
    private SocketHandler socketHandler;
    private SupportMapFragment mapFragment;
    private LinearLayout llTexts;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
        socketHandler = new SocketHandler(this);
        socketHandler.start();

        llTexts = (LinearLayout)findViewById(R.id.llTexts);
        Button btnMessage = (Button)findViewById(R.id.btnMessage);
        btnMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final Dialog dialog = new Dialog(MapsActivity.this);
                dialog.setContentView(R.layout.show_single_list_item);
                DbItem dbItem = adapter.getItem(position);
                TextView tvDate = (TextView) dialog.findViewById(R.id.tvDate);
                TextView tvCategory = (TextView)dialog.findViewById(R.id.tvCategory);
                TextView tvAmount = (TextView)dialog.findViewById(R.id.tvAmount);
                tvDate.setText("Date: " + dbItem.getDate());
                tvCategory.setText("Categoty: " + dbItem.getCategory());
                tvAmount.setText("Amount: " + dbItem.getAmount() + " kr");
                dialog.show();
                final int N = 10; // total number of textviews to add

                final TextView[] myTextViews = new TextView[N]; // create an empty array;

                for (int i = 0; i < N; i++) {
                    // create a new textview
                    final TextView rowTextView = new TextView(MapsActivity.this);

                    // set some properties of rowTextView or something
                    rowTextView.setText("This is row #" + i);

                    // add the textview to the linearlayout
                    llTexts.addView(rowTextView);

                    // save a reference to the textview for later
                    myTextViews[i] = rowTextView;
            }
        }});
        mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onResume(){
        super.onResume();

    }
    @Override
    public void onPause(){
        super.onPause();
    }
    @Override
    public void onDestroy(){
        super.onDestroy();
        socketHandler.closeSocket();
    }

    private Location getLocationString() {
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
        CurrentLocation listener = new CurrentLocation(this);
        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, listener);
        Location currentLocation = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        try {
            mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

            // getting GPS status
            boolean isGPSEnabled = mLocationManager
                    .isProviderEnabled(LocationManager.GPS_PROVIDER);

            // getting network status
            boolean isNetworkEnabled = mLocationManager
                    .isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (!isGPSEnabled && !isNetworkEnabled) {
                // no network provider is enabled
            } else {
                boolean canGetLocation = true;
                if (isNetworkEnabled) {
                    mLocationManager.requestLocationUpdates(
                            LocationManager.NETWORK_PROVIDER,
                            0,
                            0, listener);
                    Log.d("Network", "Network Enabled");
                    if (mLocationManager != null) {
                        currentLocation = mLocationManager
                                .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                        if (currentLocation != null) {
                            double latitude = currentLocation.getLatitude();
                            double longitude = currentLocation.getLongitude();
                        }
                    }
                }
                // if GPS Enabled get lat/long using GPS Services
                if (isGPSEnabled) {
                    if (currentLocation == null) {
                        mLocationManager.requestLocationUpdates(
                                LocationManager.GPS_PROVIDER,
                                0,
                                0, listener);
                        Log.d("GPS", "GPS Enabled");
                        if (mLocationManager != null) {
                            currentLocation = mLocationManager
                                    .getLastKnownLocation(LocationManager.GPS_PROVIDER);
                            if (currentLocation != null) {
                                double latitude = currentLocation.getLatitude();
                                double longitude = currentLocation.getLongitude();
                            }
                        }
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        mLocationManager.removeUpdates(listener);
        return currentLocation;
    }



    public LocationManager recieveLocationManager(){
        return mLocationManager;
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
        Location theLocation = getLocationString();
        // Add a marker in Sydney and move the camera
        LatLng location = new LatLng(theLocation.getLatitude(), theLocation.getLongitude());
        mMap.addMarker(new MarkerOptions().position(location).title("Bjaern is here!"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(location));
    }
}
