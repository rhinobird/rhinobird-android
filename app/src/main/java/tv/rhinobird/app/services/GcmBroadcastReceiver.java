package tv.rhinobird.app.services;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import tv.rhinobird.app.MainActivity;
import tv.rhinobird.app.R;

/**
 * Created by emilio on 5/28/15.
 */
public class GcmBroadcastReceiver extends BroadcastReceiver {
    protected static final String TAG = GcmBroadcastReceiver.class.getSimpleName();

    private Context mContext;
    private Bundle extras;

    @Override
    public void onReceive(Context context, Intent intent) {
        mContext = context;
        extras = intent.getExtras();

        if (extras.getString("notificationType").equals("new_live_stream")) {
            showStreamNotification();
        }

    }

    private void showStreamNotification() {
        String caption = extras.getString("caption");
        String url = extras.getString("url");
        String username = extras.getString("username");

        PendingIntent resultPendingIntent = getPendingIntent(url);
        // Vibration options
        long[] pattern = new long[]{500, 500};

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(mContext)
                        .setContentIntent(resultPendingIntent)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle(username + " is going live!")
                        .setVibrate(pattern)
                        .setContentText(caption);

        NotificationManager mNotificationManager =
                (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(0, mBuilder.build());
    }

    private PendingIntent getPendingIntent(String url) {
        Intent resultIntent = new Intent(mContext, MainActivity.class);
        resultIntent.putExtra("url", url);

        return PendingIntent.getActivity(
                mContext,
                0,
                resultIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
        );
    }
}
