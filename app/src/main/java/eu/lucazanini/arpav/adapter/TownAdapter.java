package eu.lucazanini.arpav.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import eu.lucazanini.arpav.R;

//https://antonioleiva.com/recyclerview-listener/
//http://stackoverflow.com/questions/33845846/why-is-adding-an-onclicklistener-inside-onbindviewholder-of-a-recyclerview-adapt leggi link in risposta
public class TownAdapter extends RecyclerView.Adapter<TownAdapter.ViewHolder> {

    private List<String> townNames;
    private OnItemClickListener listener;

    public TownAdapter(List<String> townNames, OnItemClickListener listener) {
        this.townNames = townNames;
        this.listener = listener;
    }

    public void update(List<String> townNames) {
        this.townNames.clear();
        this.townNames.addAll(townNames);
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.search_row, viewGroup, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        String townName = townNames.get(position);
        viewHolder.getTextView().setText(townName);
//        viewHolder.bind(townNames.get(position), listener);
        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onItemClick(townName);
            }
        });
    }

    @Override
    public int getItemCount() {
        return townNames.size();
    }

    public interface OnItemClickListener {
        void onItemClick(String town);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView textView;

        public ViewHolder(View v) {
            super(v);
            textView = (TextView) v.findViewById(R.id.town_name);
        }

        public TextView getTextView() {
            return textView;
        }

/*        public void bind(final String town, final OnItemClickListener listener) {
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onItemClick(town);
                }
            });
        }*/
    }
}
