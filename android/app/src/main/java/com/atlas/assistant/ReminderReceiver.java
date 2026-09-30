package com.atlas.assistant;

import android.app.*;
import android.content.*;
import android.os.Build;

public class ReminderReceiver extends BroadcastReceiver {
  @Override public void onReceive(Context context, Intent intent) {
    String title=intent.getStringExtra("title"); String body=intent.getStringExtra("body");
    NotificationManager nm=(NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
    Notification.Builder b=Build.VERSION.SDK_INT>=26?new Notification.Builder(context,"atlas_reminders"):new Notification.Builder(context);
    b.setSmallIcon(android.R.drawable.ic_dialog_info).setContentTitle(title==null?"ATLAS":title).setContentText(body==null?"Lembrete":body).setAutoCancel(true);
    nm.notify((int)(System.currentTimeMillis()%1000000000),b.build());
  }
}
