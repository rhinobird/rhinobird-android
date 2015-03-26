package tv.rhinobird.app;

import android.annotation.TargetApi;
import android.app.Activity;

import android.app.ActionBar;
import android.app.ActivityManager;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.widget.DrawerLayout;
import android.webkit.CookieManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.PermissionRequest;
import android.webkit.WebViewClient;
import android.widget.RelativeLayout;

import org.xwalk.core.XWalkPreferences;
import org.xwalk.core.XWalkView;
import org.xwalk.core.internal.XWalkLaunchScreenManager;

import tv.rhinobird.app.R;


public class MainActivity extends Activity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks {

    private static final String TAG = MainActivity.class.getSimpleName();

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getActionBar().hide();

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            TypedValue typedValue = new TypedValue();
            Resources.Theme theme = getTheme();
            theme.resolveAttribute(R.attr.colorPrimary, typedValue, true);
            int color = typedValue.data;

            Bitmap bm = BitmapFactory.decodeResource(getResources(), R.mipmap.icon_rhino);
            ActivityManager.TaskDescription td = new ActivityManager.TaskDescription(null, bm, color);

            setTaskDescription(td);
            bm.recycle();

        }
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.container, PlaceholderFragment.newInstance(position + 1))
                .commit();
    }

    public void onSectionAttached(int number) {

    }

    public void restoreActionBar() {
        ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.main, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        //private WebView mWebRTCWebView;
        private XWalkView xWalkWebView;
        private XWalkView xWalkWebView1;
        private XWalkLaunchScreenManager xWalkLaunchScreenManager;

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            final String wrapUrl = "https://beta.rhinobird.tv/";

            xWalkWebView1 = (XWalkView) rootView.findViewById(R.id.fragment_loader_webview);
            xWalkWebView1.load("file:///android_asset/index.html", null);

            xWalkWebView = (XWalkView) rootView.findViewById(R.id.fragment_main_webview);
            xWalkWebView.clearCache(true);
            xWalkWebView.setVisibility(View.GONE);

            if (DetectConnection.checkInternetConnection(getActivity ())) {
                xWalkWebView.load(wrapUrl, null);
            }
            else {
              xWalkWebView.load("file:///android_asset/error_page.html", null);
            }
            //XWalkPreferences.setValue(XWalkPreferences.REMOTE_DEBUGGING, true);

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    // TODO Auto-generated method stub
                    //hide loading image
                    xWalkWebView.setVisibility(View.VISIBLE);
                    xWalkWebView1.setVisibility(View.GONE);
                    Log.d(TAG, "webview visible");
                }
            }, 10000);
            return rootView;
        }
        @Override
        public void onPause() {
            super.onPause();
                if (xWalkWebView != null) {
                    xWalkWebView.pauseTimers();
                    xWalkWebView.onHide();
                }

        }

        @Override
        public void onStop() {
            super.onStop();
            xWalkWebView.evaluateJavascript("if(window.localStream){window.localStream.stop();}", null);
            xWalkWebView.stopLoading();
            /**
             * When the application falls into the background we want to stop the media stream
             * such that the camera is free to use by other apps.
             */
/*            if(Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {
                mWebRTCWebView.evaluateJavascript("if(window.localStream){window.localStream.stop();}", null);
            }*/
        }

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            ((MainActivity) activity).onSectionAttached(
                    getArguments().getInt(ARG_SECTION_NUMBER));
        }

        /**
         * Convenience method to set some generic defaults for a
         * given WebView
         *
         * @param webView
         */
    /*    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        private void setUpWebViewDefaults(WebView webView) {
            WebSettings settings = webView.getSettings();

            // Enable Javascript
            settings.setJavaScriptEnabled(true);

            // Use WideViewport and Zoom out if there is no viewport defined
            settings.setUseWideViewPort(true);
            settings.setLoadWithOverviewMode(true);

            // Enable pinch to zoom without the zoom buttons
            settings.setBuiltInZoomControls(true);

            // Allow use of Local Storage
            settings.setDomStorageEnabled(true);

            if(Build.VERSION.SDK_INT > Build.VERSION_CODES.HONEYCOMB) {
                // Hide the zoom controls for HONEYCOMB+
                settings.setDisplayZoomControls(false);
            }

            // Enable remote debugging via chrome://inspect
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                WebView.setWebContentsDebuggingEnabled(true);
            }

            webView.setWebViewClient(new WebViewClient());

            // AppRTC requires third party cookies to work
            CookieManager cookieManager = CookieManager.getInstance();
            cookieManager.setAcceptThirdPartyCookies(mWebRTCWebView, true);
        }*/
    }

}
