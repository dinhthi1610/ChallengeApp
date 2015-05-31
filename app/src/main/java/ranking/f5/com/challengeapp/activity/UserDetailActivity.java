package ranking.f5.com.challengeapp.activity;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import ranking.f5.com.challengeapp.R;
import ranking.f5.com.challengeapp.utils.Constants;
import ranking.f5.com.challengeapp.utils.Utils;


public class UserDetailActivity extends Activity {

    private TextView mTvUsername;
    private ImageView mIvProfile;
    private TextView mTvCaption;
    private String mUsername, mProfileImage, mCaption;
    private ImageLoader mImageLoader;
    private ProgressDialog mProgressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_detail);
        mImageLoader = ImageLoader.getInstance();
        mImageLoader.init(ImageLoaderConfiguration.createDefault(this));

        mTvUsername = (TextView) findViewById(R.id.user_detail_tv_username);
        mIvProfile = (ImageView) findViewById(R.id.user_detail_iv_profile);
        mTvCaption = (TextView) findViewById(R.id.user_detail_tv_caption);

        if (getIntent().getExtras() != null) {
            mUsername = getIntent().getExtras().getString(Constants.KEY_USER_NAME);
            mTvUsername.setText(mUsername);
            if (mProgressDialog != null && mProgressDialog.isShowing()) {
                return;
            }

            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.show();

            initAuthenticationInstagram(getIntent().getExtras().getLong(Constants.USER_ID));
        }
    }


    public void initAuthenticationInstagram(long userId) {
        RequestQueue mQueue = Volley.newRequestQueue(getApplicationContext());
        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.GET,
                Utils.apiGetUserDetailById(userId), null, new Response.Listener<JSONObject>() {

            @TargetApi(Build.VERSION_CODES.KITKAT)
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONArray jsonArray = response.getJSONArray("data");
                    JSONObject object = jsonArray.getJSONObject(0).getJSONObject("caption");
                    mCaption = object.getString("text");

                    JSONObject image = jsonArray.getJSONObject(0).getJSONObject("images");
                    JSONObject standImage = image.getJSONObject("standard_resolution");
                    mProfileImage = standImage.getString("url");
                    mTvCaption.setText(mCaption);
                    mImageLoader.displayImage(mProfileImage, mIvProfile, new ImageLoadingListener() {
                        @Override
                        public void onLoadingStarted(String imageUri, View view) {

                        }

                        @Override
                        public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                            mProgressDialog.dismiss();
                        }

                        @Override
                        public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                            mProgressDialog.dismiss();
                        }

                        @Override
                        public void onLoadingCancelled(String imageUri, View view) {
                            mProgressDialog.dismiss();
                        }
                    });
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
            }
        });
        mQueue.add(jsonObjReq);
    }
}
