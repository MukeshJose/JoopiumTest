package com.example.joopiumtest;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.internal.FirebaseInstanceIdInternal;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MainActivity extends AppCompatActivity {

    EditText mIdMessageBox;
    Button mIdSendButton;
    Button mIdRingButton;
    Button mIdStopButton;
    TextView mIdMessageDisplayBox;
    public static String message;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        mIdMessageBox = findViewById(R.id.id_message_box);
        mIdSendButton = findViewById(R.id.id_send_button);
        mIdRingButton = findViewById(R.id.id_ring_button);
        mIdStopButton = findViewById(R.id.id_stop_button);
        mIdMessageDisplayBox = findViewById(R.id.id_message_display_box);

        NotificationChannel channel = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            channel = new NotificationChannel("Notification", "Notification", NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }

        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference dataRef = firebaseDatabase.getReference("message");


        mIdSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mIdMessageDisplayBox.setVisibility(View.INVISIBLE);
                message = mIdMessageBox.getText().toString();
                dataRef.setValue(message);
                Toast.makeText(getApplicationContext(), "SUCCESS", Toast.LENGTH_SHORT).show();

            }
        });

        mIdRingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startService(new Intent(MainActivity.this, NewService.class));
                mIdRingButton.setVisibility(View.INVISIBLE);
                mIdStopButton.setVisibility(View.VISIBLE);
                mIdSendButton.setActivated(false);
                dataRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        // This method is called once with the initial value and again
                        // whenever data at this location is updated.
                        String value = dataSnapshot.getValue(String.class);
                        mIdMessageDisplayBox.setVisibility(View.VISIBLE);
                        mIdMessageDisplayBox.setText(value);

                        Intent newIntent = new Intent(MainActivity.this, NotificationsActivity.class);
                        newIntent.setAction(Intent.ACTION_POWER_CONNECTED);

                        PendingIntent pendingIntent = PendingIntent.getBroadcast(MainActivity.this, 0, newIntent, PendingIntent.FLAG_MUTABLE);
                        NotificationCompat.Builder newBuilder = new NotificationCompat.Builder(MainActivity.this, "Notification");
                        newBuilder.setSmallIcon(R.drawable.ic_launcher_background);
                        newBuilder.setContentTitle("New Notification");
                        newBuilder.setContentText(value);
                        newBuilder.setPriority(NotificationCompat.PRIORITY_DEFAULT);
                        newBuilder.setContentIntent(pendingIntent);
                        NotificationManager newManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                        newManager.notify(0, newBuilder.build());
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        Toast.makeText(MainActivity.this, "Some error occurred", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
        mIdStopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopService(new Intent(MainActivity.this, NewService.class));
                mIdRingButton.setVisibility(View.VISIBLE);
                mIdStopButton.setVisibility(View.GONE);
                mIdSendButton.setActivated(true);
                mIdMessageDisplayBox.setVisibility(View.INVISIBLE);
            }
        });
    }
}