package com.madcamp.petclub.Board;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.madcamp.petclub.MainActivity.BaseActivity;
import com.madcamp.petclub.MainActivity.models.Comment;
import com.madcamp.petclub.MainActivity.models.Post;
import com.madcamp.petclub.MainActivity.models.User;
import com.madcamp.petclub.MyPets.models.Pet;
import com.madcamp.petclub.R;
import com.madcamp.petclub.login.SharedPreference;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class PostDetailActivity extends BaseActivity implements View.OnClickListener {

    private static final String TAG = "PostDetailActivity";

    public static final String EXTRA_POST_KEY = "post_key";

    private DatabaseReference mPostReference;
    private StorageReference mImageReference;
    private DatabaseReference mCommentsReference;
    private ValueEventListener mPostListener;
    private String mPostKey;
    private CommentAdapter mAdapter;

    private View mAuthor;
    private ImageView mAuthorPhotoView;
    private TextView mAuthorView;
    private TextView mAuthorCompanionView;
    private TextView mTitleView;
    private ImageView mImageView;
    private TextView mBodyView;

    private ImageView mCommentPhotoView;
    private EditText mCommentField;
    private Button mCommentButton;
    private RecyclerView mCommentsRecycler;

    private String userID;
    private String userCompanions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);

        userID = SharedPreference.getAttribute(getApplicationContext(),"userID");
        userCompanions = "WITH  ";

        // Get post key from intent
        mPostKey = getIntent().getStringExtra(EXTRA_POST_KEY);
        if (mPostKey == null) {
            throw new IllegalArgumentException("Must pass EXTRA_POST_KEY");
        }



        // Initialize Database
        mPostReference = FirebaseDatabase.getInstance().getReference()
                .child("posts").child(mPostKey);
        mCommentsReference = FirebaseDatabase.getInstance().getReference()
                .child("post-comments").child(mPostKey);



        // Initialize Views
        mAuthor = (View) findViewById(R.id.Author);
        registerForContextMenu(mAuthor);
        mAuthor.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                Toast.makeText(getApplicationContext(),"clicked",Toast.LENGTH_SHORT).show();
                return false;
            }
        });


        mAuthorPhotoView = findViewById(R.id.postAuthorPhoto);
        mAuthorView = findViewById(R.id.postAuthor);
        mAuthorCompanionView = findViewById(R.id.postAuthorCompanion);
        mTitleView = findViewById(R.id.postTitle);
        mImageView = findViewById(R.id.postImage);
        mImageView.setVisibility(View.GONE);
        mBodyView = findViewById(R.id.postBody);

        mCommentPhotoView = findViewById(R.id.commentPhoto);
        mCommentField = findViewById(R.id.fieldCommentText);
        mCommentButton = findViewById(R.id.buttonPostComment);
        mCommentsRecycler = findViewById(R.id.recyclerPostComments);


        //

        // Initialize Storage

        mCommentButton.setOnClickListener(this);
        mCommentsRecycler.setLayoutManager(new LinearLayoutManager(this));

    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo)
    {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.setHeaderTitle("Context Menu");
        menu.add(0, v.getId(), 0, "INFORMATION");
        menu.add(0, v.getId(), 0, "SEND FRIEND REQUEST");
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if(item.getTitle()=="Action 1"){function1(item.getItemId());}
        else if(item.getTitle()=="Action 2"){function2(item.getItemId());}
        else {return false;}
        return true;
    }

    public void function1(int id){
        Toast.makeText(this, "function 1 called", Toast.LENGTH_SHORT).show();
    }
    public void function2(int id){
        Toast.makeText(this, "function 2 called", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onStart() {
        super.onStart();

        final long ONE_MEGABYTE = 1024 * 1024*1024;

        Log.e("디버그",mAuthorView.getText().toString()+"/"+mTitleView.getText().toString()+"-----------------------------------------------------");

        // Add value event listener to the post
        // [START post_value_event_listener]
        ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Get Post object and use the values to update the UI
                Post post = dataSnapshot.getValue(Post.class);
                final ArrayList<Pet> pet_data = new ArrayList<Pet>();

                if(FirebaseStorage.getInstance().getReference().child(post.author + "/profile/profileImage") != null){
                    final long ONE_MEGABYTE = 1024 * 1024*1024;
                    FirebaseStorage.getInstance().getReference().child(post.author + "/profile/profileImage").getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                        @Override
                        public void onSuccess(byte[] bytes) {
                            // Data for "images/island.jpg" is returns, use this as needed
                            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0 , bytes.length);
                            mAuthorPhotoView.setImageBitmap(bitmap);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            // Handle any errors
                            Log.e("디버그","프로필 사진 못 봐~~~~~~~~~~");
                        }
                    });
                }
                mAuthorView.setText(post.author);
                mTitleView.setText(post.title);
                mBodyView.setText(post.body);

                /*
                FirebaseFirestore.getInstance().collection("pet").document(post.uid).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>(){
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        DocumentSnapshot documentSnapshot = task.getResult();
                        for(documentSnapshot : documentSnapshot.get("petSpecies")){
                        }
                    }
                });
                */


                FirebaseDatabase.getInstance().getReference("/pet/"+post.uid).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        System.out.println("Done clear the pet data");
                        pet_data.clear();
                        for (DataSnapshot postSnapshot: dataSnapshot.getChildren()){
                            String key = postSnapshot.getKey();
                            System.out.println("key is "+key);
                            //postSnapshot.child("petSpecies").getValue();
                            //Pet get = postSnapshot.getValue(Pet.class);
                            userCompanions = userCompanions + postSnapshot.child("petSpecies").getValue().toString() + "   ";
                            Log.e("디버그",postSnapshot.child("petSpecies").getValue().toString());
                            Log.e("디버그","key is "+key);
                            Log.e("디버그",userCompanions+"!!!!!!!!!!!!!!!!!!!!!!!");
                            //pet_data.add(get);
                        }
                    }


                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

                mImageReference = FirebaseStorage.getInstance().getReference().child(post.author+"/"+post.title);
                Log.e("디버그",post.author+"/"+post.title+"-----------------------------------------------------");

                mImageReference.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                    @Override
                    public void onSuccess(byte[] bytes) {
                        // Data for "images/island.jpg" is returns, use this as needed
                        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0 , bytes.length);
                        mImageView.setVisibility(mImageView.VISIBLE);
                        mImageView.setImageBitmap(bitmap);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Handle any errors
                        //Toast.makeText(getApplicationContext(),"이미지를 로드할 수 없습니다.",Toast.LENGTH_LONG).show();
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
                // [START_EXCLUDE]
                Toast.makeText(PostDetailActivity.this, "Failed to load post.",
                        Toast.LENGTH_SHORT).show();
                // [END_EXCLUDE]
            }
        };
        mPostReference.addValueEventListener(postListener);
        // [END post_value_event_listener]


        // Keep copy of post listener so we can remove it when app stops
        mPostListener = postListener;

        // Listen for comments
        mAdapter = new CommentAdapter(this, mCommentsReference);
        mCommentsRecycler.setAdapter(mAdapter);
    }

    @Override
    public void onStop() {
        super.onStop();

        // Remove post value event listener
        if (mPostListener != null) {
            mPostReference.removeEventListener(mPostListener);
        }

        // Clean up comments listener
        mAdapter.cleanupListener();
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.buttonPostComment) {
            postComment();
        }
    }

    private void postComment() {
        final String uid = getUid();
        FirebaseDatabase.getInstance().getReference().child("users").child(uid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        // Get user information
                        User user = dataSnapshot.getValue(User.class);
                        String authorName = user.userID;

                        // Create new comment object
                        String commentText = mCommentField.getText().toString();
                        Comment comment = new Comment(uid, authorName, commentText);

                        // Push the comment, it will appear in the list
                        mCommentsReference.push().setValue(comment);
                        if(FirebaseStorage.getInstance().getReference().child(user.userID + "/profile/profileImage") != null){
                            final long ONE_MEGABYTE = 1024 * 1024*1024;
                            FirebaseStorage.getInstance().getReference().child(user.userID + "/profile/profileImage").getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                                @Override
                                public void onSuccess(byte[] bytes) {
                                    // Data for "images/island.jpg" is returns, use this as needed
                                    Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0 , bytes.length);
                                    mCommentPhotoView.setImageBitmap(bitmap);
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception exception) {
                                    // Handle any errors
                                    Log.e("디버그","프로필 사진 못 봐~~~~~~~~~~ post");
                                }
                            });
                        }

                        // Clear the field
                        mCommentField.setText(null);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    private static class CommentViewHolder extends RecyclerView.ViewHolder {

        public TextView authorView;
        public TextView bodyView;
        public ImageView profileView;

        public CommentViewHolder(View itemView) {
            super(itemView);

            authorView = itemView.findViewById(R.id.commentAuthor);
            bodyView = itemView.findViewById(R.id.commentBody);
            profileView = itemView.findViewById(R.id.commentPhoto);
        }
    }

    private static class CommentAdapter extends RecyclerView.Adapter<CommentViewHolder> {

        private Context mContext;
        private DatabaseReference mDatabaseReference;
        private StorageReference mStorageReference;
        private ChildEventListener mChildEventListener;

        private List<String> mCommentIds = new ArrayList<>();
        private List<Comment> mComments = new ArrayList<>();
        private List<Bitmap> mCommentPhotos = new ArrayList<>();

        public CommentAdapter(final Context context, DatabaseReference ref) {
            final long ONE_MEGABYTE = 1024 * 1024*1024;

            mContext = context;
            mDatabaseReference = ref;
            mStorageReference = FirebaseStorage.getInstance().getReference();


            // Create child event listener
            // [START child_event_listener_recycler]
            ChildEventListener childEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
                    Log.d(TAG, "onChildAdded:" + dataSnapshot.getKey());

                    // A new comment has been added, add it to the displayed list
                    Comment comment = dataSnapshot.getValue(Comment.class);

                    // [START_EXCLUDE]
                    // Update RecyclerView
                    mCommentIds.add(dataSnapshot.getKey());
                    mComments.add(comment);
                    if(mStorageReference.child(comment.uid+"/profile/profileImage") != null) {
                        mStorageReference.child(comment.uid+"/profile/profileImage").getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                            @Override
                            public void onSuccess(byte[] bytes) {
                                // Data for "images/island.jpg" is returns, use this as needed
                                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0 , bytes.length);
                                mCommentPhotos.add(bitmap);
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception exception) {
                                // Handle any errors
                                Log.e("디버그","프로필 사진 못 봐~~~~~~~~~~ post");
                            }
                        });
                    }
                    notifyItemInserted(mComments.size() - 1);
                    // [END_EXCLUDE]
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {
                    Log.d(TAG, "onChildChanged:" + dataSnapshot.getKey());

                    // A comment has changed, use the key to determine if we are displaying this
                    // comment and if so displayed the changed comment.
                    Comment newComment = dataSnapshot.getValue(Comment.class);
                    String commentKey = dataSnapshot.getKey();

                    if(mStorageReference.child(newComment.uid+"/profile/profileImage") != null) {
                        mStorageReference.child(newComment.uid+"/profile/profileImage").getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                            @Override
                            public void onSuccess(byte[] bytes) {
                                // Data for "images/island.jpg" is returns, use this as needed
                                Bitmap newCommentPhoto = BitmapFactory.decodeByteArray(bytes, 0 , bytes.length);
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception exception) {
                                // Handle any errors
                                Log.e("디버그","프로필 사진 못 봐~~~~~~~~~~ post");
                            }
                        });
                    }

                    // [START_EXCLUDE]
                    final int commentIndex = mCommentIds.indexOf(commentKey);
                    if (commentIndex > -1) {
                        // Replace with the new data
                        mComments.set(commentIndex, newComment);
                        if(mStorageReference.child(newComment.uid+"/profile/profileImage") != null) {
                            mStorageReference.child(newComment.uid+"/profile/profileImage").getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                                @Override
                                public void onSuccess(byte[] bytes) {
                                    // Data for "images/island.jpg" is returns, use this as needed
                                    Bitmap newCommentPhoto = BitmapFactory.decodeByteArray(bytes, 0 , bytes.length);
                                    mCommentPhotos.set(commentIndex, newCommentPhoto);
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception exception) {
                                    // Handle any errors
                                    Log.e("디버그","프로필 사진 못 봐~~~~~~~~~~ post");
                                }
                            });
                        }

                        // Update the RecyclerView
                        notifyItemChanged(commentIndex);
                    } else {
                        Log.w(TAG, "onChildChanged:unknown_child:" + commentKey);
                    }
                    // [END_EXCLUDE]
                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {
                    Log.d(TAG, "onChildRemoved:" + dataSnapshot.getKey());

                    // A comment has changed, use the key to determine if we are displaying this
                    // comment and if so remove it.
                    String commentKey = dataSnapshot.getKey();

                    // [START_EXCLUDE]
                    int commentIndex = mCommentIds.indexOf(commentKey);
                    if (commentIndex > -1) {
                        // Remove data from the list
                        mCommentIds.remove(commentIndex);
                        mComments.remove(commentIndex);
                        mCommentPhotos.remove(commentIndex);

                        // Update the RecyclerView
                        notifyItemRemoved(commentIndex);
                    } else {
                        Log.w(TAG, "onChildRemoved:unknown_child:" + commentKey);
                    }
                    // [END_EXCLUDE]
                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName) {
                    Log.d(TAG, "onChildMoved:" + dataSnapshot.getKey());

                    // A comment has changed position, use the key to determine if we are
                    // displaying this comment and if so move it.
                    Comment movedComment = dataSnapshot.getValue(Comment.class);
                    String commentKey = dataSnapshot.getKey();

                    // ...
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.w(TAG, "postComments:onCancelled", databaseError.toException());
                    Toast.makeText(mContext, "Failed to load comments.",
                            Toast.LENGTH_SHORT).show();
                }
            };
            ref.addChildEventListener(childEventListener);
            // [END child_event_listener_recycler]

            // Store reference to listener so it can be removed on app stop
            mChildEventListener = childEventListener;
        }

        @Override
        public CommentViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            View view = inflater.inflate(R.layout.item_comment, parent, false);
            return new CommentViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final CommentViewHolder holder, int position) {
            Comment comment = mComments.get(position);
            holder.authorView.setText(comment.author);
            holder.bodyView.setText(comment.text);

            if(FirebaseStorage.getInstance().getReference().child(comment.uid + "/profile/profileImage") != null){
                final long ONE_MEGABYTE = 1024 * 1024*1024;
                FirebaseStorage.getInstance().getReference().child(comment.uid + "/profile/profileImage").getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                    @Override
                    public void onSuccess(byte[] bytes) {
                        // Data for "images/island.jpg" is returns, use this as needed
                        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0 , bytes.length);
                        holder.profileView.setImageBitmap(bitmap);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Handle any errors
                        Log.e("디버그","프로필 사진 못 봐~~~~~~~~~~ binding");
                    }
                });
            }
        }

        @Override
        public int getItemCount() {
            return mComments.size();
        }

        public void cleanupListener() {
            if (mChildEventListener != null) {
                mDatabaseReference.removeEventListener(mChildEventListener);
            }
        }

    }
}