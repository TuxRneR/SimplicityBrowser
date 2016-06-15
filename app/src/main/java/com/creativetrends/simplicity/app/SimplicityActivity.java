package com.creativetrends.simplicity.app;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DownloadManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDialogFragment;
import android.support.v7.widget.Toolbar;
import android.util.Patterns;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.webkit.CookieManager;
import android.webkit.DownloadListener;
import android.webkit.WebChromeClient;
import android.webkit.WebStorage;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

// We need to use AppCompatActivity from android.support.v7.app.AppCompatActivity to have access to the SupportActionBar until the minimum API is >= 21.
public class SimplicityActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, Shortcut.CreateHomeScreenSchortcutListener {
    // favoriteIcon is public static so it can be accessed from Shortcut.
    public static Bitmap favoriteIcon;
    // mainWebView is public static so it can be accessed from AboutDialog and SettingsFragment.  It is also used in onCreate(), onOptionsItemSelected(), onNavigationItemSelected(), and loadUrlFromTextBox().
    public static WebView mainWebView;

    // mainMenu is public static so it can be accessed from SettingsFragment.  It is also used in onCreateOptionsMenu() and onOptionsItemSelected().
    public static Menu mainMenu;
    // cookieManager is public static so it can be accessed from SettingsFragment.  It is also used in onCreate(), onOptionsItemSelected(), and onNavigationItemSelected().
    public static CookieManager cookieManager;
    // javaScriptEnabled is public static so it can be accessed from SettingsFragment.  It is also used in onCreate(), onCreateOptionsMenu(), onOptionsItemSelected(), and loadUrlFromTextBox().
    public static boolean javaScriptEnabled;
    // firstPartyCookiesEnabled is public static so it can be accessed from SettingsFragment.  It is also used in onCreate(), onCreateOptionsMenu(), onPrepareOptionsMenu(), and onOptionsItemSelected().
    public static boolean firstPartyCookiesEnabled;
    // thirdPartyCookiesEnabled is uesd in onCreate(), onCreateOptionsMenu(), onPrepareOptionsMenu(), and onOptionsItemSelected().
    public static boolean thirdPartyCookiesEnabled;
    // domStorageEnabled is public static so it can be accessed from SettingsFragment.  It is also used in onCreate(), onCreateOptionsMenu(), and onOptionsItemSelected().
    public static boolean domStorageEnabled;
    // homepage is public static so it can be accessed from  SettingsFragment.  It is also used in onCreate() and onOptionsItemSelected().
    public static String homepage;

    // drawerToggle is used in onCreate(), onPostCreate(), onConfigurationChanged(), onNewIntent(), and onNavigationItemSelected().
    private ActionBarDrawerToggle drawerToggle;
    // drawerLayout is used in onCreate(), onNewIntent(), and onBackPressed().
    private DrawerLayout drawerLayout;
    // formattedUrlString is used in onCreate(), onOptionsItemSelected(), onCreateHomeScreenShortcutCreate(), and loadUrlFromTextBox().
    private String formattedUrlString;

    // urlTextBox is used in onCreate(), onOptionsItemSelected(), and loadUrlFromTextBox().
    private EditText urlTextBox;

