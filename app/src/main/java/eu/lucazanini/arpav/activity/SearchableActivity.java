package eu.lucazanini.arpav.activity;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;

import java.lang.ref.WeakReference;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import eu.lucazanini.arpav.R;
import eu.lucazanini.arpav.adapter.TownAdapter;
import eu.lucazanini.arpav.database.TownDataSource;
import eu.lucazanini.arpav.helper.LocaleHelper;

/**
 * It shows the list of towns in Veneto
 */
public class SearchableActivity extends AppCompatActivity {

    public final static int FAVOURITE_TOWN_CODE = 0;
    public final static int TEMPORARY_TOWN_CODE = 1;
    public final static String TOWN_NAME = "town_name";
    protected @BindView(R.id.searchableToolbar) Toolbar searchableToolbar;
    protected @BindView(R.id.search_list) RecyclerView recyclerView;
    private TownAdapter townAdapter;

    public static Intent getIntent(Context context) {
        return new Intent(context, SearchableActivity.class);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_searchable);

        ButterKnife.bind(this);

        recyclerView.setHasFixedSize(true);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        AsyncTask<Void, Void, List<String>> readTowns = new ReadTowns(this).execute();

        setSupportActionBar(searchableToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.search_menu, menu);

        SearchManager searchManager =
                (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView =
                (SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setSearchableInfo(
                searchManager.getSearchableInfo(getComponentName()));

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.length() > 1) {
                    AsyncTask<String, Void, List<String>> readFilteredTowns = new ReadFilteredTowns(SearchableActivity.this).execute(newText);
                } else {
                    AsyncTask<Void, Void, List<String>> readTowns = new ReadTowns(SearchableActivity.this).execute();
                }
                return false;
            }
        });

        return true;
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase));
    }

    /**
     * It manages the town list
     */
    private class ReadTowns extends AsyncTask<Void, Void, List<String>> {

        private final WeakReference<SearchableActivity> weakActivity;

        private ReadTowns(SearchableActivity activity) {
            weakActivity = new WeakReference<>(activity);
        }

        @Override
        protected List<String> doInBackground(Void... params) {
            final SearchableActivity activity = weakActivity.get();
            List<String> townNames = null;
            TownDataSource townDataSource = new TownDataSource(activity);
            if (activity != null) {
                townDataSource.open();
                townNames = townDataSource.getTownNames();
            }
            return townNames;
        }

        @Override
        protected void onPostExecute(List<String> townNames) {
            final SearchableActivity activity = weakActivity.get();
            if (activity != null && townNames != null) {
                activity.townAdapter = new TownAdapter(townNames, new TownAdapter.OnItemClickListener() {
                    @Override
                    public void onItemClick(String town) {
                        Intent resultIntent = new Intent();
                        resultIntent.putExtra(TOWN_NAME, town);
                        activity.setResult(Activity.RESULT_OK, resultIntent);
                        activity.finish();
                    }
                });
                activity.recyclerView.setAdapter(townAdapter);
            }
            TownDataSource townDataSource = new TownDataSource(activity);
            townDataSource.close();
        }
    }

    /**
     * It manages the towns matching the text in the search view widget
     */
    private class ReadFilteredTowns extends AsyncTask<String, Void, List<String>> {

        private final WeakReference<SearchableActivity> weakActivity;

        private ReadFilteredTowns(SearchableActivity activity) {
            weakActivity = new WeakReference<>(activity);
        }

        @Override
        protected List<String> doInBackground(String... params) {
            final SearchableActivity activity = weakActivity.get();
            String like = params[0];
            List<String> townNames = null;
            TownDataSource townDataSource = new TownDataSource(activity);
            if (activity != null) {
                townDataSource.open();
                townNames = townDataSource.getTownNames(like);
            }
            return townNames;
        }

        @Override
        protected void onPostExecute(List<String> townNames) {
            final SearchableActivity activity = weakActivity.get();
            if (activity != null && townNames != null) {
                activity.townAdapter.update(townNames);
            }
            TownDataSource townDataSource = new TownDataSource(activity);
            townDataSource.close();
        }
    }
}
