package research.sg.edu.edapp;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

public class AlarmReceiver extends BroadcastReceiver {
    public AlarmReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        StartMasterService(context);
    }

    public void StartMasterService(Context context) {

        Intent intent = new Intent(context,MasterService.class);
        context.startService(intent);
    }
}
