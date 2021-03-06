package com.example.jgraham.kitabureg1;

/*

 .----------------.  .----------------.  .----------------.  .----------------.  .----------------.  .----------------.
| .--------------. || .--------------. || .--------------. || .--------------. || .--------------. || .--------------. |
| |  ___  ____   | || |     _____    | || |  _________   | || |      __      | || |   ______     | || | _____  _____ | |
| | |_  ||_  _|  | || |    |_   _|   | || | |  _   _  |  | || |     /  \     | || |  |_   _ \    | || ||_   _||_   _|| |
| |   | |_/ /    | || |      | |     | || | |_/ | | \_|  | || |    / /\ \    | || |    | |_) |   | || |  | |    | |  | |
| |   |  __'.    | || |      | |     | || |     | |      | || |   / ____ \   | || |    |  __'.   | || |  | '    ' |  | |
| |  _| |  \ \_  | || |     _| |_    | || |    _| |_     | || | _/ /    \ \_ | || |   _| |__) |  | || |   \ `--' /   | |
| | |____||____| | || |    |_____|   | || |   |_____|    | || ||____|  |____|| || |  |_______/   | || |    `.__.'    | |
| |              | || |              | || |              | || |              | || |              | || |              | |
| '--------------' || '--------------' || '--------------' || '--------------' || '--------------' || '--------------' |
 '----------------'  '----------------'  '----------------'  '----------------'  '----------------'  '----------------'

 */

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.example.jgraham.kitabureg1.database.KitabuEntry;
import com.example.jgraham.kitabureg1.database.MySQLiteDbHelper;
import com.google.android.gms.gcm.GoogleCloudMessaging;

/*
    * Defined a class to receive notifications.
    * and updating the database accordingly
    * after receiving the notifications.
    *
 */
public class GcmIntentService extends IntentService {
    IBinder mBinder;

    public GcmIntentService() {
        super("GcmIntentService");
    }

    public class GcmBind extends Binder
    {
        // Get the service
        GcmIntentService getService() {
            return GcmIntentService.this;
        }

    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);

        mBinder = new GcmBind();

        String messageType = gcm.getMessageType(intent);

        Log.d("LOG", extras.toString());
        if (extras != null && !extras.isEmpty()) {
            if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) {
                Log.d("LOG", extras.toString());
                MySQLiteDbHelper dbHelper = MySQLiteDbHelper.getInstance(getApplicationContext());

                if (extras.get("del") != null) {
                    int delVal = Integer.parseInt(extras.getString("del"));
                    KitabuEntry entry = dbHelper.fetchEntryByIndex(delVal);
                    dbHelper.removeEntry(delVal);
                    Tab1.values.remove(entry);
                } else if (extras.get("save") != null) {

//                    JSONObject object = extras.getParcelable("save");
  //                  Log.d("Log", object.toString());
    //                KitabuEntry kitabuEntry = new KitabuEntry(object);
      //              dbHelper.insertEntry(kitabuEntry);
                }

                /*
                 * We are sending all of these details via GCM.
                 * I hope this works.... :/
                 */
                else if (extras.getString("url") != null) {
                    KitabuEntry entry = new KitabuEntry(extras.getString("id"),
                            extras.getString("url"),
                            extras.getString("phoneno"),
                            extras.getString("tags"),
                            2,
                            extras.getString("title"));
                    try {
                        KitabuEntry entry1 = dbHelper.fetchEntryByIndex(entry.getmId());
                        if (entry1 == null) {
                            dbHelper.insertEntry(entry);
                        } else {
                            Log.d("DB: ", "UPDATING");
                            dbHelper.removeEntry(entry.getmId());
                            entry.setmType(2);
                            dbHelper.insertEntry(entry);
                            // dbHelper.updateEntry(entry.getmId());
                        }
                    } catch (Exception e) {
                        Log.d("GCM: ", "Received notification, but didn't push");
                    }
                    Intent m_intent = new Intent(this, MainActivity.class);
                    m_intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

                    PendingIntent contentIntent = PendingIntent.getActivity(this, 0, m_intent, PendingIntent.FLAG_CANCEL_CURRENT);
                    NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    NotificationCompat.Builder mBuilder = (NotificationCompat.Builder) new NotificationCompat.Builder(this)
                            .setSmallIcon(R.drawable.kicondroplet).setContentTitle(getString(R.string.app_name))
                            .setStyle(new NotificationCompat.BigTextStyle()
                                    .bigText(extras.getString("phoneno") + " suggested " + extras.getString("url")))
                            .setContentText(extras.getString("phoneno") + " suggested " + extras.getString("url"))
                            .setAutoCancel(true).setSound(Settings.System.DEFAULT_NOTIFICATION_URI);
                    mBuilder.setContentIntent(contentIntent);
                    nm.notify(0, mBuilder.build());
                }
            }
            // Wakeup the app after notification is received.
            GcmBroadcastReceiver.completeWakefulIntent(intent);
        }
    }
}
