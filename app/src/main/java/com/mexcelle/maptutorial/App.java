package com.mexcelle.maptutorial;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

import androidx.annotation.RequiresApi;

public class App extends Application {
    public  static  final  String CHANNEL="default-channel";

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onCreate() {
        super.onCreate();

        NotificationChannel notificationChannel=new NotificationChannel(
                CHANNEL,"LocationChannel", NotificationManager.IMPORTANCE_HIGH);

        NotificationManager manager= (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        manager.createNotificationChannel(notificationChannel);
    }
}
