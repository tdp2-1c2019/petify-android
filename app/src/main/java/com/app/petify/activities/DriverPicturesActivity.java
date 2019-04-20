package com.app.petify.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.app.petify.R;
import com.app.petify.utils.DownloadImageTask;
import com.facebook.AccessToken;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;

public class DriverPicturesActivity extends AppCompatActivity {
    private FirebaseStorage storage;
    private StorageReference storageReference;
    private String driverFbId;
    private String baseRef;
    private Uri localFilePath;

    private ImageView mRegistroImage;
    private Button mRegistroSubir;
    private boolean registroCargado;
    private ImageView mSeguroImage;
    private Button mSeguroSubir;
    private boolean seguroCargado;
    private ImageView mAutoImage;
    private Button mAutoSubir;
    private boolean autoCargado;
    private Button mTerminarCarga;

    Snackbar snackbar;

    private final int REGISTRO_REQUEST = 71;
    private final int SEGURO_REQUEST = 72;
    private final int AUTO_REQUEST = 73;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_pictures);

        mRegistroImage = findViewById(R.id.registroImage);
        mRegistroSubir = findViewById(R.id.subirFotoRegistro);
        mSeguroImage = findViewById(R.id.seguroImage);
        mSeguroSubir = findViewById(R.id.subirFotoSeguro);
        mAutoImage = findViewById(R.id.autoImage);
        mAutoSubir = findViewById(R.id.subirFotoAuto);

        registroCargado = false;
        seguroCargado = false;
        autoCargado = false;
        mTerminarCarga = findViewById(R.id.terminarCarga);
        mTerminarCarga.setVisibility(View.GONE);
        mTerminarCarga.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getBaseContext(), DriverHomeActivity.class);
                startActivity(i);
            }
        });

        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        driverFbId = accessToken.getUserId();
        baseRef = "drivers/" + driverFbId;

        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference(baseRef);
        // Si alguna imagen ya esta en su perfil, la traemos
        storageReference.child("registro").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                new DownloadImageTask(mRegistroImage).execute(uri.toString());
                registroCargado = true;
                permitirAvanzar();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                e.printStackTrace();
            }
        });
        storageReference.child("seguro").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                new DownloadImageTask(mSeguroImage).execute(uri.toString());
                seguroCargado = true;
                permitirAvanzar();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                e.printStackTrace();
            }
        });
        storageReference.child("auto").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                new DownloadImageTask(mAutoImage).execute(uri.toString());
                autoCargado = true;
                permitirAvanzar();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                e.printStackTrace();
            }
        });

        mRegistroSubir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseImage(REGISTRO_REQUEST);
            }
        });
        mSeguroSubir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseImage(SEGURO_REQUEST);
            }
        });
        mAutoSubir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseImage(AUTO_REQUEST);
            }
        });
    }

    private void chooseImage(int request_type) {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Seleccione Imagen"), request_type);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null && data.getData() != null) {
            localFilePath = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), localFilePath);
                switch (requestCode) {
                    case REGISTRO_REQUEST:
                        mRegistroImage.setImageBitmap(bitmap);
                        uploadImage(REGISTRO_REQUEST);
                        break;
                    case SEGURO_REQUEST:
                        mSeguroImage.setImageBitmap(bitmap);
                        uploadImage(SEGURO_REQUEST);
                        break;
                    case AUTO_REQUEST:
                        mAutoImage.setImageBitmap(bitmap);
                        uploadImage(AUTO_REQUEST);
                        break;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void uploadImage(final int request_type) {
        if (localFilePath != null) {
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Subiendo...");
            progressDialog.show();

            StorageReference ref = storageReference;
            switch (request_type) {
                case REGISTRO_REQUEST:
                    ref = ref.child("registro");
                    break;
                case SEGURO_REQUEST:
                    ref = ref.child("seguro");
                    break;
                case AUTO_REQUEST:
                    ref = ref.child("auto");
                    break;
            }
            ref.putFile(localFilePath)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {

                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            progressDialog.dismiss();
                            snackbar = Snackbar.make(findViewById(R.id.driver_profile_layout), "Imagen cargada", Snackbar.LENGTH_SHORT);
                            switch (request_type) {
                                case REGISTRO_REQUEST:
                                    registroCargado = true;
                                    break;
                                case SEGURO_REQUEST:
                                    seguroCargado = true;
                                    break;
                                case AUTO_REQUEST:
                                    autoCargado = true;
                                    break;
                            }
                            permitirAvanzar();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            snackbar = Snackbar.make(findViewById(R.id.driver_profile_layout), "Error: " + e.getMessage(), Snackbar.LENGTH_SHORT);
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot
                                    .getTotalByteCount());
                            progressDialog.setMessage("Progreso: " + (int) progress + "%");
                        }
                    });
        }
    }

    private void permitirAvanzar() {
        if (registroCargado && seguroCargado && autoCargado) {
            mTerminarCarga.setVisibility(View.VISIBLE);
        }
    }
}