    @Override
    // Remove Android Studio's warning about the dangers of using SetJavaScriptEnabled.  The whole premise of Privacy Browser is built around an understanding of these dangers.
    @SuppressLint("SetJavaScriptEnabled")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.coordinator_layout);

        // We need to use the SupportActionBar from android.support.v7.app.ActionBar until the minimum API is >= 21.
        Toolbar supportAppBar = (Toolbar) findViewById(R.id.appBar);
        setSupportActionBar(supportAppBar);

        final FrameLayout fullScreenVideoFrameLayout = (FrameLayout) findViewById(R.id.fullScreenVideoFrameLayout);

        // We need to use the SupportActionBar from android.support.v7.app.ActionBar until the minimum API is >= 21.
        final ActionBar appBar = getSupportActionBar();

        // Setup AdView for the free flavor.


        // Implement swipe to refresh
        final SwipeRefreshLayout swipeToRefresh = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);
        assert swipeToRefresh != null;
        swipeToRefresh.setColorSchemeResources(R.color.white);
        swipeToRefresh.setProgressBackgroundColorSchemeResource(R.color.md_blue_500);
        swipeToRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mainWebView.reload();
            }
        });

        mainWebView = (WebView) findViewById(R.id.mainWebView);

        if (appBar != null) {

            appBar.setCustomView(R.layout.url_bar);
            appBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);


            urlTextBox = (EditText) appBar.getCustomView().findViewById(R.id.urlTextBox);
            urlTextBox.setOnKeyListener(new View.OnKeyListener() {
                public boolean onKey(View v, int keyCode, KeyEvent event) {

                    if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {

                        try {
                            loadUrlFromTextBox();
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }

                        return true;
                    } else {

                        return false;
                    }
                }
            });
        }


        drawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);

        drawerLayout.setDrawerTitle(GravityCompat.START, getString(R.string.navigation_drawer));


        final NavigationView navigationView = (NavigationView) findViewById(R.id.navigationView);
        assert navigationView != null;
        navigationView.setNavigationItemSelectedListener(this);


        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, supportAppBar, R.string.open_navigation, R.string.close_navigation);

        mainWebView.setWebViewClient(new WebViewClient() {

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                mainWebView.loadUrl(url);
                return true;
            }


            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                urlTextBox.setText(url);
                if (appBar != null) {
                if ((url.contains("https://"))) {
                ImageView secure = (ImageView) appBar.getCustomView().findViewById(R.id.favoriteIcon);
                    secure.setVisibility(View.VISIBLE);
                    } else {
                        ImageView secure = (ImageView) appBar.getCustomView().findViewById(R.id.favoriteIcon);
                        secure.setVisibility(View.GONE);
                    }
                }
            }


            @Override
            public void onPageFinished(WebView view, String url) {
                formattedUrlString = url;

                // Only update urlTextBox if the user is not typing in it.
                if (!urlTextBox.hasFocus()) {
                    urlTextBox.setText(formattedUrlString);
                }
            }
        });

        mainWebView.setWebChromeClient(new WebChromeClient() {
            // Update the progress bar when a page is loading.
            @Override
            public void onProgressChanged(WebView view, int progress) {
                // Make sure that appBar is not null.
                if (appBar != null) {
                    ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar);
                    assert progressBar != null;
                    progressBar.setProgress(progress);
                    if (progress < 100) {
                        progressBar.setVisibility(View.VISIBLE);
                    } else {
                        progressBar.setVisibility(View.GONE);

                        //Stop the SwipeToRefresh indicator if it is running
                        swipeToRefresh.setRefreshing(false);
                    }
                }
            }

            @Override
            public void
            onReceivedTitle(WebView view, String title) {
                super.onReceivedTitle(view, title);
                TextView title_text = (TextView) findViewById(R.id.profile_name);
                assert title_text != null;
                title_text.setText(title);
            }

            // Set the favorite icon when it changes.
            @Override
            public void onReceivedIcon(WebView view, Bitmap icon) {
                // Save a copy of the favorite icon for use if a shortcut is added to the home screen.
                favoriteIcon = icon;
                ImageView site_fav = (ImageView) findViewById(R.id.profile_pic);
                assert site_fav != null;
                site_fav.setImageBitmap(Bitmap.createScaledBitmap(icon, 64, 64, true));

            }

            // Enter full screen video
            @Override
            public void onShowCustomView(View view, CustomViewCallback callback) {
                if (appBar != null) {
                    appBar.hide();
                }

                // Show the fullScreenVideoFrameLayout.
                fullScreenVideoFrameLayout.addView(view);
                fullScreenVideoFrameLayout.setVisibility(View.VISIBLE);

                // Hide the mainWebView.
                mainWebView.setVisibility(View.GONE);

                // Hide the ad if this is the free flavor.


                /* SYSTEM_UI_FLAG_HIDE_NAVIGATION hides the navigation bars on the bottom or right of the screen.
                ** SYSTEM_UI_FLAG_FULLSCREEN hides the status bar across the top of the screen.
                ** SYSTEM_UI_FLAG_IMMERSIVE_STICKY makes the navigation and status bars ghosted overlays and automatically rehides them.
                */

                // Set the one flag supported by API >= 14.
                view.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);

                // Set the two flags that are supported by API >= 16.
                if (Build.VERSION.SDK_INT >= 16) {
                    view.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN);
                }

                // Set all three flags that are supported by API >= 19.
                if (Build.VERSION.SDK_INT >= 19) {
                    view.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
                }
            }

            // Exit full screen video
            public void onHideCustomView() {
                if (appBar != null) {
                    appBar.show();
                }

                // Show the mainWebView.
                mainWebView.setVisibility(View.VISIBLE);

                // Show the ad if this is the free flavor.

                // Hide the fullScreenVideoFrameLayout.
                fullScreenVideoFrameLayout.removeAllViews();
                fullScreenVideoFrameLayout.setVisibility(View.GONE);
            }
        });

        // Allow the downloading of files.
        mainWebView.setDownloadListener(new DownloadListener() {
            // Launch the Android download manager when a link leads to a download.
            @Override
            public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
                DownloadManager downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
                DownloadManager.Request requestUri = new DownloadManager.Request(Uri.parse(url));

                // Add the URL as the description for the download.
                requestUri.setDescription(url);

                // Show the download notification after the download is completed.
                requestUri.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);

                // Initiate the download and display a Snackbar.
                downloadManager.enqueue(requestUri);
                Snackbar.make(findViewById(R.id.mainWebView), R.string.download_started, Snackbar.LENGTH_SHORT).show();
            }
        });

        // Allow pinch to zoom.
        mainWebView.getSettings().setBuiltInZoomControls(true);

        // Hide zoom controls.
        mainWebView.getSettings().setDisplayZoomControls(false);

        // Initialize the default preference values the first time the program is run.
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        // Get the shared preference values.
        SharedPreferences savedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        // Set JavaScript initial status.
        javaScriptEnabled = savedPreferences.getBoolean("javascript_enabled", true);
        mainWebView.getSettings().setJavaScriptEnabled(javaScriptEnabled);

        // Initialize cookieManager.
        cookieManager = CookieManager.getInstance();

        // Set cookies initial status.
        firstPartyCookiesEnabled = savedPreferences.getBoolean("first_party_cookies_enabled", true);
        cookieManager.setAcceptCookie(firstPartyCookiesEnabled);

        // Set third-party cookies initial status if API >= 21.
        if (Build.VERSION.SDK_INT >= 21) {
            thirdPartyCookiesEnabled = savedPreferences.getBoolean("third_party_cookies_enabled", true);
            cookieManager.setAcceptThirdPartyCookies(mainWebView, thirdPartyCookiesEnabled);
        }

        // Set DOM storage initial status.
        domStorageEnabled = savedPreferences.getBoolean("dom_storage_enabled", true);
        mainWebView.getSettings().setDomStorageEnabled(domStorageEnabled);

        // Set homepage initial status.
        homepage = savedPreferences.getString("homepage", "https://www.google.com");

        // Get the intent information that started the app.
        final Intent intent = getIntent();

        if (intent.getData() != null) {
            // Get the intent data and convert it to a string.
            final Uri intentUriData = intent.getData();
            formattedUrlString = intentUriData.toString();
        }

        // If formattedUrlString is null assign the homepage to it.
        if (formattedUrlString == null) {
            formattedUrlString = homepage;
        }

        // Load the initial website.
        mainWebView.loadUrl(formattedUrlString);

        // Load the ad if this is the free flavor.

    }

    @Override
    protected void onNewIntent(Intent intent) {
        // Sets the new intent as the activity intent, so that any future getIntent()s pick up this one instead of creating a new activity.
        setIntent(intent);

        if (intent.getData() != null) {
            // Get the intent data and convert it to a string.
            final Uri intentUriData = intent.getData();
            formattedUrlString = intentUriData.toString();
        }

        // Close the navigation drawer if it is open.
        if (drawerLayout.isDrawerVisible(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        }

        // Load the website.
        mainWebView.loadUrl(formattedUrlString);

        // Clear the keyboard if displayed and remove the focus on the urlTextBar if it has it.
        mainWebView.requestFocus();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_options, menu);

        // Set mainMenu so it can be used by onOptionsItemSelected.
        mainMenu = menu;

        // Get MenuItems for checkable menu items.

        MenuItem toggleDomStorage = menu.findItem(R.id.toggleDomStorage);


        toggleDomStorage.setChecked(domStorageEnabled);
        /* toggleSaveFormData does nothing until database storage is implemented.
        toggleSaveFormData.setChecked(saveFormDataEnabled);
        */

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {


        MenuItem clearCookies = menu.findItem(R.id.clearCookies);
        clearCookies.setEnabled(cookieManager.hasCookies());

        // Run all the other default commands.
        super.onPrepareOptionsMenu(menu);

        // return true displays the menu.
        return true;
    }

    @Override
    // Remove Android Studio's warning about the dangers of using SetJavaScriptEnabled.
    @SuppressLint("SetJavaScriptEnabled")
    // removeAllCookies is deprecated, but it is required for API < 21.
    @SuppressWarnings("deprecation")
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        int menuItemId = menuItem.getItemId();

        // Some options need to update the drawable for toggleJavaScript.


        // Set the commands that relate to the menu entries.
        switch (menuItemId) {
            case R.id.simplicityHome:
                mainWebView.loadUrl(homepage);
                return true;

            case R.id.simplicityBack:
                if (mainWebView.canGoBack()) {
                    mainWebView.goBack();
                }
                return true;

            case R.id.simplicityForward:
                if (mainWebView.canGoForward()) {
                    mainWebView.goForward();
                }
                return true;

            case R.id.clearCookies:
                if (Build.VERSION.SDK_INT < 21) {
                    cookieManager.removeAllCookie();
                } else {
                    cookieManager.removeAllCookies(null);
                }
                Snackbar.make(findViewById(R.id.mainWebView), R.string.cookies_deleted, Snackbar.LENGTH_SHORT).show();
                return true;

            case R.id.clearDomStorage:
                WebStorage webStorage = WebStorage.getInstance();
                webStorage.deleteAllData();
                Snackbar.make(findViewById(R.id.mainWebView), R.string.dom_storage_deleted, Snackbar.LENGTH_SHORT).show();
                return true;

            case R.id.share:
                Intent shareIntent = new Intent();
                shareIntent.setAction(Intent.ACTION_SEND);
                shareIntent.putExtra(Intent.EXTRA_TEXT, urlTextBox.getText().toString());
                shareIntent.setType("text/plain");
                startActivity(Intent.createChooser(shareIntent, "Share URL"));
                return true;

            case R.id.addToHomescreen:
                // Show the Shortcut AlertDialog and name this instance createShortcut.
                AppCompatDialogFragment shortcutDialog = new Shortcut();
                shortcutDialog.show(getSupportFragmentManager(), "createShortcut");

                //Everything else will be handled by Shortcut and the associated listeners below.
                return true;

            default:
                return super.onOptionsItemSelected(menuItem);
        }
    }

    @Override
    // removeAllCookies is deprecated, but it is required for API < 21.
    @SuppressWarnings("deprecation")
    public boolean onNavigationItemSelected(MenuItem menuItem) {
        int menuItemId = menuItem.getItemId();

        switch (menuItemId) {
            case R.id.home:
                mainWebView.loadUrl(homepage);
                break;

            case R.id.back:
                if (mainWebView.canGoBack()) {
                    mainWebView.goBack();
                }
                break;

            case R.id.forward:
                if (mainWebView.canGoForward()) {
                    mainWebView.goForward();
                }
                break;

            case R.id.downloads:
                // Launch the system Download Manager.
                Intent downloadManagerIntent = new Intent(DownloadManager.ACTION_VIEW_DOWNLOADS);

                // Launch as a new task so that Download Manager and Privacy Browser show as separate windows in the recent tasks list.
                downloadManagerIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                startActivity(downloadManagerIntent);
                break;

            case R.id.settings:
                // Launch PreferenceFragment.
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                break;

            case R.id.about:
                // Show the AboutDialog AlertDialog and name this instance aboutDialog.
                AppCompatDialogFragment aboutDialog = new AboutDialog();
                aboutDialog.show(getSupportFragmentManager(), "aboutDialog");
                break;

            case R.id.clearAndExit:
                // Clear DOM storage.
                WebStorage domStorage = WebStorage.getInstance();
                domStorage.deleteAllData();

                // Clear cookies.  The commands changed slightly in API 21.
                if (Build.VERSION.SDK_INT >= 21) {
                    cookieManager.removeAllCookies(null);
                } else {
                    cookieManager.removeAllCookie();
                }

                // Destroy the internal state of the webview.
                mainWebView.destroy();

                // Close Privacy Browser.  finishAndRemoveTask also removes Privacy Browser from the recent app list.
                if (Build.VERSION.SDK_INT >= 21) {
                    finishAndRemoveTask();
                } else {
                    finish();
                }
                break;

            default:
                break;
        }

        // Close the navigation drawer.
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Sync the state of the DrawerToggle after onRestoreInstanceState has finished.
        drawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        // Update the status of the drawerToggle icon.
        drawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public void onCreateHomeScreenShortcutCancel(DialogFragment dialog) {
        // Do nothing because the user selected "Cancel".
    }

    @Override
    public void onCreateHomeScreenShortcutCreate(DialogFragment dialog) {
        // Get shortcutNameEditText from the alert dialog.
        EditText shortcutNameEditText = (EditText) dialog.getDialog().findViewById(R.id.shortcutNameEditText);

        // Create the bookmark shortcut based on formattedUrlString.
        Intent bookmarkShortcut = new Intent();
        bookmarkShortcut.setAction(Intent.ACTION_VIEW);
        bookmarkShortcut.setData(Uri.parse(formattedUrlString));

        // Place the bookmark shortcut on the home screen.
        Intent placeBookmarkShortcut = new Intent();
        placeBookmarkShortcut.putExtra("android.intent.extra.shortcut.INTENT", bookmarkShortcut);
        placeBookmarkShortcut.putExtra("android.intent.extra.shortcut.NAME", shortcutNameEditText.getText().toString());
        placeBookmarkShortcut.putExtra("android.intent.extra.shortcut.ICON", favoriteIcon);
        placeBookmarkShortcut.setAction("com.android.launcher.action.INSTALL_SHORTCUT");
        sendBroadcast(placeBookmarkShortcut);
    }

    // Override onBackPressed to handle the navigation drawer and mainWebView.
    @Override
    public void onBackPressed() {
        final WebView mainWebView = (WebView) findViewById(R.id.mainWebView);

        // Close the navigation drawer if it is available.  GravityCompat.START is the drawer on the left on Left-to-Right layout text.
        if (drawerLayout.isDrawerVisible(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            // Load the previous URL if available.
            if (mainWebView.canGoBack()) {
                mainWebView.goBack();
            } else {
                // Pass onBackPressed to the system.
                super.onBackPressed();
            }
        }
    }

    private void loadUrlFromTextBox() throws UnsupportedEncodingException {
        // Get the text from urlTextBox and convert it to a string.
        String unformattedUrlString = urlTextBox.getText().toString();
        URL unformattedUrl = null;
        Uri.Builder formattedUri = new Uri.Builder();

        // Check to see if unformattedUrlString is a valid URL.  Otherwise, convert it into a Duck Duck Go search.
        if (Patterns.WEB_URL.matcher(unformattedUrlString).matches()) {
            // Add http:// at the beginning if it is missing.  Otherwise the app will segfault.
            if (!unformattedUrlString.startsWith("http")) {
                unformattedUrlString = "http://" + unformattedUrlString;
            }

            // Convert unformattedUrlString to a URL, then to a URI, and then back to a string, which sanitizes the input and adds in any missing components.
            try {
                unformattedUrl = new URL(unformattedUrlString);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }

            // The ternary operator (? :) makes sure that a null pointer exception is not thrown, which would happen if .get was called on a null value.
            final String scheme = unformattedUrl != null ? unformattedUrl.getProtocol() : null;
            final String authority = unformattedUrl != null ? unformattedUrl.getAuthority() : null;
            final String path = unformattedUrl != null ? unformattedUrl.getPath() : null;
            final String query = unformattedUrl != null ? unformattedUrl.getQuery() : null;
            final String fragment = unformattedUrl != null ? unformattedUrl.getRef() : null;

            formattedUri.scheme(scheme).authority(authority).path(path).query(query).fragment(fragment);
            formattedUrlString = formattedUri.build().toString();
        } else {
            // Sanitize the search input and convert it to a DuckDuckGo search.
            final String encodedUrlString = URLEncoder.encode(unformattedUrlString, "UTF-8");

            // Use the correct search URL based on javaScriptEnabled.
            if (javaScriptEnabled) {
                formattedUrlString = "https://duckduckgo.com/?q=" + encodedUrlString;
            } else {
                formattedUrlString = "https://duckduckgo.com/html/?q=" + encodedUrlString;
            }
        }

        mainWebView.loadUrl(formattedUrlString);

        // Hides the keyboard so we can see the webpage.
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(mainWebView.getWindowToken(), 0);
    }
}
