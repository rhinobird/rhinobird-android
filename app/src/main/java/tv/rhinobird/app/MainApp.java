package tv.rhinobird.app;

import android.app.Application;

/**
 * Created by emilio on 5/28/15.
 */
public class MainApp extends Application {
    private static GCM_Lib gcm;

    @Override
    public void onCreate() {
        gcm = new GCM_Lib(this);
        gcm.register();
    }

    public static String getRegistrationId() {
        return gcm.getRegistrationId();
    }
}
