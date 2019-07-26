package com.madcamp.petclub.MyInfo;

import com.madcamp.petclub.MainActivity.PostListFragment;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;

public class MyTopPostsFragment extends PostListFragment {
    public MyTopPostsFragment() {}

    @Override
    public Query getQuery(DatabaseReference databaseReference, int order) {
        // [START my_top_posts_query]
        // My top posts by number of stars
        String myUserId = getUid();
        Query myTopPostsQuery = databaseReference.child("posts").child(myUserId)
                .orderByChild("starCount");
        // [END my_top_posts_query]

        return myTopPostsQuery;
    }
}