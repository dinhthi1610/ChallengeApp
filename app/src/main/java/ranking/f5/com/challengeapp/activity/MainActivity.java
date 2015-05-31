package ranking.f5.com.challengeapp.activity;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;

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
import ranking.f5.com.challengeapp.utils.Utils;

/**
 * @author Dinhthi
 *         This class will implement Map view v2 and feed instagram posts
 */
public class MainActivity extends Activity implements OnMapReadyCallback {

    private List<LocationEntity> mLocationEntities;

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
        //====================get current location feed post======
        LocationManager service = (LocationManager) getSystemService(LOCATION_SERVICE);
        Location location = service.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
        LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());
        //========================================================

        // will move camera to current location and load last posts of instagram on google maps.
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(userLocation, 15);
        map.animateCamera(cameraUpdate);
        map.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition cameraPosition) {
                initDataAndLoadMarkerOnMap(cameraPosition.target, map);
            }
        });
    }

    /**
     * This method using to load instagram post from search API wit current location and distance.
     * After has data, the map will load all marker with information and move to details screen when touch on each of marker.
     *
     * @param location is camera position when user drag map. It's mean when user move the camera. will load new feed for this location.
     * @param map      is google map
     */
    public void initDataAndLoadMarkerOnMap(LatLng location, final GoogleMap map) {
        RequestQueue mQueue = Volley.newRequestQueue(getApplicationContext());
        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.GET,
                Utils.apiSearchFriend(location, 1000), null, new Response.Listener<JSONObject>() {

            @TargetApi(Build.VERSION_CODES.KITKAT)
            @Override
            public void onResponse(JSONObject response) {
                try {
                    mLocationEntities = new ArrayList<>();
                    JSONArray jsonArray = response.getJSONArray("data");
                    for (int i = 0; i < jsonArray.length(); i++) {
                        // get Location object from json result
                        JSONObject location = jsonArray.getJSONObject(i).getJSONObject("location");
                        LocationEntity locationEntity = new LocationEntity();
                        locationEntity.setLat(location.getDouble("latitude"));
                        locationEntity.setLng(location.getDouble("longitude"));

                        // get User object from json result
                        JSONObject user = jsonArray.getJSONObject(i).getJSONObject("user");
                        locationEntity.setName(user.getString("full_name"));
                        locationEntity.setId(user.getLong("id"));
                        locationEntity.setProfileImage(user.getString("profile_picture"));
                        locationEntity.setUserName(user.getString("username"));

                        // sometime will have some duplication from response result, we will check it and store if not existing.
                        boolean isAdd = false;
                        if (mLocationEntities.size() > 1) {
                            for (int j = 0; j < mLocationEntities.size(); j++) {
                                if (!mLocationEntities.get(j).getUserName().equalsIgnoreCase(locationEntity.getUserName())) {
                                    isAdd = true;
                                } else {
                                    isAdd = false;
                                    break;
                                }
                            }

                            if (isAdd) {
                        mLocationEntities.add(locationEntity);
                    }

                        } else {
                            mLocationEntities.add(locationEntity);
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                if (mLocationEntities != null && mLocationEntities.size() != 0) {
                    String titleName;
                    LatLng objectMaker;
                    for (int i = 0; i < mLocationEntities.size(); i++) {
                        objectMaker = new LatLng(mLocationEntities.get(i).getLat(), mLocationEntities.get(i).getLng());

                        // sometime user return with null name, will use username to show for details.
                        if (mLocationEntities.get(i).getName().trim().length() != 0) {
                            titleName = mLocationEntities.get(i).getName();
                        } else {
                            titleName = mLocationEntities.get(i).getUserName();
                        }

                        // add new marker
                        map.addMarker(new MarkerOptions().position(objectMaker)
                                .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_maker))
                                .title(titleName));
                    }
                }

                map.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                    @Override
                    public void onInfoWindowClick(Marker marker) {
                        Intent userDetailIntent = new Intent(MainActivity.this, UserDetailActivity.class);
                            for (int i = 0; i < mLocationEntities.size(); i++) {
                                if (marker.getTitle().trim().equalsIgnoreCase(mLocationEntities.get(i).getName())
                                        || marker.getTitle().trim().equalsIgnoreCase(mLocationEntities.get(i).getUserName())) {
                                    userDetailIntent.putExtra(Constants.KEY_USER_NAME, mLocationEntities.get(i).getUserName());
                                    userDetailIntent.putExtra(Constants.USER_ID, mLocationEntities.get(i).getId());
                                    startActivity(userDetailIntent);
                                    break;
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
    protected void onDestroy() {
        super.onDestroy();
        // clear list to performance memory
        if (mLocationEntities != null) {
            mLocationEntities.clear();
            mLocationEntities = null;
        }
    }
}
