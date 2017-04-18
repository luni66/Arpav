package eu.lucazanini.arpav.model;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import eu.lucazanini.arpav.R;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {
//    private static final String TAG = "CustomAdapter";

//    private String[] mDataSet;
    private List<String> mDataset;

    // BEGIN_INCLUDE(recyclerViewSampleViewHolder)
    /**
     * Provide a reference to the type of views that you are using (custom ViewHolder)
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView textView;

        public ViewHolder(View v) {
            super(v);
            textView = (TextView) v.findViewById(R.id.town_name);
        }

        public TextView getTextView() {
            return textView;
        }
    }
    // END_INCLUDE(recyclerViewSampleViewHolder)
/*
    *//**
     * Initialize the dataset of the Adapter.
     *
//     * @param dataSet String[] containing the data to populate views to be used by RecyclerView.
//     *//*
    public MyAdapter(String[] dataSet) {
        mDataSet = dataSet;
    }*/
    public MyAdapter(List<String> myDataset) {
        mDataset = myDataset;
    }

    public void update(List<String> myDataset) {
        mDataset.clear();
        mDataset.addAll(myDataset);
        notifyDataSetChanged();
    }

    // BEGIN_INCLUDE(recyclerViewOnCreateViewHolder)
    // Create new views (invoked by the layout manager)
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view.
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.search_row, viewGroup, false);

        return new ViewHolder(v);
    }
    // END_INCLUDE(recyclerViewOnCreateViewHolder)

    // BEGIN_INCLUDE(recyclerViewOnBindViewHolder)
    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {

        // Get element from your dataset at this position and replace the contents of the view
        // with that element
//        viewHolder.getTextView().setText(mDataSet[position]);
        viewHolder.getTextView().setText(mDataset.get(position));
    }
    // END_INCLUDE(recyclerViewOnBindViewHolder)

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
//        return mDataSet.length;
        return mDataset.size();
    }
}
