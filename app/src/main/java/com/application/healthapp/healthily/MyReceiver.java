package com.application.healthapp.healthily;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class MyReceiver extends BroadcastReceiver {
    public MyReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        Log.d("TAG","I am here");
        Intent intent1 = new Intent(context, MyNewIntentService.class);
        context.startService(intent1);
    }
}