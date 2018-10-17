package com.sachavs.alartest.fragments.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.sachavs.alartest.OnFragmentListener;
import com.sachavs.alartest.R;
import com.sachavs.alartest.fragments.objects.Item;

import java.util.List;

public class ListAdapter extends RecyclerView.Adapter<ListAdapter.ViewHolder> {

    private List<Item> items;
    private OnFragmentListener listener;

    public ListAdapter(List<Item> items, OnFragmentListener listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        Item item = items.get(position);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final ImageView image;
        public final TextView name;
        public final View view;

        ViewHolder(View view) {
            super(view);
            this.view = view;
            image = view.findViewById(R.id.image);
            name = view.findViewById(R.id.name_view);
        }

        void bind(final Item item) {
            name.setText(item.getName());
            String url = "IMAGE_URL";
            listener.loadImageToView(image, item.getId(), url);

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.openDetail(item);
                }
            });
        }
    }
}
