package eu.lucazanini.arpav;

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
import eu.lucazanini.arpav.database.TownDataSource;
import eu.lucazanini.arpav.model.MyAdapter;
import timber.log.Timber;

public class SearchableActivity  extends AppCompatActivity {

    protected @BindView(R.id.search_list) RecyclerView mRecyclerView;
//    protected @BindView(R.id.list) ListView mListView;
//    private RecyclerView.Adapter mAdapter;
    private MyAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
//    protected String[] myDataset;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_searchable);

        ButterKnife.bind(this);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        AsyncTask<Void, Void, List<String>> readTowns = new ReadTowns(this).execute();

        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
    }

    @Override
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

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Timber.d("onQueryTextSubmit "+query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                Timber.d("onQueryTextChange "+newText);
                if(newText.length()>1) {
                    AsyncTask<String, Void, List<String>> readFilteredTowns = new ReadFilteredTowns(SearchableActivity.this).execute(newText);
                } else {
                    AsyncTask<Void, Void, List<String>> readTowns = new ReadTowns(SearchableActivity.this).execute();
                }
                return false;
            }
        });

        return true;
    }

    public static Intent getIntent(Context context){
        Intent intent = new Intent(context, SearchableActivity.class);
       return  intent;
    }

    private class ReadTowns extends AsyncTask<Void, Void, List<String>>{

        private final WeakReference<SearchableActivity> weakActivity;

        private ReadTowns(SearchableActivity activity) {
            weakActivity = new WeakReference<>(activity);
        }

        @Override
        protected List<String> doInBackground(Void... params) {
            final SearchableActivity activity = weakActivity.get();
            List<String> townNames = null;
            TownDataSource townDataSource = new TownDataSource(activity);
            if(activity != null) {
                townDataSource.open();
                townNames = townDataSource.getTownNames();
            }
            return townNames;
        }

        @Override
        protected void onPostExecute(List<String> townNames) {
            final SearchableActivity activity = weakActivity.get();
            if(activity != null && townNames!=null) {
                activity.mAdapter = new MyAdapter(townNames);
                activity.mRecyclerView.setAdapter(mAdapter);
            }
            TownDataSource townDataSource = new TownDataSource(activity);
            townDataSource.close();
        }
    }

    private class ReadFilteredTowns extends AsyncTask<String, Void, List<String>>{

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
            if(activity != null) {
                townDataSource.open();
                townNames = townDataSource.getTownNames(like);
            }
            return townNames;
        }

        @Override
        protected void onPostExecute(List<String> townNames) {
            final SearchableActivity activity = weakActivity.get();
            if(activity != null && townNames!=null) {
                activity.mAdapter.update(townNames);
//                activity.mAdapter = new MyAdapter(townNames);
//                activity.mRecyclerView.setAdapter(mAdapter);
//                activity.mAdapter.notifyDataSetChanged();
            }
            TownDataSource townDataSource = new TownDataSource(activity);
            townDataSource.close();
        }
    }

}
