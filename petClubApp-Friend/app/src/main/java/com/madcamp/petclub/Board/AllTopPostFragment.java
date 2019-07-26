package com.madcamp.petclub.Board;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.madcamp.petclub.MainActivity.PostListFragment;

public class AllTopPostFragment extends PostListFragment {

    public AllTopPostFragment() {
    }

    @Override
    public Query getQuery(DatabaseReference databaseReference, int order) {
        // [START recent_posts_query]
        // Last 100 posts, these are automatically the 100 most recent
        // due to sorting by push() keys
        Query recentPostsQuery = databaseReference.child("posts").orderByChild("starCount");
        // [END recent_posts_query]

        return recentPostsQuery;
    }
}