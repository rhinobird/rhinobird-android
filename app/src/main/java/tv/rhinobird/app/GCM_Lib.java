package tv.rhinobird.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.io.IOException;

/**
 * Created by emilio on 5/28/15.
 */
public class GCM_Lib {
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;


    private static final String TAG = GCM_Lib.class.getSimpleName();

    private SharedPreferences sharedPrefs;
    private Context mContext;


    public GCM_Lib(Context context) {
        sharedPrefs = context.getSharedPreferences("RbPrefs", Context.MODE_PRIVATE);
        mContext = context;
    }

    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(mContext);
        if (resultCode != ConnectionResult.SUCCESS) {
            Log.i(TAG, "This device is not supported.");
            return false;
        }
        return true;
    }

    public void register() {
        if (checkPlayServices()) {
            if (!isRegistered()) {
                RegisterTask task = new RegisterTask();
                task.execute();
            }
        }

    }

    public boolean isRegistered() {
        String gcm_id = getRegistrationId();
        return !gcm_id.equals("");
    }

    public String getRegistrationId() {
        String gcm_id = sharedPrefs.getString("gcm_register", "");
        Log.d(TAG, "Current GCM: " + gcm_id);
        return gcm_id;
    }


    private void setRegistrationId(String regId) {
        Log.d(TAG, "Setting reg id " + regId);

        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putString("gcm_register", regId);
        editor.apply();
    }


    public void clearRegistrationId() {
        setRegistrationId("");
    }

    class RegisterTask extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... params) {
            GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(mContext);
            String msg = null;
            try {
                msg = gcm.register(BuildConfig.GCM_PROJECT_ID);
            } catch (IOException e) {
                e.printStackTrace();
                return "";
            }
            return msg;
        }

        @Override
        protected void onPostExecute(String s) {
            setRegistrationId(s);
        }
    }
}
