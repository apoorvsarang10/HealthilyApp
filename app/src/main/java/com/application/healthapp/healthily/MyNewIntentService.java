package com.application.healthapp.healthily;

import android.annotation.TargetApi;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class MyNewIntentService extends IntentService {

    private FirebaseFirestore firebaseFirestore;
    public String title, body;

    private static final int NOTIFICATION_ID = 3;

    public MyNewIntentService() {
        super("MyNewIntentService");
    }

    @TargetApi(Build.VERSION_CODES.O)
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected void onHandleIntent(Intent intent) {
//        FirebaseApp.initializeApp(this);

        firebaseFirestore = FirebaseFirestore.getInstance();
        Date currentDate = Calendar.getInstance().getTime();
        Log.d("TAG", currentDate.toString());
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        String date;
        try {
            date = df.format(currentDate);
        } catch (Exception e) {
            e.printStackTrace();
            date = "";
        }
        Log.d("TAG", "I am in here");
        Log.d("TAG", date);
        final Context context = this;

        firebaseFirestore.collection("Notifications").document(date).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isComplete()) {
                    DocumentSnapshot documentSnapshot = task.getResult();
                    if (documentSnapshot != null && documentSnapshot.exists()) {
                        Log.d("TAG", documentSnapshot.getString("title")); //Print the title
                        Log.d("TAG", documentSnapshot.getString("body"));  //Print the body
                        title = documentSnapshot.getString("title");
                        body = documentSnapshot.getString("body");
                    } else {
                        Log.d("TAG", "Data Not Found"); //Print the title
                        title = "No notification";
                        body = "No notification for today";
                    }
                } else {
                    Log.d("TAG", "Task incomplete"); //Print the title
                    title = "No notification";
                    body = "No notification for today";
                }
                Notification.Builder builder;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    CharSequence name = "Channel Name";
                    String description = "Channel Desc";
                    int importance = NotificationManager.IMPORTANCE_DEFAULT;
                    NotificationChannel channel = new NotificationChannel("3000", name, importance);
                    channel.setDescription(description);
                    // Register the channel with the system; you can't change the importance
                    // or other notification behaviors after this
                    NotificationManager notificationManager = getSystemService(NotificationManager.class);
                    notificationManager.createNotificationChannel(channel);

                    builder = new Notification.Builder(context, "3000");
                } else {

                    builder = new Notification.Builder(context);
                }
                Log.d("TAG", title);
                Log.d("TAG", body);
                builder.setContentTitle(title);
                builder.setContentText(body);
                builder.setSmallIcon(R.drawable.ic_launcher_background);
                builder.setAutoCancel(true);
                Intent notifyIntent = new Intent(context, MainActivity.class);
                PendingIntent pendingIntent = PendingIntent.getActivity(context, 2, notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                //to be able to launch your activity from the notification
                builder.setContentIntent(pendingIntent);
                Notification notificationCompat = builder.build();
                NotificationManagerCompat managerCompat = NotificationManagerCompat.from(context);
                managerCompat.notify(NOTIFICATION_ID, notificationCompat);
            }
        });

    }
}
