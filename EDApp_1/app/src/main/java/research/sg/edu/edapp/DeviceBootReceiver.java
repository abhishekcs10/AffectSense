package research.sg.edu.edapp;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.Toast;

public class DeviceBootReceiver extends BroadcastReceiver {
    public DeviceBootReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {

            Toast.makeText(context, "AffectSense Restarted..", Toast.LENGTH_SHORT).show();
            RestoreServices(context);
        }
    }

    public void RestoreServices(Context context){

        Intent intent = new Intent(context,MasterService.class);
        context.startService(intent);
        StoreRunningStatus(context,true);
    }

    public void StoreRunningStatus(Context context,boolean flag){

        SharedPreferences pref = context.getSharedPreferences(context.getResources().getString(R.string.mood_sharedpref_file), Context.MODE_MULTI_PROCESS);
        SharedPreferences.Editor run_editor =pref.edit();
        run_editor.putBoolean(context.getResources().getString(R.string.sharedpref_running_status), flag);
        run_editor.apply();
        run_editor.commit();
    }

}
