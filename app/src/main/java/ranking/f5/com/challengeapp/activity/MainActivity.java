package ranking.f5.com.challengeapp.activity;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import ranking.f5.com.challengeapp.R;
import ranking.f5.com.challengeapp.model.LocationEntity;
import ranking.f5.com.challengeapp.utils.Constants;

/**
 * @author Dinhthi
 *         This class will implement Map view v2 and feed instagram posts
 */
public class MainActivity extends Activity implements OnMapReadyCallback {

    private List<LocationEntity> mLocationEntities = new ArrayList<>();
    private float mZoom = 15;
    private Location mLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.main_mapview);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(final GoogleMap map) {
        map.setMyLocationEnabled(true);

        LocationManager service = (LocationManager) getSystemService(LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        String provider = service.getBestProvider(criteria, false);
        Location location = service.getLastKnownLocation(provider);
        LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(userLocation, mZoom);
        map.animateCamera(cameraUpdate);
        initAuthenticationInstagram(userLocation, map);
        map.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition cameraPosition) {
                map.clear();
                LatLng center = cameraPosition.target;
                initAuthenticationInstagram(center, map);
            }
        });
    }

    public void initAuthenticationInstagram(LatLng location, final GoogleMap map) {
        String endPointURL = "https://api.instagram.com/v1/media/search?distance=500&&client_id=" + Constants.CLIENT_ID + "&lat=" + location.latitude + "&lng=" + location.longitude;
        RequestQueue mQueue = Volley.newRequestQueue(getApplicationContext());
        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.GET,
                endPointURL, null, new Response.Listener<JSONObject>() {

            @TargetApi(Build.VERSION_CODES.KITKAT)
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONArray jsonArray = response.getJSONArray("data");
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject location = jsonArray.getJSONObject(i).getJSONObject("location");
                        LocationEntity locationEntity = new LocationEntity();
                        locationEntity.setLat(location.getDouble("latitude"));
                        locationEntity.setLng(location.getDouble("longitude"));

                        JSONObject user = jsonArray.getJSONObject(i).getJSONObject("user");
                        locationEntity.setName(user.getString("full_name"));
                        locationEntity.setId(user.getLong("id"));
                        locationEntity.setProfileImage(user.getString("profile_picture"));
                        locationEntity.setUserName(user.getString("username"));
                        mLocationEntities.add(locationEntity);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                if (mLocationEntities != null && mLocationEntities.size() != 0) {
                    for (int i = 0; i < mLocationEntities.size(); i++) {
                        LatLng objectMaker = new LatLng(mLocationEntities.get(i).getLat(), mLocationEntities.get(i).getLng());
                        map.addMarker(new MarkerOptions().position(objectMaker)
                                .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_maker))
                                .anchor(0.0f, 1.0f)
                                .title(mLocationEntities.get(i).getName()));
                    }
                }

                map.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                    @Override
                    public void onInfoWindowClick(Marker marker) {
                        Intent userDetailIntent = new Intent(MainActivity.this, UserDetailActivity.class);
                        if (mLocationEntities != null && mLocationEntities.size() != 0) {
                            for (int i = 0; i < mLocationEntities.size(); i++) {
                                if (marker.getTitle().trim().equalsIgnoreCase(mLocationEntities.get(i).getName())) {
                                    userDetailIntent.putExtra(Constants.KEY_USER_NAME, mLocationEntities.get(i).getUserName());
                                    userDetailIntent.putExtra(Constants.USER_ID, mLocationEntities.get(i).getId());
                                    startActivity(userDetailIntent);
                                    break;
                                }
                            }
                        }
                    }
                });

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
            }
        });
        mQueue.add(jsonObjReq);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }


}
