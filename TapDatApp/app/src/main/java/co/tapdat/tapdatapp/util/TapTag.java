package co.tapdat.tapdatapp.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import org.apache.http.client.HttpResponseException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by arash on 9/23/14.
 */
public class TapTag {
    private String mName;
    private String TagID;
    private String mAuthToken;

    private final static String TAP_TAGS_API_ENDPOINT_URL = "http://10.0.2.2:3000/mobile/1/nfc_tags.json";

    // way of holding payloads (multiple array? )  string for URL, threshold
    public String newTag(Context mContext, String AuthToken){

        return "baldi";

    }


    private class newTagTask extends UrlJsonAsyncTask {
        public newTagTask(Context context) {
            super(context);
        }

        @Override
        protected JSONObject doInBackground(String... urls) {
            DefaultHttpClient client = new DefaultHttpClient();
            HttpPost post = new HttpPost(urls[0]);
            JSONObject holder = new JSONObject();
            JSONObject userObj = new JSONObject();
            String response = null;
            JSONObject json = new JSONObject();

            try {
                try {
                    // setup the returned values in case
                    // something goes wrong
                    json.put("success", false);
                    json.put("info", "Something went wrong. Retry!");

                    // add the users's info to the post params
                    userObj.put("auth_token", mAuthToken);
                    holder.put("user", userObj);
                    StringEntity se = new StringEntity(holder.toString());
                    post.setEntity(se);

                    // setup the request headers
                    post.setHeader("Accept", "application/json");
                    post.setHeader("Content-Type", "application/json");

                    ResponseHandler<String> responseHandler = new BasicResponseHandler();
                    response = client.execute(post, responseHandler);
                    json = new JSONObject(response);

                } catch (HttpResponseException e) {
                    e.printStackTrace();
                    Log.e("ClientProtocol", "" + e);
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.e("IO", "" + e);
                }
            } catch (JSONException e) {
                e.printStackTrace();
                Log.e("JSON", "" + e);
            }


            return json;
        }

        @Override
        protected void onPostExecute(JSONObject json) {
            try {
                if (!json.getString("response").isEmpty()) {
                    // everything is ok
                    //SharedPreferences.Editor editor = mPreferences.edit();
                    // save the returned auth_token into
                    // the SharedPreferences
                  //  editor.putString("AuthToken", json.getJSONObject("response").getString("id"));
                   // editor.putString("NickName", json.getJSONObject("response").getString("name"));
                 //   editor.commit();

                    // launch the HomeActivity and close this one

                }
                //Toast.makeText(context, json.getString("info"), Toast.LENGTH_LONG).show();
            } catch (Exception e) {
                // something went wrong: show a Toast
                // with the exception message
                Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
            } finally {
                super.onPostExecute(json);
            }
        }
    }


}
