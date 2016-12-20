package research.sg.edu.edapp;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.KeyguardManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.ComponentName;
import android.content.ContentProviderClient;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.os.PowerManager;
import android.provider.SyncStateContract;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class MasterService extends Service {

    static long old_time,old_time_f,old_time_a,old_time_s;

    public MasterService() {
    }

    public static class RecentUseComparator implements Comparator<UsageStats> {

        @Override
        public int compare(UsageStats lhs, UsageStats rhs) {
            return (lhs.getLastTimeUsed() > rhs.getLastTimeUsed()) ? -1 : (lhs.getLastTimeUsed() == rhs.getLastTimeUsed()) ? 0 : 1;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {

        super.onCreate();
        System.out.println("[MasterService] getting started...");
    }

    @Override
    public synchronized void onDestroy() {
        super.onDestroy();
        System.out.println("[StopService] getting stopped...");

        CancelAlarm();
    }

    @Override
    public synchronized int onStartCommand(Intent intent,int flags, int startId) {

        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        PowerManager.WakeLock wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MyWakelockTag");
        wakeLock.acquire();

        check_and_fire_service();

        WriteAlarmFiringTime(0);

        wakeLock.release();

        SetAlarmAgain();
        return START_STICKY;
    }

    public void SetAlarmAgain() {

        long currentTimeMillis = System.currentTimeMillis();
        int common_interval=1000 * Integer.parseInt(getResources().getString(R.string.common_interval));

        long nextUpdateTimeMillis = currentTimeMillis + common_interval;

        Intent intent = new Intent(this, this.getClass());
        PendingIntent pendingIntent = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager manager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {

            manager.set(AlarmManager.RTC_WAKEUP,nextUpdateTimeMillis,pendingIntent);
        }
        else {

            manager.setExact(AlarmManager.RTC_WAKEUP,nextUpdateTimeMillis,pendingIntent);
        }
    }

    public void CancelAlarm() {

        Intent intent = new Intent(this, this.getClass());
        PendingIntent pendingIntent = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager manager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        manager.cancel(pendingIntent);
    }

    public void WriteAlarmFiringTime(int idx) {

        String imei_no,service_name="[MasterService]:";
        long curr_time;

        long diff;

        curr_time=System.currentTimeMillis();
        diff=(curr_time-old_time)/1000;

        old_time=curr_time;

        TelephonyManager telephonyManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
        imei_no = (String)telephonyManager.getDeviceId();

        SimpleDateFormat sdf = new SimpleDateFormat(getResources().getString(R.string.time_format));
        String currentDateandTime = sdf.format(new Date());

        File sdCardRoot = Environment.getExternalStorageDirectory();
        File dataDir = new File(sdCardRoot, getResources().getString(R.string.data_file_path));

        if(!dataDir.exists()) {
            dataDir.mkdirs();
        }

        String registration_file_name = imei_no + "_Alarm_timing.txt";

        if(idx==1){
            service_name="[FileUpload]:";

            diff=(curr_time-old_time_f)/1000;
            old_time_f=curr_time;
        }
        else if(idx==2){
            service_name="[AppLog]:";

            diff=(curr_time-old_time_a)/1000;
            old_time_a=curr_time;
        }
        else if(idx==3){
            service_name="[Sensor]:";

            diff=(curr_time-old_time_s)/1000;
            old_time_s=curr_time;
        }

        //String registration_dtls=service_name+"Alarm at:"+currentDateandTime+","+diff+"\n";
        String registration_dtls=service_name+","+diff+"\n";
        byte[] registration_data = registration_dtls.getBytes();

        File registration_file = new File(dataDir, registration_file_name);
        try {

            FileOutputStream fos;
            fos = new FileOutputStream(registration_file,true);
            fos.write(registration_data);
            fos.close();
        }
        catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }

    public void check_and_fire_service() {

        String file_upload_timestamp=null,app_log_timestamp=null,sensor_sample_timestamp=null,sms_call_browse_timestamp=null,mood_log_timestamp=null,param_log_timestamp=null;
        Date file_upload_time,param_log_time,app_log_time,sensor_sample_time,sms_call_browse_time,mood_log_time;
        Date curr_time;

        curr_time=new Date();

        // Parameter Monitoring Service

        long param_log_interval=1000 * 60 * Integer.parseInt(getResources().getString(R.string.param_logging_interval));

        param_log_timestamp=read_time("R.string.param_log_timestamp");
        param_log_time=convert_to_date(param_log_timestamp);
        if(find_time_diff(curr_time,param_log_time)>=0){

            Date next_param_log_time;

            StartParamLoggingService();

            next_param_log_time=new Date(curr_time.getTime()+param_log_interval);
            store_time("R.string.param_log_timestamp",next_param_log_time);
        }

        // App Logging Service

        long app_log_interval=1000 * Integer.parseInt(getResources().getString(R.string.app_logging_interval));

        app_log_timestamp=read_time("R.string.app_log_timestamp");
        app_log_time=convert_to_date(app_log_timestamp);
        if(find_time_diff(curr_time,app_log_time)>=0){

            Date next_app_log_time;

            StartAppLoggingService();

            next_app_log_time=new Date(curr_time.getTime()+app_log_interval);
            store_time("R.string.app_log_timestamp",next_app_log_time);
        }

        // File Upload Service

        long file_upload_interval=1000 * 60 * Integer.parseInt(getResources().getString(R.string.file_upload_interval));

        file_upload_timestamp=read_time("R.string.file_upload_timestamp");
        file_upload_time=convert_to_date(file_upload_timestamp);
        if(find_time_diff(curr_time,file_upload_time)>=0){

            Date next_file_upload_time;

            StartFileUploadService();

            next_file_upload_time=new Date(curr_time.getTime()+file_upload_interval);
            store_time("R.string.file_upload_timestamp",next_file_upload_time);
        }

        // Sensor Reading Service

        long sensor_sampling_interval=1000 * 60 * Integer.parseInt(getResources().getString(R.string.sensor_sleep_interval));

        sensor_sample_timestamp=read_time("R.string.sensor_sample_timestamp");
        sensor_sample_time=convert_to_date(sensor_sample_timestamp);
        if(find_time_diff(curr_time,sensor_sample_time)>=0){

            Date next_sensor_sample_time;

            StartSensorReadingService();

            next_sensor_sample_time=new Date(curr_time.getTime()+sensor_sampling_interval);
            store_time("R.string.sensor_sample_timestamp",next_sensor_sample_time);
        }

        // SMS, Call and Browsing Recording Service

        long sms_call_browse_interval=1000 * 60 * Integer.parseInt(getResources().getString(R.string.sms_call_browse_interval));

        sms_call_browse_timestamp=read_time("R.string.sms_call_browse_timestamp");
        sms_call_browse_time=convert_to_date(sms_call_browse_timestamp);
        if(find_time_diff(curr_time,sms_call_browse_time)>=0){

            Date next_sms_call_browse_time;

            StartSMSCallBrowseReadingService();

            next_sms_call_browse_time=new Date(curr_time.getTime()+sms_call_browse_interval);
            store_time("R.string.sms_call_browse_timestamp",next_sms_call_browse_time);
        }

        // Mood Logging activity

        //long mood_log_interval=1000 * 60 * Integer.parseInt(getResources().getString(R.string.mood_interval));
        long mood_log_interval=1000 * Integer.parseInt(getResources().getString(R.string.mood_interval));

        mood_log_timestamp=read_time("R.string.mood_log_timestamp");
        mood_log_time=convert_to_date(mood_log_timestamp);
        if(find_time_diff(curr_time,mood_log_time)>=0){

            Date next_mood_log_time;

            //InvokeMoodRecorder();

            Check_and_fire_mood_recorder();

            next_mood_log_time=new Date(curr_time.getTime()+mood_log_interval);
            store_time("R.string.mood_log_timestamp",next_mood_log_time);
        }
    }

    public void StartParamLoggingService() {

        System.out.println("[MasterService]: ParamLoggingService is running");
        //WriteAlarmFiringTime(1);

        Intent intent = new Intent(MasterService.this,MonitorService.class);
        this.startService(intent);
    }

    public void StartAppLoggingService() {

        System.out.println("[MasterService]: AppLoggingService is running");
        WriteAlarmFiringTime(1);

        Intent intent = new Intent(MasterService.this,AppLoggingService.class);
        this.startService(intent);
    }

    public void StartFileUploadService() {

        System.out.println("[MasterService]: FileuploadService is running");
        WriteAlarmFiringTime(2);

        Intent intent = new Intent(MasterService.this,FileUploadService.class);
        this.startService(intent);
    }

    public void StartSensorReadingService() {

        System.out.println("[MasterService]: SensorService is running");
        WriteAlarmFiringTime(3);

        Intent intent = new Intent(MasterService.this,SensorService.class);
        this.startService(intent);
    }

    public void StartSMSCallBrowseReadingService() {

        System.out.println("[MasterService]: SMSCallBrowseService is running");
        WriteAlarmFiringTime(4);

        Intent intent = new Intent(MasterService.this,SMSCallBrowseService.class);
        this.startService(intent);
    }

    public void InvokeMoodRecorder() {

        Intent openMainActivity = new Intent("research.sg.edu.edapp.MoodRecorder");
        openMainActivity.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        getApplicationContext().startActivity(openMainActivity);
    }

    public void Check_and_fire_mood_recorder() {

        int esm_min_txt_length;

        String CurrentAppName=null,LastAppName=null;
        String last_esm_fire_time=null;

        String PROVIDER_NAME = "research.sg.edu.edapp.kb.KbContentProvider";
        String URL = "content://" + PROVIDER_NAME + "/cte";
        Uri CONTENT_URI = Uri.parse(URL);

        esm_min_txt_length=Integer.parseInt(getApplicationContext().getResources().getString(R.string.esm_min_txt_length));

        ContentProviderClient CR = getApplicationContext().getContentResolver().acquireContentProviderClient(CONTENT_URI);

        try {

            Cursor tCursor = CR.query(CONTENT_URI, null, null, null, null);
            //Log.d(SyncStateContract.Constants.LOG,"Number of typed chars:"+tCursor.getCount());

            if(tCursor.getCount()>esm_min_txt_length) {

                CurrentAppName=getCurrentAppName();

                tCursor.moveToLast();
                LastAppName=tCursor.getString(1);

                tCursor.close();

                if(CurrentAppName.equalsIgnoreCase(LastAppName)) {

                    System.out.println("[MasterService]:Same app, do not invoke mood recorder");
                }
                else {

                    if (CurrentAppName.equalsIgnoreCase("LockScreen")) {

                        // Screen is locked, do not fire the ESM
                        System.out.println("[MasterService]Screen locked, Event-based ESM not Fired...");
                    }

                    else if (CurrentAppName.equalsIgnoreCase("research.sg.edu.edapp")) {

                        // User yet to record emotion, do not fire the ESM
                        System.out.println("[MasterService]User yet to record emotion, Event-based ESM not Fired...");
                    }

                    else {

                        last_esm_fire_time = RetrieveESMDetail(getApplicationContext());
                        System.out.println("[MasterService]:"+last_esm_fire_time);

                        if (last_esm_fire_time == null || last_esm_fire_time.trim().equals("null")) {

                            System.out.println("[MasterService]: Going to invoke mood recorder");

                            InvokeMoodRecorder();

                            SimpleDateFormat sdf = new SimpleDateFormat(getApplicationContext().getResources().getString(R.string.time_format));
                            String esm_time = sdf.format(new Date());

                            StoreESMDetail(getApplicationContext(), LastAppName, esm_time);
                        }

                        else {

                            SimpleDateFormat sdf = new SimpleDateFormat(getApplicationContext().getResources().getString(R.string.time_format));

                            Date last_esm_time = sdf.parse(last_esm_fire_time);
                            Date curr_time = new Date();

                            //Log.d(SyncStateContract.Constants.LOG,"Difference since last ESM:"+find_time_diff(curr_time, last_esm_time));

                            if (check_elapsed_time_since_last_probe(curr_time, last_esm_time) > Integer.parseInt(getApplicationContext().getResources().getString(R.string.esm_app_min_window))) {

                                System.out.println("[MasterService]: Going to invoke mood recorder");
                                InvokeMoodRecorder();

                                //SimpleDateFormat sdf = new SimpleDateFormat(context.getResources().getString(R.string.time_format));
                                String esm_time = sdf.format(new Date());
                                StoreESMDetail(getApplicationContext(), LastAppName, esm_time);
                            }
                        }
                    }
                }
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    public String read_time(String time_variable){

        SharedPreferences eda_pref;
        String time=null;

        try {
            Context con = getApplicationContext().createPackageContext(getResources().getString(R.string.sharedpref_pkg), Context.CONTEXT_IGNORE_SECURITY);
            eda_pref = con.getSharedPreferences(getResources().getString(R.string.sharedpref_file), Context.MODE_MULTI_PROCESS);

            time = eda_pref.getString(time_variable, null);
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        if(time==null){

            time = "2000-01-01 00:00:00.000";
        }

        return time;
    }

    public void store_time(String time_variable, Date value) {

        SharedPreferences eda_pref;

        eda_pref = getApplicationContext().getSharedPreferences(getResources().getString(R.string.sharedpref_file), Context.MODE_MULTI_PROCESS);
        SharedPreferences.Editor eda_editor =eda_pref.edit();
        eda_editor.putString(time_variable, convert_to_string(value));
        eda_editor.apply();
        eda_editor.commit();

    }

    public Date convert_to_date(String time){

        Date date=null;

        SimpleDateFormat format = new SimpleDateFormat(getResources().getString(R.string.time_format));
        try {
            date = format.parse(time);
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return date;
    }

    public String convert_to_string(Date date) {

        SimpleDateFormat sdf = new SimpleDateFormat(getResources().getString(R.string.time_format));
        String date_string = sdf.format(date);

        return date_string;
    }

    public float find_time_diff (Date dt1, Date dt2){

        float diff = dt1.getTime() - dt2.getTime();
        return diff;
    }

    public float check_elapsed_time_since_last_probe (Date dt1, Date dt2) {

        float diff = dt1.getTime() - dt2.getTime();

        long secondsInMilli = 1000;
        long minutesInMilli = secondsInMilli * 60;

        float diff_in_min = diff / minutesInMilli;

        System.out.println("Time Diff in Minutes:"+diff_in_min);

        return diff_in_min;
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

    /********************************/
    /* ESM Detail related functions */
    /********************************/

    public void StoreESMDetail(Context context,String esm_app,String esm_time){

        ContentValues values = new ContentValues();
        values.put(EsmDetail.EsmEntry.ESM_APP_NAME, esm_app);
        values.put(EsmDetail.EsmEntry.ESM_TIMESTAMP, esm_time);

        Uri uri = context.getContentResolver().insert(ESMContentProvider.CONTENT_URI, values);
    }

    public String RetrieveESMDetail(Context context) {

        String last_esm_fire_time=null;

        String PROVIDER_NAME = "research.sg.edu.edapp.ESMContentProvider";
        String URL = "content://" + PROVIDER_NAME + "/cte";
        Uri CONTENT_URI = Uri.parse(URL);

        ContentProviderClient CR = context.getContentResolver().acquireContentProviderClient(CONTENT_URI);
        try {

            Cursor eCursor = CR.query(CONTENT_URI, null, null, null, null);

            if(eCursor.getCount()>0) {

                eCursor.moveToLast();
                last_esm_fire_time=eCursor.getString(2);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return last_esm_fire_time;
    }

    /***************************************/
    /* End of ESM Detail related functions */
    /***************************************/
}
