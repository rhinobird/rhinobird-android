Chromium WebView Samples
===========================

This is a repository with useful examples for developing apps using the Chromium WebView.

If you spot any issues or questions please feel free to file an issue or reach out to [@gauntface](http://www.twitter.com/gauntface).

## WebRTC

In the Developer Preview of L the WebView will support WebRTC.

The methods this example relies may change as this is only a preview. At the
moment the example is using the new permission request API in WebChromeClient:

    mWebRTCWebView.setWebChromeClient(new WebChromeClient() {
        @Override
        public void onPermissionRequest(final PermissionRequest request) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    request.grant(request.getResources());
                }
            });
        }
    });

In the final version of this example should change with the launch of L to use the
preauthorizePermission method (At the moment this method is not working).

<p align="center">
<img src="http://i.imgur.com/AUYL7dK.png" alt="WebRTC on the Chrome WebView Example" />
</p>
