package cs408team3.wikidroid;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.SearchView.OnQueryTextListener;
import android.widget.Toast;
import cs408team3.wikidroid.blur.Blur;
import cs408team3.wikidroid.blur.BlurTask;
import cs408team3.wikidroid.search.SearchArticle;

public class MainActivity extends Activity {

    private static final String   TAG                     = "MainActivity";

    private static final int      ACTIONBAR_NORMAL_TITLE  = 0x1;
    private static final int      ACTIONBAR_DRAWER_TITLE  = 0x2;

    private static final String   STATE_FIRST_PAGE_LOADED = "mFirstPageLoaded";

    // TODO: remove
    private List<String>          mListTitles             = new ArrayList<String>();

    private DrawerLayout          mDrawerLayout;

    // TODO: replace
    private ArrayAdapter<String>  mDrawerListAdapter;
    private ListView              mDrawerList;
    private ImageView             mBlurImage;
    private FrameLayout           mContentFrame;
    private WebView               mWebPage;
    private MenuItem              mSearchMenuItem;
    private ProgressBar           mWebProgressBar;

    private ActionBarDrawerToggle mDrawerToggle;

    private CharSequence          mDrawerTitle;
    private CharSequence          mTitle;

    // Indicator for that web page has already been loaded at least once
    private boolean               mFirstPageLoaded        = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_PROGRESS);
        setContentView(R.layout.activity_main);

        mTitle = mDrawerTitle = getTitle();
        mWebPage = (WebView) findViewById(R.id.content_view);
        mWebProgressBar = (ProgressBar) findViewById(R.id.content_progress);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);
        mBlurImage = (ImageView) findViewById(R.id.blur_image);
        mContentFrame = (FrameLayout) findViewById(R.id.content_frame);

        mDrawerToggle = new WikiDroidActionBarDrawerToggle(this, mDrawerLayout, R.drawable.ic_drawer, R.string.drawer_open, R.string.drawer_close);

        // Set the drawer toggle as the DrawerListener
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        // Setup mWebPage
        mWebPage.getSettings().setBuiltInZoomControls(true);
        // take off the zoom button, just use fingers to zoom in/out
        mWebPage.getSettings().setDisplayZoomControls(false);
        // this part is to handle new links on the same view.
        mWebPage.setWebViewClient(new WebViewClient() {

            @Override
            public void onPageFinished(WebView view, String url) {
                Log.i(TAG, "Page " + url + " loaded");

                // Mark that we have already loaded at least one web page
                if (!mFirstPageLoaded) {
                    mFirstPageLoaded = !mFirstPageLoaded;
                }

                setTitle(Utils.trimWikipediaTitle(view.getTitle()), ACTIONBAR_NORMAL_TITLE);
            }
        });
        mWebPage.setWebChromeClient(new WebChromeClient() {

            @Override
            public void onProgressChanged(WebView view, int progress) {
                Log.v(TAG, "Page load progress " + progress);

                if (!mFirstPageLoaded) {
                    // No web page loaded before
                    // Using Progress Bar as an loading indicator
                    if (progress < 100) {
                        startWebProgressBar();
                    }

                    if (progress == 100) {
                        stopWebProgressBar();
                    }
                } else {
                    // Web page has been loaded before
                    // Using Window.FEATURE_PROGRESS as an loading indicator
                    // TODO: the indicator needs to be improved
                    setProgress(progress * 100);
                }
            }
        });

        // TODO: replace
        // Set the adapter for the list view
        mDrawerListAdapter = new ArrayAdapter<String>(this, R.layout.drawer_list_item, R.id.drawer_list_item_text, mListTitles);
        mDrawerList.setAdapter(mDrawerListAdapter);

        // Set the list's click listener
        // mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

        // Disable Drawer Scrim Color
        mDrawerLayout.setScrimColor(Color.TRANSPARENT);

        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putBoolean(STATE_FIRST_PAGE_LOADED, mFirstPageLoaded);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        mFirstPageLoaded = savedInstanceState.getBoolean(STATE_FIRST_PAGE_LOADED, false);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // Check if the key event was the Back button and if there's history
        if ((keyCode == KeyEvent.KEYCODE_BACK) && mWebPage.canGoBack()) {
            mWebPage.goBack();
            return true;
        }
        // If it wasn't the Back key or there's no web page history, bubble up
        // to the default
        // system behavior (probably exit the activity)
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    // Called whenever we call invalidateOptionsMenu()
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // If the nav drawer is open, show / hide action items related to the
        // content view
        boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);

        // Show Add tab
        menu.findItem(R.id.action_add_tab).setVisible(drawerOpen);
        // Hide search
        menu.findItem(R.id.search).setVisible(!drawerOpen);

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        // Associate searchable configuration with the SearchView
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        mSearchMenuItem = menu.findItem(R.id.search);

        SearchView searchView = (SearchView) mSearchMenuItem.getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setOnQueryTextListener(new OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {
                // Toast.makeText(getApplicationContext(), "Teste",
                // Toast.LENGTH_LONG).show();
                boolean haveNet = Utils.isNetworkAvailable(getApplicationContext());
                if (haveNet == false) {
                    Toast t = Toast.makeText(getApplicationContext(), "Sorry, No internet connection", Toast.LENGTH_SHORT);
                    t.setGravity(Gravity.CENTER, 5, 5);
                    t.show();
                    return false;
                }
                else {
                    if (!Utils.verifySearchString(query, TAG)) {
                        Toast t = Toast.makeText(getApplicationContext(), "Sorry, invalid input. Try again", Toast.LENGTH_SHORT);
                        t.setGravity(Gravity.CENTER, 5, 5);
                        t.show();
                        return false;
                    }

                    if (mSearchMenuItem != null) {
                        mSearchMenuItem.collapseActionView();
                    }

                    SearchArticle search = new SearchArticle(getApplicationContext(), mWebPage);
                    search.execute(query);

                    return true;
                }
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Pass the event to ActionBarDrawerToggle, if it returns
        // true, then it has handled the app icon touch event
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        // Handle your other action bar items...
        switch (item.getItemId()) {
        case R.id.action_add_tab:
            // TODO: remove
            mListTitles.add("New Tab");
            mDrawerListAdapter.notifyDataSetChanged();

            return true;
        case R.id.languages:
            // TODO: ADD LANGUAGE CODE HERE

            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Test if there is available network to search on internet
     * @return
     */
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    /**
     * Method to test if the string is valid or not.
     * Test if its null, blank " " or ""
     * @param term
     * @return
     */
    private boolean verifyString(String term){

        String aux = term.replaceAll("[^\\w]", "");

        if(aux == null){
            System.err.println("term is null");
            return false;
        }
        if(aux.equals("")){
            System.err.println("term is empty");
            return false;
        }

        if(aux.equals("")){
            System.err.println("term is just blanket spaces");
            return false;
        }
        if(term.equals("@")){
            System.err.println("term is just @");
            return false;
        }
        if(term.equals("&")){
            System.err.println("term is just &");
            return false;
        }
        if(term.equals("\"\"")){
            System.err.println("term is just \"\"");
            return false;
        }
        return true;
    }

    private class WikiDroidActionBarDrawerToggle extends ActionBarDrawerToggle implements BlurTask.Listener {

        private Bitmap scaled;

        // private BlurTask blurTask;

        public WikiDroidActionBarDrawerToggle(Activity activity, DrawerLayout drawerLayout, int drawerImageRes, int openDrawerContentDescRes, int closeDrawerContentDescRes) {
            super(activity, drawerLayout, drawerImageRes, openDrawerContentDescRes, closeDrawerContentDescRes);
        }

        @Override
        public void onDrawerSlide(View drawerView, float slideOffset) {
            super.onDrawerSlide(drawerView, slideOffset);
            if (slideOffset > 0.0f) {
                setBlurAlpha(slideOffset);
            } else {
                clearBlurImage();
            }
        }

        // Called when a drawer has settled in a completely closed state.
        @Override
        public void onDrawerClosed(View view) {
            super.onDrawerClosed(view);
            toggleTitle(ACTIONBAR_NORMAL_TITLE);
            clearBlurImage(); // Clear background blur
            invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
        }

        // Called when a drawer has settled in a completely open state.
        @Override
        public void onDrawerOpened(View drawerView) {
            super.onDrawerOpened(drawerView);
            toggleTitle(ACTIONBAR_DRAWER_TITLE);
            invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
        }

        private void setBlurAlpha(float slideOffset) {
            if (mBlurImage.getVisibility() != View.VISIBLE) {
                setBlurImage();
            }

            mBlurImage.setAlpha(slideOffset);
        }

        private void setBlurImage() {
            mBlurImage.setImageBitmap(null);
            mBlurImage.setVisibility(View.VISIBLE);

            scaled = Utils.drawViewToBitmap(scaled, mContentFrame, mContentFrame.getWidth(), mContentFrame.getHeight(), Blur.DEFAULT_DOWNSAMPLING);
            // blurTask = new BlurTask(mContentFrame.getContext(), null,
            // scaled);
            new BlurTask(mContentFrame.getContext(), null, scaled);

            mBlurImage.setImageBitmap(scaled);
            Log.v(TAG, "BlurImage set");
        }

        private void clearBlurImage() {
            mBlurImage.setVisibility(View.GONE);
            mBlurImage.setImageBitmap(null);
            // blurTask = null;
        }

        @Override
        public void onBlurOperationFinished() {
            mBlurImage.invalidate();
        }

    }

    private void setTitle(String title, int status) {
        switch (status) {
        case ACTIONBAR_NORMAL_TITLE:
            if (title != null)
                mTitle = title;
            getActionBar().setTitle(mTitle);
            return;
        case ACTIONBAR_DRAWER_TITLE:
            if (title != null)
                mDrawerTitle = title;
            getActionBar().setTitle(mDrawerTitle);
            return;
        }
    }

    private void toggleTitle(int status) {
        setTitle(null, status);
    }

    private void startWebProgressBar() {
        if (mWebProgressBar.getVisibility() != View.VISIBLE) {
            mWebProgressBar.setVisibility(View.VISIBLE);
        }
    }

    private void stopWebProgressBar() {
        if (mWebProgressBar.getVisibility() != View.GONE) {
            mWebProgressBar.setVisibility(View.GONE);
        }
    }
}
