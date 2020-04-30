package com.codebasepk.notificationsender;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private EditText username;
    private EditText password;
    private Button login;
    private final String USER_NAME = "admin";
    private final String PASSWORD = "admin";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        username = findViewById(R.id.username);
        password = findViewById(R.id.password);
        login = findViewById(R.id.submit);
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = username.getText().toString();
                String pwd = password.getText().toString();
                if (name == null || name.trim().isEmpty()) {
                    Toast.makeText(MainActivity.this, "username should not be empty", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (pwd == null || pwd.trim().isEmpty()) {
                    Toast.makeText(MainActivity.this, "password should not be empty", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (name != null && !name.trim().isEmpty() && pwd != null && !pwd.trim().isEmpty()) {
                    if (!name.equals(USER_NAME)) {
                        Toast.makeText(MainActivity.this, "username is not correct", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (!pwd.equals(PASSWORD)) {
                        Toast.makeText(MainActivity.this, "password is not correct", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Log.i("TAG", " logged in");
                    // TODO: 4/22/20 Logged in here
                    Toast.makeText(MainActivity.this, "Success", Toast.LENGTH_SHORT).show();
                    new android.os.Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            startActivity(new Intent(getApplicationContext(), SendNotification.class));
                        }
                    }, 1000);
                }




            }
        });
    }
}
