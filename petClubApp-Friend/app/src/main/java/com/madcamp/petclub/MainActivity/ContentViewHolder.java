package com.madcamp.petclub.MainActivity;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.DrawableImageViewTarget;
import com.madcamp.petclub.R;

public class ContentViewHolder extends RecyclerView.ViewHolder{
    View mView;
    public ContentViewHolder(@NonNull View itemView) {
        super(itemView);
        mView = itemView;

    }
    public void setTitle(String title){
        TextView post_title = (TextView)mView.findViewById(R.id.post_title);
        post_title.setText(title);
    }
    public void setDesc(String desc){
        TextView post_desc = (TextView)mView.findViewById(R.id.post_desc);
        post_desc.setText(desc);
    }
    public void setImage(Context ctx, String image, String type){
        ImageView post_image = (ImageView) mView.findViewById(R.id.post_image);

        if(type == "image"){
            Glide.with(ctx).load(image).into(post_image);
        }
        else{
            Glide.with(ctx).load(image).into(new DrawableImageViewTarget(post_image));
        }

    }

    public View getmView(){
        return mView;
    }
}
