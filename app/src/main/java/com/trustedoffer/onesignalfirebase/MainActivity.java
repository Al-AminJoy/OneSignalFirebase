package com.trustedoffer.onesignalfirebase;

import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCanceledListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.onesignal.OneSignal;
import com.squareup.picasso.Picasso;
//import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;


public class MainActivity extends AppCompatActivity {
    private String title, prc;
    private EditText etTitle, etPrice;
    private Button btSave, btSelectImage, btNext;
    private ProgressBar pbUploadImage;
    private Uri imageUri;
    private ImageView ivSelectedImage;
    private DatabaseReference databaseReference;
    private StorageReference storageReference;
    private RecyclerView recyclerView;
    private StorageTask storageTask;
    private static final int imageReq = 1;
    private FirebaseDatabase firebaseDatabase;
    private ArrayList<ProductModelClass> list = new ArrayList<>();
    private ProductAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //This Email is Used For Storage
        //firebasestorage111@gmail.com
        findId();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
        //FirebaseReference
        databaseReference = FirebaseDatabase.getInstance().getReference("product");
        storageReference = FirebaseStorage.getInstance().getReference("product");
        firebaseDatabase = FirebaseDatabase.getInstance();

        btSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveData();
            }
        });

        btSelectImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImage();
            }
        });
        // OneSignal Initialization
        OneSignal.startInit(this)
                .inFocusDisplaying(OneSignal.OSInFocusDisplayOption.Notification)
                .unsubscribeWhenNotificationsAreDisabled(true)
                .init();
        //Deep Linking Code
        Intent intent = getIntent();
        Uri data = intent.getData();
        if (data != null && intent != null) {
            Toast.makeText(getApplicationContext(), "Data Is : " + data.toString(), Toast.LENGTH_LONG).show();
        }

    }

    //For Set Image In Image View
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == imageReq && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            Picasso.with(this)
                    .load(imageUri)
                    .into(ivSelectedImage);
            ivSelectedImage.setVisibility(View.VISIBLE);
        }

    }

    //For Selecting Image
    private void selectImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, imageReq);
    }

    private void saveData() {

        if (storageTask != null && storageTask.isInProgress()) {
            Toast.makeText(getApplicationContext(), "File Is In Upload", Toast.LENGTH_SHORT).show();

        } else {
            title = etTitle.getText().toString().trim();
            prc = etPrice.getText().toString().trim();
            int price = Integer.parseInt(prc);
            uploadData(price);


        }
    }


    private void loadData() {
        databaseReference.keepSynced(true);
        list.clear();
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                    ProductModelClass product = dataSnapshot1.getValue(ProductModelClass.class);
                    //product.setKey(dataSnapshot1.getKey());
                    list.add(product);
                }
                Collections.reverse(list);
                adapter = new ProductAdapter(getApplicationContext(), list);
                recyclerView.setAdapter(adapter);
                adapter.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void findId() {
        etTitle = findViewById(R.id.etTitle);
        etPrice = findViewById(R.id.etPrice);
        btSave = findViewById(R.id.btSave);
        recyclerView = findViewById(R.id.rvProductList);
        btSelectImage = findViewById(R.id.btSelectImgId);
        pbUploadImage = findViewById(R.id.progressId);
        ivSelectedImage = findViewById(R.id.ivSelectedImage);

    }

    //Getting Image Extension
    public String getFileExtension(Uri imageUri) {
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(imageUri));
    }

    @Override
    protected void onStart() {
        loadData();
        super.onStart();
    }

    /*  @Override
      protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
          super.onActivityResult(requestCode, resultCode, data);
          if (requestCode==imageReq && resultCode==RESULT_OK && data!=null && data.getData()!=null){
              imageUri=data.getData();
              Picasso.with(this)
                      .load(imageUri)
                      .into(ivSelectedImage);
              ivSelectedImage.setVisibility(View.VISIBLE);
          }
      }*/
    private void uploadData(final int price) {
        //Storagereference is for store image in Firebase Storage
        StorageReference reference = storageReference.child(System.currentTimeMillis() + "." + getFileExtension(imageUri));
        reference.putFile(imageUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // Get a URL to the uploaded content
                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                        while (!uriTask.isSuccessful()) ;
                        Uri downloadUri = uriTask.getResult();

                        Toast.makeText(getApplicationContext(), "Image Inserted", Toast.LENGTH_SHORT).show();
                        //Getting Realtime Database Key
                        String key = databaseReference.push().getKey();
                        //Adding Data In Model Class For Upload In Realtime Database
                        ProductModelClass productModelClass = new ProductModelClass(key, title, price, downloadUri.toString());
                        //Adding  Data In Realtime Database
                        databaseReference.child(key).setValue(productModelClass);
                        loadData();


                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Handle unsuccessful uploads
                        Toast.makeText(getApplicationContext(), "Image Not Inserted", Toast.LENGTH_SHORT).show();

                    }
                });
    }

}
