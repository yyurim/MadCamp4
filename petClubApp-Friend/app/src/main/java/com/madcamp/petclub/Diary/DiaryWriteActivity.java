package com.madcamp.petclub.Diary;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import static com.madcamp.petclub.Diary.DiaryActivity.diary_data;

public class DiaryWriteActivity extends AppCompatActivity {
    public static EditText write_title, write_contents;
    public ImageView diary_image;
    private FloatingActionButton addImage;
    private TextView dateView, monthView;

    private static final int PICK_FROM_ALBUM = 1;
    private static final String REQUIRED = "Required";
    private int flagImage = 0;

    private DatabaseReference databaseReference;
    private StorageReference mStorage;

    String title, contents, petName, saveDate;
    private String userID;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        Intent intent = getIntent();
        petName = intent.getStringExtra("petName");
        saveDate = intent.getStringExtra("saveDate");
        userID = SharedPreference.getAttribute(getApplicationContext(), "userID");
        System.out.println("userID in write mode is "+userID);
        setContentView(R.layout.diary_tab_write);
        // requestWindowFeature(Window.FEATURE_NO_TITLE);
        // getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        dateView = (TextView) findViewById(R.id.write_date);
        monthView = (TextView) findViewById(R.id.write_month);

        String mth = saveDate.split("-")[1];
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
        dateView.setText(saveDate.split("-")[2]);

        userID = SharedPreference.getAttribute(getApplicationContext(),"userID");
        databaseReference = FirebaseDatabase.getInstance().getReference();
        mStorage = FirebaseStorage.getInstance().getReference();

        write_title = (EditText)findViewById(R.id.edit_title_write);
        diary_image = (ImageView) findViewById(R.id.imgPreview);
        write_contents = (EditText)findViewById(R.id.edit_contents_write);

        title = write_title.getText().toString();
        contents = write_contents.getText().toString();

        addImage = findViewById(R.id.write_addImage);
        addImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
                startActivityForResult(intent, PICK_FROM_ALBUM);
            }
        });

        if (TextUtils.isEmpty(title)){
            write_title.setError(REQUIRED);
        }
        if (TextUtils.isEmpty(contents)){
            write_contents.setError(REQUIRED);
        }



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
        title = write_title.getText().toString();
        contents = write_contents.getText().toString();

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Calendar time = Calendar.getInstance();
        String format_time1 = format.format(time.getTime());

        //String key = databaseReference.child("diary").push().getKey();
        DiaryClass diaryClass = new DiaryClass(userID, title, contents, saveDate);
        Map<String, Object> childUpdates= new HashMap<>();
        childUpdates.put("/pet/"+userID+"/"+petName+"/"+saveDate, diaryClass.toMap());
        databaseReference.updateChildren(childUpdates);
        System.out.println("new diary added");

        if(flag == 1) {
            // [START upload_memory]
            diary_image.setDrawingCacheEnabled(true);
            diary_image.buildDrawingCache();

            Bitmap bitmap = ((BitmapDrawable) diary_image.getDrawable()).getBitmap();
            ByteArrayOutputStream uploadStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, uploadStream);
            byte[] bytes = uploadStream.toByteArray();

            UploadTask uploadTask = mStorage.child("pet/"+ userID + "/"+ petName+ "/diary/" + format_time1 + "/photo").putBytes(bytes);
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
        getMenuInflater().inflate(R.menu.menu_new_diary, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.write_diary) {
            submitDiary(flagImage);
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}