package com.example.calendarapplication;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import com.example.calendarapplication.taskDB.TaskDB;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    @Override
    public void onNewToken(String token) {
        Log.d("FCM Log", "Refreshed token: " + token);
    }
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        if (remoteMessage.getNotification() != null) {                      //포어그라운드
            sendNotification(remoteMessage.getNotification().getBody(), remoteMessage.getNotification().getTitle());
        }else if (remoteMessage.getData().size() > 0) {                           //백그라운드
            sendNotification(remoteMessage.getData().get("body"), remoteMessage.getData().get("title"));
            /* 백그라운드 작동 내용 */
        }
    }

    private void sendNotification(String messageBody, String messageTitle)  {
        Log.d("FCM Log", messageTitle + ": " + messageBody);

        final TaskDB taskDB = TaskDB.getInstance(getApplicationContext());
        final ArrayList<Task> taskArrayList = (ArrayList<Task>) taskDB.taskDao().getAll();

        int year = Integer.parseInt(dateFormat("yyyy"));
        int month = Integer.parseInt(dateFormat("MM"));
        int day = Integer.parseInt(dateFormat("dd"));

        boolean isTodayTaskExist = false;
        for(int i = 0; i < taskArrayList.size(); i++){
            Task task = taskArrayList.get(i);

            if(Integer.parseInt(task.getDeadline()) - Integer.parseInt(task.getEstimatedDay()) <= 0){
                isTodayTaskExist = true;
                break;
            }

            if(year == Integer.parseInt(task.getYear()) && month == Integer.parseInt(task.getMonth()) &&
                        day == Integer.parseInt(task.getDay())) {
                isTodayTaskExist = true;
                break;
            }
        }

        if(!isTodayTaskExist)
            return;

        /* 알림의 탭 작업 설정 */
        Intent intent = new Intent(this, MainActivity.class);

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        String channelId = "Channel ID";
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        /* 알림 만들기 */
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, channelId)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle(messageTitle)
                        .setContentText(messageBody)
                        .setAutoCancel(true)
                        .setFullScreenIntent(pendingIntent, true);
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        /* 채널 만들기*/
        /* Android 8.0 이상에서 알림을 게시하려면 알림을 만들어야 함 */
        String channelName = "Channel Name";
        NotificationChannel channel = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH);
        notificationManager.createNotificationChannel(channel);

        notificationManager.notify(0, notificationBuilder.build());
    }

    public String dateFormat(String pattern) {
        long now = System.currentTimeMillis();
        Date date = new Date(now);
        return new SimpleDateFormat(pattern).format(date);
    }
}