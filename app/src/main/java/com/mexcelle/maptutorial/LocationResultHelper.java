package com.mexcelle.maptutorial;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Build;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;

public class LocationResultHelper {
    private Context context;
    private List<Location> locations;

    public LocationResultHelper(Context context, List<Location> locations) {
        this.context = context;
        this.locations = locations;
    }

    public  String getLocationText()
    {

        if (locations.isEmpty())
        {
            return "Location not received";
        }
        else
            {
                StringBuilder sb=new StringBuilder();
                for (Location location:locations)
                {
                    sb.append("(");
                    sb.append(location.getLatitude());
                    sb.append(",");
                    sb.append(location.getLongitude());
                    sb.append(")");
                    sb.append("\n");
                }
                return  sb.toString();
            }

    }

    private CharSequence getLocationResultTitle()
    {
        String result=context.getResources().getQuantityString(R.plurals.num_locations_reported,locations.size(),locations.size());
        return String.format(result + ":" + DateFormat.getDateTimeInstance().format(new Date()));
    }
    public  void  showNotification()
    {
        Intent notificationIntent=new Intent(context,MainActivity.class);
        TaskStackBuilder stackBuilder=TaskStackBuilder.create(context);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(notificationIntent);

        PendingIntent notificationPendingIntent=
                stackBuilder.getPendingIntent(0,PendingIntent.FLAG_UPDATE_CURRENT);

        Notification.Builder notificationBuilder=null;
        if (Build.VERSION.SDK_INT>=android.os.Build.VERSION_CODES.O)
        {
            notificationBuilder=new Notification.Builder(context,App.CHANNEL)
                    .setContentTitle(getLocationResultTitle())
                    .setContentText(getLocationText())
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setAutoCancel(true)
                    .setContentIntent(notificationPendingIntent);
        }
        genotificationManager().notify(0,notificationBuilder.build());
    }

    private NotificationManager genotificationManager()
    {
        NotificationManager manager= (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        return  manager;
    }


}
