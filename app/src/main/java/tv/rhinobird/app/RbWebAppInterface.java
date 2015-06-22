package tv.rhinobird.app;

import android.content.Context;
import android.widget.Toast;

import org.xwalk.core.JavascriptInterface;

/**
 * Created by emilio on 5/28/15.
 */
public class RbWebAppInterface {
    Context mContext;

    /**
     * Instantiate the interface and set the context
     */
    RbWebAppInterface(Context c) {
        mContext = c;
    }

    @android.webkit.JavascriptInterface
    @JavascriptInterface
    public String getRegistrationId() {
        return MainApp.getRegistrationId();
    }

}
