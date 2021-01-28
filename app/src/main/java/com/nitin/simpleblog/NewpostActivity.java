package com.nitin.simpleblog;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import id.zelory.compressor.Compressor;

public class NewpostActivity extends AppCompatActivity {

    private static final int MAX_LENGTH =100 ;
    private Toolbar newPostToolbar;
    private ImageView newPostImage;
    private Button newPostBtn;
    private EditText newPostDesc;
    private Uri postImageUri=null;

    private StorageReference storageReference;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;
    private String current_user_id;
    private Bitmap compressedImageFile;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_newpost);

        newPostToolbar = findViewById(R.id.post_toolbar);
        setSupportActionBar(newPostToolbar);
        getSupportActionBar().setTitle("Add New Post");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        storageReference = FirebaseStorage.getInstance().getReference();
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();

        current_user_id = firebaseAuth.getCurrentUser().getUid();

        newPostBtn = findViewById(R.id.new_post_btn);
        newPostDesc = findViewById(R.id.new_post_desc);
        newPostImage = findViewById(R.id.new_post_image);

        newPostImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setMinCropResultSize(512, 512)
                        .setAspectRatio(1, 1)
                        .start(NewpostActivity.this);
            }
        });

        newPostBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final String desc = newPostDesc.getText().toString();

                if (!TextUtils.isEmpty(desc) && postImageUri != null) {
                    final String randomName = random();

                    final StorageReference filePath = storageReference.child("post_images").child(randomName + ".jpg");

                    filePath.putFile(postImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(final UploadTask.TaskSnapshot taskSnapshot) {
                            filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    final String downloadUri=uri.toString();




                            if (taskSnapshot.getTask().isSuccessful()) {

                                //This is how we get the image_file.
                                //we have the File class.
                                File new_image_file = new File(postImageUri.getPath());

                                try {
                                    //We then compress using this.
                                    compressedImageFile = new Compressor(NewpostActivity.this)
                                            .setMaxHeight(200).setMaxWidth(200).setQuality(10).compressToBitmap(new_image_file);

                                }
                                catch(Exception e) {
                                    e.printStackTrace();
                                }

                                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                compressedImageFile.compress(Bitmap.CompressFormat.JPEG,100,baos);
                                byte[] thumbData = baos.toByteArray();


                                //We store the thumbnail into the thumbs folder.
                                final UploadTask uploadTask = storageReference.child("post_images/thumbs").child(randomName+".jpg").putBytes(thumbData);
                                uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                        String thumbdownloadUri = taskSnapshot.getUploadSessionUri().toString();





                                        Map<String, Object> postMap = new HashMap<>();
                                        postMap.put("image_url", downloadUri);
                                        postMap.put("image_thumb",thumbdownloadUri);
                                        postMap.put("desc", desc);
                                        postMap.put("user_id", current_user_id);
                                        postMap.put("timestamp", FieldValue.serverTimestamp());
                                        firebaseFirestore.collection("Posts").add(postMap).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                            @Override
                                            public void onComplete(@NonNull Task<DocumentReference> task) {

                                                if (task.isSuccessful()) {
                                                    Toast.makeText(NewpostActivity.this,"Post was added",Toast.LENGTH_SHORT).show();
                                                    Intent intent = new Intent(NewpostActivity.this,MainActivity.class);
                                                    startActivity(intent);
                                                    finish(); //This tells that the user should not be able to go back again.



                                                } else {
                                                    String error = task.getException().getMessage();
                                                    Toast.makeText(NewpostActivity.this, "Firestore Error: " + error, Toast.LENGTH_SHORT).show();
                                                }

                                            }
                                        });

                                        //If the task is successfull, we will do the stuff we want to do. If the task is not successfull
                                        //We will do our rest work.
                                        //This will upload the thumbnail as well.
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        //If the task is not successfull, we will do.
                                        Toast.makeText(NewpostActivity.this,"Error:",Toast.LENGTH_SHORT).show();
                                    }
                                });


                            } else {

                                String error = taskSnapshot.getError().getMessage();
                                Toast.makeText(NewpostActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                        }
                    });

                }
            }
        });
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {

                postImageUri=result.getUri();
                newPostImage.setImageURI(postImageUri);

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }


    public static String random() {
        Random generator = new Random();
        StringBuilder randomStringBuilder = new StringBuilder();
        int randomLength = generator.nextInt(MAX_LENGTH);
        char tempChar;
        for (int i = 0; i < randomLength; i++){
            tempChar = (char) (generator.nextInt(96) + 32);
            randomStringBuilder.append(tempChar);
        }
        return randomStringBuilder.toString();
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent intent = new Intent(NewpostActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}