package com.madcamp.petclub.MyPets;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.madcamp.petclub.MyPets.models.Pet;
import com.madcamp.petclub.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

public class PetWriteActivity extends AppCompatActivity {
    private static final int PICK_FROM_ALBUM = 1;
    int flagImage=0;

    private ImageView profileImage;
    public static EditText write_name, write_age, write_gender, write_species,
            write_neutralization, write_bf, write_firstdate;
    private static final String REQUIRED = "Required";
    public static Button save_btn;
    private DatabaseReference databaseReference;
    private StorageReference mStorage;
    private String name, age, gender, species, neutralization, bf, firstdate;

    private Uri petPhotoUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        final String userID = ((Intent) intent).getStringExtra("userID");

        setContentView(R.layout.pet_write);
        databaseReference = FirebaseDatabase.getInstance().getReference();
        mStorage = FirebaseStorage.getInstance().getReference();

        profileImage = (ImageView) findViewById(R.id.pet_profile);
        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
                startActivityForResult(intent, PICK_FROM_ALBUM);
            }
        });

        write_name = (EditText) findViewById(R.id.new_name);
        write_age = (EditText) findViewById(R.id.new_age);
        write_gender = (EditText) findViewById(R.id.new_gender);
        write_species = (EditText) findViewById(R.id.new_species);
        write_neutralization = (EditText) findViewById(R.id.new_neutralization);
        write_bf = (EditText) findViewById(R.id.new_bf);
        write_firstdate = (EditText) findViewById(R.id.new_firstDate);

        name = write_name.getText().toString();
        age = write_age.getText().toString();
        gender = write_gender.getText().toString();
        species = write_species.getText().toString();
        neutralization = write_neutralization.getText().toString();
        bf = write_bf.getText().toString();
        firstdate = write_firstdate.getText().toString();

        if (TextUtils.isEmpty(name)) {
            write_name.setError(REQUIRED);
        }
        if (TextUtils.isEmpty(firstdate)) {
            write_firstdate.setError(REQUIRED);
        }
        if (TextUtils.isEmpty(species)) {
            write_species.setError(REQUIRED);
        }


        save_btn = (Button) findViewById(R.id.save_pet);
        save_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                name = write_name.getText().toString();
                age = write_age.getText().toString();
                gender = write_gender.getText().toString();
                species = write_species.getText().toString();
                neutralization = write_neutralization.getText().toString();
                bf = write_bf.getText().toString();
                firstdate = write_firstdate.getText().toString();

                String key = name;
                Pet pet = new Pet(userID, name, age, gender, species, firstdate, neutralization, bf);
                Map<String, Object> childUpdates = new HashMap<>();
                childUpdates.put("/pet/" + userID + "/" + key, pet.toMap());
                databaseReference.updateChildren(childUpdates);

                if (flagImage == 1) {
                    // [START upload_memory]
                    profileImage.setDrawingCacheEnabled(true);
                    profileImage.buildDrawingCache();

                    Bitmap bitmap = ((BitmapDrawable) profileImage.getDrawable()).getBitmap();
                    ByteArrayOutputStream uploadStream = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, uploadStream);
                    byte[] bytes = uploadStream.toByteArray();

                    UploadTask uploadTask = mStorage.child("pet/" + userID + "/" + name + "/profile/profileImage").putBytes(bytes);
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

                    System.out.println("new pet added");
                    finish();
                }
            }
        });
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        if (requestCode==PICK_FROM_ALBUM) {
            Uri selectedImageUri = data.getData();
            // Set the image in ImageView
            profileImage.setImageURI(selectedImageUri);
            flagImage = 1;
        }
    }
}



