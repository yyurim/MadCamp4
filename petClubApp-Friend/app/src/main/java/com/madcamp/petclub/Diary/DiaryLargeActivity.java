package com.madcamp.petclub.Diary;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.madcamp.petclub.R;

import com.madcamp.petclub.login.SharedPreference;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;


import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

import static com.madcamp.petclub.Diary.DiaryActivity.diary_data;


public class DiaryLargeActivity extends AppCompatActivity {

    ViewPager pager;
    int position = 0;
    String key, userID, petName, day, month;
    public static EditText update_title, update_contents;
    public static Button edit_btn, delete_btn;
    private DatabaseReference diary_selecteddate;
    private TextView dateView, monthView;
    private ImageView diary_image;
    private static final int PICK_FROM_ALBUM = 1;

    private FloatingActionButton addImage;
    private int flagImage = 0;

    private DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
    private StorageReference mStorage;

    Map<String, Object> childUpdates;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diary_detail);
        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        //getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        userID = SharedPreference.getAttribute(getApplicationContext(),"userID");
        mStorage = FirebaseStorage.getInstance().getReference();

        Intent intent = getIntent();
        petName = intent.getStringExtra("petName");

        update_title = (EditText) findViewById(R.id.edit_title_update);
        update_contents = (EditText) findViewById(R.id.edit_contents_update);

        dateView = (TextView) findViewById(R.id.date);
        monthView = (TextView) findViewById(R.id.month);

        update_title = (EditText) findViewById(R.id.edit_title_update);
        update_title.setText(diary_data.get(position).getTitle());

        diary_image = (ImageView) findViewById(R.id.imgPreview);

        update_contents = (EditText) findViewById(R.id.edit_contents_update);
        update_contents.setText(diary_data.get(position).getContents());

        addImage = findViewById(R.id.addImage);
        addImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
                startActivityForResult(intent, PICK_FROM_ALBUM);
            }
        });

        key = diary_data.get(position).getDate();
        String mth = key.split("-")[1];
        if (mth=="1") { monthView.setText("January"); }
        if (mth=="2") { monthView.setText("February"); }
        if (mth=="3") { monthView.setText("March"); }
        if (mth=="4") { monthView.setText("April"); }
        if (mth=="5") { monthView.setText("May"); }
        if (mth=="6") { monthView.setText("June"); }
        if (mth=="7") { monthView.setText("July"); }
        if (mth=="8") { monthView.setText("August"); }
        if (mth=="9") { monthView.setText("September"); }
        if (mth=="10") { monthView.setText("October"); }
        if (mth=="11") { monthView.setText("November"); }
        if (mth=="12") { monthView.setText("December"); }
        dateView.setText(key.split("-")[2]);
        System.out.println("key Date number is "+key);
    }

    @Override
    public void onStart(){
        super.onStart();
        final long ONE_MEGABYTE = 1024 * 1024*1024;
        FirebaseStorage.getInstance().getReference("pet/"+ userID + "/"+ petName+ "/diary/" + diary_data.get(position).getDate() ).child("photo").getDownloadUrl().addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getApplicationContext(), "못 갖고왔다!", Toast.LENGTH_SHORT).show();
            }
        }).addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Glide.with(getApplicationContext()).load(uri).into(diary_image);
            }

        });
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            if (resultCode == RESULT_OK) {
                if (requestCode==PICK_FROM_ALBUM) {
                    diary_image.setVisibility(diary_image.VISIBLE);
                    Glide.with(getApplicationContext()).load(data.getData()).into(diary_image);
                    //diary_image.setVisibility(diary_image.VISIBLE);
                    //diary_image.setImageURI(data.getData());
                    flagImage = 1;
                }
            }
        } catch (Exception e) {
            Log.e("FileSelectorActivity", "File select error", e);
        }

    }

    private void submitDiary(int flag) {
        final String revised_title = update_title.getText().toString();
        final String revised_contents = update_contents.getText().toString();

        System.out.println("new content is "+update_contents);

        DiaryClass diaryClass= new DiaryClass(userID, revised_title, revised_contents, key);
        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/pet/"+userID+"/"+petName+"/"+key, diaryClass.toMap());
        databaseReference.updateChildren(childUpdates);
        System.out.println("done revising");
        // [END single_value_read]

        if(flag == 1) {
            // [START upload_memory]
            diary_image.setDrawingCacheEnabled(true);
            diary_image.buildDrawingCache();

            Bitmap bitmap = ((BitmapDrawable) diary_image.getDrawable()).getBitmap();
            ByteArrayOutputStream uploadStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, uploadStream);
            byte[] bytes = uploadStream.toByteArray();

            UploadTask uploadTask = mStorage.child("pet/"+ userID + "/"+ petName+ "/diary/" + key + "/photo").putBytes(bytes);
            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Handle unsuccessful uploads
                    Log.e("디버그", "업로드안됨*****************************");
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
                    // ...
                    Log.e("디버그", "업로드됨!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!11*****************************");
                }
            });
            // [END upload_memory]
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_diary, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch(id){
            case R.id.edit_diary:
                submitDiary(flagImage);
                finish();
                break;

            case R.id.delete_diary:
                childUpdates = new HashMap<>();
                childUpdates.put("/pet/"+userID+"/"+petName+"/"+key, null);
                databaseReference.updateChildren(childUpdates);
                System.out.println("done deleting");
                finish();
                break;


        }
        //noinspection SimplifiableIfStatement
        if (id == R.id.edit_diary) {

            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}