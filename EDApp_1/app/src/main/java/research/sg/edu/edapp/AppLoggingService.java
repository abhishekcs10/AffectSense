package research.sg.edu.edapp;

import android.app.ActivityManager;
import android.app.KeyguardManager;
import android.app.Service;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class AppLoggingService extends Service {

    //private static String old_pkg="Dummy_Pkg";

    public AppLoggingService() {
    }

    public static class RecentUseComparator implements Comparator<UsageStats> {

        @Override
        public int compare(UsageStats lhs, UsageStats rhs) {
            return (lhs.getLastTimeUsed() > rhs.getLastTimeUsed()) ? -1 : (lhs.getLastTimeUsed() == rhs.getLastTimeUsed()) ? 0 : 1;
        }
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public synchronized void onDestroy() {
        super.onDestroy();
    }

    @Override
    public synchronized int onStartCommand(Intent intent,int flags, int startId) {

        LogAppName();
        return START_STICKY;
    }

    public void LogAppName() {

        String app_name;
        String last_app;

        app_name=getCurrentAppName();
        last_app=RetrieveLastApp();

        System.out.println("Current_app:" + app_name + ",Last App:" + last_app );

        if(!app_name.isEmpty()) {

            if(!last_app.equalsIgnoreCase(app_name)){

                StoreLastApp(app_name);
                WriteAppName(app_name);
            }
        }
    }

    public String getCurrentAppName(){
        String packagename;

        if(Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {

            KeyguardManager myKM = (KeyguardManager) getApplicationContext().getSystemService(Context.KEYGUARD_SERVICE);
            if( myKM.inKeyguardRestrictedInputMode()) {
                //it is locked

                //System.out.println("[AppLogger]Screen is locked");
                packagename = "LockScreen";
            }
            else {

                ActivityManager am = (ActivityManager) getApplicationContext().getSystemService(ACTIVITY_SERVICE);

                List<ActivityManager.RunningTaskInfo> taskInfo = am.getRunningTasks(1);
                ComponentName componentInfo = taskInfo.get(0).topActivity;

                packagename = componentInfo.getPackageName();
            }
        }
        else {

            KeyguardManager myKM = (KeyguardManager) getApplicationContext().getSystemService(Context.KEYGUARD_SERVICE);
            if( myKM.inKeyguardRestrictedInputMode()) {
                //it is locked

                packagename = "LockScreen";
            }

            else {
                packagename=getTopPackage();
            }
        }
        return packagename;
    }

    public String getTopPackage(){

        RecentUseComparator mRecentComp=new RecentUseComparator();
        //String NONE_PKG="Dummy_Pkg";

        long ts = System.currentTimeMillis();
        UsageStatsManager mUsageStatsManager = (UsageStatsManager)getSystemService("usagestats");
        List<UsageStats> usageStats = mUsageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_BEST, ts-1000*10, ts);
        if (usageStats == null || usageStats.size()== 0) {
            //return NONE_PKG;
            return RetrieveLastApp();
        }
        Collections.sort(usageStats, mRecentComp);
        //old_pkg=usageStats.get(0).getPackageName();
        System.out.println("[Current_app]:"+usageStats.get(0).getPackageName());
        return usageStats.get(0).getPackageName();
    }

    public String RetrieveLastApp(){

        String last_app="Dummy";

        try {

            Context con = getApplicationContext().createPackageContext(getResources().getString(R.string.applogger_pkg), Context.CONTEXT_IGNORE_SECURITY);
            SharedPreferences pref = con.getSharedPreferences(getResources().getString(R.string.applogger_sharedpref_file), Context.MODE_MULTI_PROCESS);

            last_app = pref.getString(getResources().getString(R.string.sharedpref_last_logged_app_name), "LastApp");
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return last_app;
    }

    public void StoreLastApp(String app_name){

        SharedPreferences pref = getApplicationContext().getSharedPreferences(getResources().getString(R.string.applogger_sharedpref_file), Context.MODE_MULTI_PROCESS);
        SharedPreferences.Editor log_editor =pref.edit();
        log_editor.putString(getResources().getString(R.string.sharedpref_last_logged_app_name), app_name);
        log_editor.apply();
        log_editor.commit();
    }

    public void WriteAppName(String string_t){

        String imei_no,app_log_file_name;
        String file_ctr;

        File sdCardRoot = Environment.getExternalStorageDirectory();
        File dataDir = new File(sdCardRoot, getResources().getString(R.string.data_file_path));

        if(!dataDir.exists()) {
            dataDir.mkdirs();
        }

        TelephonyManager telephonyManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
        imei_no = (String)telephonyManager.getDeviceId();

        file_ctr=RetrieveAppCtr();

        app_log_file_name = imei_no + "_" + file_ctr + getResources().getString(R.string.app_file_postfix);

        File app_file = new File(dataDir, app_log_file_name);

        try{
            SimpleDateFormat sdf = new SimpleDateFormat(getResources().getString(R.string.time_format));
            String currentDateandTime = sdf.format(new Date());

            string_t = string_t + "," + currentDateandTime + "\n";
            FileOutputStream fos = new FileOutputStream(app_file,true);
            fos.write(string_t.getBytes());
            fos.close();
        }catch(Exception e) {
            Log.d("EXCEPTION", e.getMessage());
        }

        int app_file_size = Integer.parseInt(String.valueOf(app_file.length() / 1024));
        int app_file_size_threshold = Integer.parseInt(getResources().getString(R.string.app_file_size_limit));
        //System.out.println("App Logger File Size:" + app_file_size + ", App File Threshold:" + app_file_size_threshold );

        if (app_file_size > app_file_size_threshold) {
            int ctr = Integer.parseInt(file_ctr) + 1;
            file_ctr=String.valueOf(ctr);
            file_ctr=String.format("%06d", Integer.parseInt(file_ctr));
            //String.format("%05d", Integer.parseInt(mood_ctr));
            move_file(app_log_file_name);
        }
        StoreAppCtr(file_ctr);
    }

    public String RetrieveAppCtr(){

        String ctr="000000";

        try {

            Context con = getApplicationContext().createPackageContext(getResources().getString(R.string.ctr_pkg), Context.CONTEXT_IGNORE_SECURITY);
            SharedPreferences pref = con.getSharedPreferences(getResources().getString(R.string.ctr_sharedpref_file), Context.MODE_MULTI_PROCESS);

            ctr = pref.getString(getResources().getString(R.string.app_ctr), "000000");
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return ctr;
    }

    public void StoreAppCtr(String ctr){

        SharedPreferences pref = getApplicationContext().getSharedPreferences(getResources().getString(R.string.ctr_sharedpref_file), Context.MODE_MULTI_PROCESS);
        SharedPreferences.Editor log_editor =pref.edit();
        log_editor.putString(getResources().getString(R.string.app_ctr), ctr);
        log_editor.apply();
        log_editor.commit();
    }

    public void move_file(String file_name){

        File sdCardRoot = Environment.getExternalStorageDirectory();

        File dataDir = new File(sdCardRoot, getResources().getString(R.string.data_file_path));
        File tobeuploadedDir = new File(sdCardRoot, getResources().getString(R.string.to_be_uploaded_file_path));

        File sourceLocation = new File(dataDir, file_name);
        File targetLocation = new File(tobeuploadedDir, file_name);

        try {
            InputStream in = new FileInputStream(sourceLocation);
            OutputStream out = new FileOutputStream(targetLocation);

            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            in.close();
            out.close();

            if(sourceLocation.exists()) {
                sourceLocation.delete();
            }
        }
        catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }
}
