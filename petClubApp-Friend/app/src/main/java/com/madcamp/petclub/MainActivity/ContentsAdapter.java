package com.madcamp.petclub.MainActivity;


import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.madcamp.petclub.MainActivity.models.Contents;
import com.madcamp.petclub.News.NewsWebView;
import com.madcamp.petclub.R;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;

public class ContentsAdapter extends FirebaseRecyclerAdapter<Contents, ContentViewHolder> {
    Context context;

    public ContentsAdapter(@NonNull FirebaseRecyclerOptions<Contents> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull ContentViewHolder holder, int i, @NonNull final Contents contents) {
        String title, desc, type;
        title = contents.getContentTitle();
        desc = contents.getContentDesc();
        type = contents.getContentDesc();
        if (title.length() > 10) {title = title.substring(0, 10)+"...";}
        if (desc.length()>15) {desc = desc.substring(0, 15)+"...";}

        holder.setTitle(title);
        holder.setDesc(desc);
        holder.setImage(context, contents.getContentImage(), type);

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String url = contents.getContentUrl();
                Intent intent = new Intent(context, NewsWebView.class);
                intent.putExtra("id", url);
                context.startActivity(intent);
            }
        });
    }


    @NonNull
    @Override
    public ContentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.news_row, parent, false);

        context = parent.getContext();

        return new ContentViewHolder(view);
    }
}
