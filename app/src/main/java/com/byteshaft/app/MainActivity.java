package com.byteshaft.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import java.net.InetAddress;

public class MainActivity extends Activity {

    private NsdManager mNsdManager;
    private NsdManager.DiscoveryListener mDiscoveryListener;
    private NsdManager.ResolveListener mResolveListener;
    private NsdServiceInfo mServiceInfo;
    private String mRPiAddress;
    private WebView mWebView;
    private boolean mIsDiscovering;
    private boolean mIsResolved;
    private ProgressBar progressBar;
    // The NSD service type that the RPi exposes.
    private final String SERVICE_TYPE = "_workstation._tcp.";
    private boolean mActivityShown;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mWebView = (WebView) findViewById(R.id.web_view);
        mWebView.setWebViewClient(new CustomWebViewClient());
        WebSettings webSettings = mWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        String url = getUrlFromIntent(getIntent());
        if (!isNetworkAvailable()) {
            showNoInternetDialog();
        } else if (url != null) {
            mWebView.loadUrl(url);
        } else {
            mNsdManager = (NsdManager) (getSystemService(NSD_SERVICE));
            initializeResolveListener();
            initializeDiscoveryListener();
            mNsdManager.discoverServices(SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, mDiscoveryListener);
            progressBar = (ProgressBar) findViewById(R.id.progressBar);
            progressBar.setVisibility(View.VISIBLE);
            new android.os.Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (!mIsResolved) {
                        mDiscoveryListener = null;
                        mResolveListener = null;
                        if (mActivityShown) {
                            networkNotFoundDialog();
                        } else {
                            finish();
                        }
                    }
                }
            }, 5000);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mActivityShown = true;
    }

    @Override
    public void onBackPressed() {
        if (mWebView.canGoBack()) {
            mWebView.goBack();
        } else {
            finish();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mIsDiscovering) {
            mNsdManager.stopServiceDiscovery(mDiscoveryListener);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        mActivityShown = false;
    }

    private void initializeDiscoveryListener() {

        // Instantiate a new DiscoveryListener
        mDiscoveryListener = new NsdManager.DiscoveryListener() {

            //  Called as soon as service discovery begins.
            @Override
            public void onDiscoveryStarted(String regType) {
            }

            @Override
            public void onServiceFound(NsdServiceInfo service) {
                // A service was found!  Do something with it.
                final String name = service.getServiceName();
                String type = service.getServiceType();
                if (type.equals(SERVICE_TYPE) && name.contains(getString(R.string.host_name))) {
                    Log.d("NSD", "Service Found @ '" + name + "'");
                    mNsdManager.resolveService(service, mResolveListener);
                }
            }

            @Override
            public void onServiceLost(NsdServiceInfo service) {
                // When the network service is no longer available.
                // Internal bookkeeping code goes here.
            }

            @Override
            public void onDiscoveryStopped(String serviceType) {
                mIsDiscovering = false;
            }

            @Override
            public void onStartDiscoveryFailed(String serviceType, int errorCode) {
                mNsdManager.stopServiceDiscovery(this);
            }

            @Override
            public void onStopDiscoveryFailed(String serviceType, int errorCode) {
                mNsdManager.stopServiceDiscovery(this);
            }
        };
    }

    private void initializeResolveListener() {
        mResolveListener = new NsdManager.ResolveListener() {

            @Override
            public void onResolveFailed(NsdServiceInfo serviceInfo, int errorCode) {
                // Called when the resolve fails.  Use the error code to debug.
                Log.e("NSD", "Resolve failed" + errorCode);
            }

            @Override
            public void onServiceResolved(NsdServiceInfo serviceInfo) {
                mIsResolved = true;
                mServiceInfo = serviceInfo;
                InetAddress host = mServiceInfo.getHost();
                final String address = host.getHostAddress();
                Log.d("NSD", "Resolved address = " + address);
                mRPiAddress = address;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mWebView.loadUrl("http://".concat(mRPiAddress));
                        progressBar.setVisibility(View.INVISIBLE);
                    }
                });
            }
        };
    }

    private void showNoInternetDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("No internet connection");
        builder.setMessage("Make sure internet is working before starting the app.");
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                finish();
            }
        });
        builder.setCancelable(false);
        builder.create();
        builder.show();
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    void networkNotFoundDialog() {
        new AlertDialog.Builder(this)
                .setTitle("No Network Found !")
                .setMessage("Make sure you are connected to a network.")
                .setCancelable(false)
                .setPositiveButton("close", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        MainActivity.this.finish();
                    }
                })
                .show();
    }

    private class CustomWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }
    }

    private String getUrlFromIntent(Intent intent) {
        String url = null;
        if (intent.getExtras() != null) {
            url = intent.getExtras().getString("url", null);
        }
        return url;
    }
}
