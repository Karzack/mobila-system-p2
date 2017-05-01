package inlamning.bjosve.p2;

/**
 * Created by bjorsven on 2017-04-30.
 */

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.util.Log;

/**
 * Created by Bjaern on 2017-04-27.
 */

public class CurrentLocation implements LocationListener {

    private MapsActivity mapsActivity;

    public CurrentLocation(MapsActivity activity){

        this.mapsActivity = activity;
    }
    @SuppressWarnings("MissingPermission")
    @Override
    public void onLocationChanged(Location location) {
        double longitude = location.getLongitude();
        double latitude = location.getLatitude();
        Log.v("Location Changed", latitude + " and " + longitude);
        mapsActivity.recieveLocationManager().removeUpdates(this);


    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }
}