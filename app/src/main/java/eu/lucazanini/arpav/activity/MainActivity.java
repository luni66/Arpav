package eu.lucazanini.arpav.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import java.util.Observable;
import java.util.Observer;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;
import eu.lucazanini.arpav.fragment.MeteogrammaFragment;
import eu.lucazanini.arpav.R;
import eu.lucazanini.arpav.location.CurrentLocation;
import eu.lucazanini.arpav.model.SlideTitles;
import timber.log.Timber;

import static eu.lucazanini.arpav.fragment.MeteogrammaFragment.REQUEST_CODE;

// http://stackoverflow.com/questions/23133912/android-viewpager-update-off-screen-but-cached-fragments-in-viewpager

// https://guides.codepath.com/android/ViewPager-with-FragmentPagerAdapter

public class MainActivity extends AppCompatActivity implements TitlesCallBack, Observer {

    private static final int PAGES = 7;
    private static final int PAGES_LIMIT = 7;
    protected @BindView(R.id.pager) ViewPager pager;
    protected @BindView(R.id.my_toolbar) Toolbar myToolbar;
    protected @BindString(R.string.action_title) String defaultTitle;
    private CollectionPagerAdapter collectionPagerAdapter;
    private CurrentLocation currentLocation;
    private ActionBar actionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        collectionPagerAdapter = new CollectionPagerAdapter(getSupportFragmentManager());

        pager.setOffscreenPageLimit(PAGES_LIMIT);
        pager.setAdapter(collectionPagerAdapter);

//        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        actionBar = getSupportActionBar();

        currentLocation = CurrentLocation.getInstance();

        if(currentLocation.isDefined()){
            actionBar.setTitle(currentLocation.getTown().getName());
        } else{
            actionBar.setTitle(defaultTitle);
        }

        currentLocation.addObserver(this);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Timber.d("onActivityResult requestCode "+requestCode);
//        super.onActivityResult(requestCode, resultCode, data);
        // Check which request we're responding to
        if (requestCode == REQUEST_CODE) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                String town  = data.getStringExtra(SearchableActivity.TOWN_NAME);
                currentLocation.setTown(town, this);
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
            case R.id.action_search:
                Intent intent = SearchableActivity.getIntent(this);
                startActivityForResult(intent, REQUEST_CODE);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        currentLocation.deleteObserver(this);
        collectionPagerAdapter.stopObserving();
    }

    @Override
    public SlideTitles getTitles() {
        return SLIDE_TITLES;
    }

    @Override
    public void update(Observable o, Object arg) {
        if(o instanceof CurrentLocation) {
            String title;
            if(arg!=null) {
                title = arg.toString();
            } else{
                title = defaultTitle;
            }
            actionBar.setTitle(title);
        }
    }

    public static class CollectionPagerAdapter extends FragmentStatePagerAdapter implements Observer {

        public CollectionPagerAdapter(FragmentManager fm) {
            super(fm);
            SLIDE_TITLES.addObserver(this);
        }

        @Override
        public void update(Observable o, Object arg) {
            notifyDataSetChanged();
        }

        public void stopObserving() {
            SLIDE_TITLES.deleteObserver(this);
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
            return SLIDE_TITLES.getSlideTitle(position);
        }
    }
}