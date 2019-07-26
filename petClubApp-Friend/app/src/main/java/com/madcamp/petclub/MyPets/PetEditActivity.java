package com.madcamp.petclub.MyPets;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.madcamp.petclub.MyPets.models.Pet;
import com.madcamp.petclub.R;
import com.madcamp.petclub.login.SharedPreference;
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

import static com.madcamp.petclub.MyPets.MyPetActivity.pet_data;

public class PetEditActivity extends AppCompatActivity {
    private static final int PICK_FROM_ALBUM = 1;
    int flagImage=0;

    StorageReference mStorage;
    private ImageView profileImage;

    public static EditText update_age, update_gender, update_bf, update_neutralization;
    private static TextView edit_name;
    String userID;
    private Button save_btn;
    private DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        int position = intent.getIntExtra("position", 0);
        String petName = intent.getStringExtra("petName");
        setContentView(R.layout.pet_full_edit);
        userID = SharedPreference.getAttribute(getApplicationContext(), "userID");

        mStorage = FirebaseStorage.getInstance().getReference();
        profileImage = (ImageView) findViewById(R.id.pet_profile);
        FirebaseStorage.getInstance().getReference("pet/" + userID + "/" + petName + "/profile").child("profileImage").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                // Data for "images/island.jpg" is returns, use this as needed
                Glide.with(getApplicationContext()).load(uri).into(profileImage);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle any errors
                Glide.with(getApplicationContext()).load(R.drawable.ic_pet_cafe).into(profileImage);
            }
        });
        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
                startActivityForResult(intent, PICK_FROM_ALBUM);
            }
        });

        update_age = (EditText) findViewById(R.id.edit_age);
        update_gender = (EditText) findViewById(R.id.edit_gender);
        update_bf = (EditText) findViewById(R.id.edit_bf);
        update_neutralization = (EditText) findViewById(R.id.edit_neutralization);
        edit_name = (TextView) findViewById(R.id.edit_name);
        update_age.setText(pet_data.get(position).getPetAge());
        update_gender.setText(pet_data.get(position).getPetGender());
        update_bf.setText(pet_data.get(position).getPetBff());
        update_neutralization.setText(pet_data.get(position).getPetNeutralization());

        String name = pet_data.get(position).getPetName();
        String first_date = pet_data.get(position).getPetFirstDate();
        String species = pet_data.get(position).getPetSpecies();

        edit_name.setText(name);

        final String revised_age = update_age.getText().toString();
        final String revised_gender = update_gender.getText().toString();
        final String revised_bf = update_bf.getText().toString();
        final String revised_neutralization = update_neutralization.getText().toString();

        save_btn = (Button) findViewById(R.id.edit_save);
        save_btn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Pet pet = new Pet(userID, name, revised_age, revised_gender, species, first_date,
                        revised_neutralization, revised_bf);
                Map<String, Object> childUpdates = new HashMap<>();
                childUpdates.put("/pet/"+userID+"/"+name, pet.toMap());
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
                System.out.println("done revising");
                finish();
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