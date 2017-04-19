package eu.lucazanini.arpav.model;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import eu.lucazanini.arpav.R;
import timber.log.Timber;

//http://stackoverflow.com/questions/24885223/why-doesnt-recyclerview-have-onitemclicklistener-and-how-recyclerview-is-dif
//https://antonioleiva.com/recyclerview-listener/
//http://stackoverflow.com/questions/33845846/why-is-adding-an-onclicklistener-inside-onbindviewholder-of-a-recyclerview-adapt leggi link in risposta
public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {
//    private static final String TAG = "CustomAdapter";

//    private String[] mDataSet;
    private List<String> mDataset;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(String town);
    }

    // BEGIN_INCLUDE(recyclerViewSampleViewHolder)
    /**
     * Provide a reference to the type of views that you are using (custom ViewHolder)
     */
//    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    public static class ViewHolder extends RecyclerView.ViewHolder {
    private final TextView textView;

        public ViewHolder(View v) {
            super(v);
//            v.setOnClickListener(this);
            textView = (TextView) v.findViewById(R.id.town_name);
        }

        public TextView getTextView() {
            return textView;
        }

        public void bind(final String town, final OnItemClickListener listener) {
//            name.setText(item.name);
//            Picasso.with(itemView.getContext()).load(item.imageUrl).into(image);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onItemClick(town);
                }
            });
        }

//        @Override
//        public void onClick(View v) {
//            Timber.d("test on click "+ getAdapterPosition());
//            MyAdapter.listener.onItemClick(textView.getText().toString());
//        }
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
    public MyAdapter(List<String> myDataset, OnItemClickListener listener) {
        mDataset = myDataset;
        this.listener = listener;
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

/*        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Timber.d("onClick");
                int itemPosition = mRecyclerView.getChildLayoutPosition(view);
                String item = mList.get(itemPosition);
//                Toast.makeText(mContext, item, Toast.LENGTH_LONG).show();
            }
        });*/

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

        viewHolder.bind(mDataset.get(position), listener);

/*        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Timber.d("onClick at " + viewHolder.getTextView().getText());

            }
        });*/

    }
    // END_INCLUDE(recyclerViewOnBindViewHolder)

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
//        return mDataSet.length;
        return mDataset.size();
    }


}
