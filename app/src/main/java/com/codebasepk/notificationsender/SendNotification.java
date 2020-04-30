package com.codebasepk.notificationsender;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.UUID;

import pk.codebase.requests.HttpHeaders;
import pk.codebase.requests.HttpRequest;
import pk.codebase.requests.HttpResponse;

public class SendNotification extends AppCompatActivity {

    private EditText title;
    private EditText description;
    private Button selectFile;
    private Button send;
    private final String TOPIC = "studentss";
    FirebaseStorage storage;
    StorageReference storageReference;
    private Uri filePath;
    protected final String SERVER_KEY = "AAAA1eomFzk:APA91bEI8LkSx4KMmTxh2xrBcIiPM78RLuUWCx91" +
            "qKgAbe8-puzLcRPZppspHaH2Ruonk2cG0YWJjYbNu7RAMJLhWHDwR8NTLa-j9L9h-HBc9pwa4RnmGLgOQjd" +
            "_Yj7gCpxbkRJ7sekM";


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.send_notification);
        title = findViewById(R.id.title);
        description = findViewById(R.id.description);
        selectFile = findViewById(R.id.select_file);
        send = findViewById(R.id.send);
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String titleText = title.getText().toString();
                String descriptionText = description.getText().toString();
                if (titleText == null || titleText.trim().isEmpty()) {
                    Toast.makeText(SendNotification.this, "please enter title",
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                if (descriptionText == null || descriptionText.trim().isEmpty()) {
                    Toast.makeText(SendNotification.this, "please enter description",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                if (filePath == null) {
                    Toast.makeText(SendNotification.this, "Please Select File",
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                if (titleText != null && !titleText.trim().isEmpty() && descriptionText != null &&
                        !descriptionText.trim().isEmpty()) {
                    uploadImage(titleText, descriptionText);
                }
            }
        });
        selectFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (hasWritePermissions()) {
                    filePath = null;
                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.setType("*/*");
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    try {
                        startActivityForResult(
                                Intent.createChooser(intent, "Select a File to Upload"),
                                123);
                    } catch (android.content.ActivityNotFoundException ex) {
                        // Potentially direct the user to the Market with a Dialog
                        Toast.makeText(SendNotification.this, "Please install a File Manager.",
                                Toast.LENGTH_SHORT).show();
                    }
                } else {
                    requestAppPermissions();
                }
            }
        });
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
    }

    private void sendNotification(String title, String description, String file) {
        HttpRequest request = new HttpRequest();
        request.setOnResponseListener(response -> {
            System.out.println("RESPONSE....................." + response.text);
            switch (response.code) {
                case HttpResponse.HTTP_OK:
                    System.out.println("RESPONSE....................." + response.text);
                    // if response is notification status update as deleted on database
                    // remove it from local database as well

                    break;
                case HttpResponse.HTTP_GATEWAY_TIMEOUT:
                    break;
            }
        });
        request.setOnErrorListener(error -> {
            // There was an error, deal with it
        });
        try {
            // building JSON Object to send data to the server
            JSONObject req = new JSONObject();
            req.put("title", title);
            req.put("description", description);
            req.put("file", file);
            JSONObject root = new JSONObject();
            root.put("data", req);
            root.put("to", "/topics/" + TOPIC);
            root.put("priority", "high");
            Log.i("TAG", "data " + root);
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.put("Authorization", "key=" + SERVER_KEY);
            request.post("https://fcm.googleapis.com/fcm/send", root, httpHeaders);
        } catch (JSONException ignore) {
        }
    }

    private void requestAppPermissions() {
        if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return;
        }

        if (hasReadPermissions() && hasWritePermissions()) {
            return;
        }

        ActivityCompat.requestPermissions(this,
                new String[]{
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                }, 1001); // your request code
    }

    private boolean hasReadPermissions() {
        return (ContextCompat.checkSelfPermission(getBaseContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
    }

    private boolean hasWritePermissions() {
        return (ContextCompat.checkSelfPermission(getBaseContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
    }

    public static String getMimeType(Context context, Uri uri) {
        String extension;
        //Check uri format to avoid null
        if (uri.getScheme().equals(ContentResolver.SCHEME_CONTENT)) {
            //If scheme is a content
            final MimeTypeMap mime = MimeTypeMap.getSingleton();
            extension = mime.getExtensionFromMimeType(context.getContentResolver().getType(uri));
        } else {
            //If scheme is a File
            //This will replace white spaces with %20 and also other special characters. This will avoid returning null values on file name with spaces and special characters.
            extension = MimeTypeMap.getFileExtensionFromUrl(Uri.fromFile(new File(uri.getPath())).toString());

        }

        return extension;
    }

    public String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                cursor.close();
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    private void uploadImage(String title, String description) {
        Log.i("TAG", " mime type " + getMimeType(getApplicationContext(), filePath));
        if (filePath != null) {
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Uploading...");
            progressDialog.show();
            StorageReference ref = storageReference.child("files/" + getFileName(filePath));
            Log.i("TAG", ref.getPath());
            ref.putFile((filePath))
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Log.i("TAG", taskSnapshot.getMetadata().getPath());
                            StorageReference dateRef = storageReference.child(taskSnapshot.getMetadata().getPath());
                            dateRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>()
                            {
                                @Override
                                public void onSuccess(Uri downloadUrl) {
                                    //do something with downloadurl
                                    Log.i("TAG", "onSuccess: " + downloadUrl);
                                    sendNotification(title, description, downloadUrl.toString());
                                    progressDialog.dismiss();
                                    Toast.makeText(SendNotification.this, "Notification Sent",
                                            Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(SendNotification.this, "Failed " +
                                    e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
//                            double progress = (100.0 * taskSnapshot.getBytesTransferred() /
//                                    taskSnapshot.getTotalByteCount());
//                            progressDialog.setMessage("Uploaded " + (int) progress + "%");
                        }
                    });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 123 && resultCode == RESULT_OK
                && data != null && data.getData() != null) {
            filePath = data.getData();
            Log.i("TAG", " file " + filePath);
        }
    }
}
