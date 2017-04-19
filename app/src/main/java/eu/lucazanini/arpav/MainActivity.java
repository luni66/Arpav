package eu.lucazanini.arpav;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import java.util.Observable;
import java.util.Observer;

import butterknife.BindView;
import butterknife.ButterKnife;
import eu.lucazanini.arpav.model.Titles;
import timber.log.Timber;

// http://stackoverflow.com/questions/23133912/android-viewpager-update-off-screen-but-cached-fragments-in-viewpager

// https://guides.codepath.com/android/ViewPager-with-FragmentPagerAdapter

public class MainActivity extends AppCompatActivity implements TitlesCallBack {

    private static final int PAGES = 7;
    private static final int PAGES_LIMIT = 7;
    protected @BindView(R.id.pager) ViewPager pager;
    private CollectionPagerAdapter collectionPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        collectionPagerAdapter = new CollectionPagerAdapter(getSupportFragmentManager());

//        final ActionBar actionBar = getSupportActionBar();
//        actionBar.setDisplayHomeAsUpEnabled(true);

        pager.setOffscreenPageLimit(PAGES_LIMIT);
        pager.setAdapter(collectionPagerAdapter);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

//        TownList towns = TownList.getInstance(this);
//        towns.save();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Timber.d("onActivityResult requestCode "+requestCode);
//        super.onActivityResult(requestCode, resultCode, data);
        // Check which request we're responding to
        if (requestCode == MeteogrammaFragment.REQUEST_CODE) {
            Timber.d("onActivityResult REQUEST_CODE");
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                Timber.d("onActivityResult RESULT_OK");
                String town  = data.getStringExtra("TOWN_NAME");
                Timber.d("onActivityResult " + town);
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // This is called when the Home (Up) button is pressed in the action bar.
                // Create a simple intent that starts the hierarchical parent activity and
                // use NavUtils in the Support Package to ensure proper handling of Up.
                Intent upIntent = new Intent(this, MainActivity.class);
                if (NavUtils.shouldUpRecreateTask(this, upIntent)) {
                    // This activity is not part of the application's task, so create a new task
                    // with a synthesized back stack.
                    TaskStackBuilder.from(this)
                            // If there are ancestor activities, they should be added here.
                            .addNextIntent(upIntent)
                            .startActivities();
                    finish();
                } else {
                    // This activity is part of the application's task, so simply
                    // navigate up to the hierarchical parent activity.
                    NavUtils.navigateUpTo(this, upIntent);
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        collectionPagerAdapter.stopObserving();
    }

    @Override
    public Titles getTitles() {
        return titles;
    }

    public static class CollectionPagerAdapter extends FragmentStatePagerAdapter implements Observer {

        public CollectionPagerAdapter(FragmentManager fm) {
            super(fm);
            titles.addObserver(this);
        }

        @Override
        public void update(Observable o, Object arg) {
            notifyDataSetChanged();
        }

        public void stopObserving() {
            titles.deleteObserver(this);
        }

        @Override
        public Fragment getItem(int i) {

            MeteogrammaFragment fragment = new MeteogrammaFragment();
            Bundle args = new Bundle();
            args.putInt(MeteogrammaFragment.NUMBER_PAGE, i);
            fragment.setArguments(args);

            return fragment;
        }

        @Override
        public int getCount() {
            return PAGES;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return titles.getTitle(position);
        }
    }

/*    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
//        return super.onCreateOptionsMenu(menu);

        super.onCreateOptionsMenu(menu);

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.search_menu, menu);

        // Associate searchable configuration with the SearchView
        SearchManager searchManager =
                (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView =
                (SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setSearchableInfo(
                searchManager.getSearchableInfo(getComponentName()));

        return true;
    }*/

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }
}
