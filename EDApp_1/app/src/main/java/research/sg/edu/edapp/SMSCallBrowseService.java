package research.sg.edu.edapp;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.CallLog;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SMSCallBrowseService extends IntentService {

    static boolean last_sms_capture = false;
    static boolean last_call_capture = false;

    public SMSCallBrowseService() {
        super("SMSCallBrowseService");
        System.out.println("[SMSCallBrowser]: last_sms_capture - "+last_sms_capture);
        System.out.println("[SMSCallBrowser]: last_call_capture - "+last_call_capture);
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        getSMSDetail();
        getCallDetail();
        //getEmailDetail();
        //getBrowserHistory();

    }

    public void getSMSDetail() {

        long last_sms_date;

        last_sms_date=RetrieveLastDate(1);

        System.out.println("[SMSCallBrowser]:"+last_sms_date);
        StringBuffer sb = new StringBuffer();

        Uri uri = Uri.parse("content://sms");
        Cursor cursor = getApplicationContext().getContentResolver().query(uri, null, null, null, "date ASC");
        System.out.println("[SMSCallBrowser]:"+cursor.getCount());
        if(cursor.moveToFirst()) {
            for(int i=0; i<cursor.getCount();i++) {
                int body_num = cursor.getColumnIndex("body");
                int number_num = cursor.getColumnIndex("address");
                int date_num = cursor.getColumnIndex("date");
                String body = (body_num==-1)?"": cursor.getString(body_num).toString();
                String number1 = (number_num==-1)?"0000000000": cursor.getString(number_num).toString();
                String number = (number1.length()>=10)?number1.substring(number1.length() - 6):number1;
                String date = (date_num==-1)?"1420106400000": cursor.getString(date_num).toString();
                Date smsDayTime = new Date(Long.valueOf(date));
                String type = cursor.getString(cursor.getColumnIndexOrThrow("type")).toString();
                cursor.moveToNext();
                //System.out.println("[SMSCallBrowser]:"+smsDayTime.getTime());
                if (smsDayTime.compareTo(new Date(last_sms_date)) <= 0)
                    continue;

                String typeOfSMS = null;
                switch (Integer.parseInt(type)) {
                    case 1:
                        typeOfSMS = "INBOX";
                        break;

                    case 2:
                        typeOfSMS = "SENT";
                        break;

                    case 3:
                        typeOfSMS = "DRAFT";
                        break;
                }
                SimpleDateFormat sdf = new SimpleDateFormat(getApplicationContext().getResources().getString(R.string.time_format));
                sb.append(number + "," + typeOfSMS + "," + sdf.format(smsDayTime) + "," + body.length()+ "\n");
                StoreLastDate(1,smsDayTime);
            }
        }
        if(!last_sms_capture){

            last_sms_capture=true;
            StoreLastDate(1,new Date());
        }
        cursor.close();
        WriteintoFile(sb,1);
    }

    public void getCallDetail() {

        long last_call_date;

        last_call_date=RetrieveLastDate(2);

        System.out.println("[SMSCallBrowser]:"+last_call_date);
        StringBuffer sb = new StringBuffer();

        Cursor managedCursor = getApplicationContext().getContentResolver().query(CallLog.Calls.CONTENT_URI, null,
                null, null, "date ASC");
        int number = managedCursor.getColumnIndex(CallLog.Calls.NUMBER);
        int type = managedCursor.getColumnIndex(CallLog.Calls.TYPE);
        int date = managedCursor.getColumnIndex(CallLog.Calls.DATE);
        int duration = managedCursor.getColumnIndex(CallLog.Calls.DURATION);

        while (managedCursor.moveToNext()) {
            String number1 = managedCursor.getString(number);
            String phNumber = (number1.length()>=10)?number1.substring(number1.length() - 6):number1;
            String callType = managedCursor.getString(type);
            String callDate = managedCursor.getString(date);
            Date callDayTime = new Date(Long.valueOf(callDate));

            if(callDayTime.compareTo(new Date(last_call_date))<=0)
                continue;

            String callDuration = managedCursor.getString(duration);
            String dir = null;
            int dircode = Integer.parseInt(callType);
            switch (dircode) {
                case CallLog.Calls.OUTGOING_TYPE:
                    dir = "OUTGOING";
                    break;

                case CallLog.Calls.INCOMING_TYPE:
                    dir = "INCOMING";
                    break;

                case CallLog.Calls.MISSED_TYPE:
                    dir = "MISSED";
                    break;
            }

            SimpleDateFormat sdf = new SimpleDateFormat(getApplicationContext().getResources().getString(R.string.time_format));
            sb.append(phNumber + "," + dir + ","+ sdf.format(callDayTime) + "," + callDuration+"\n");
            StoreLastDate(2,callDayTime);
        }

        if(!last_call_capture){

            last_call_capture=true;
            StoreLastDate(2,new Date());
        }
        managedCursor.close();
        WriteintoFile(sb,2);
    }

    public long RetrieveLastDate(int date_type) {

        long last_date=new Date().getTime();

        SharedPreferences sp = getApplicationContext().getSharedPreferences(getApplicationContext().getResources().getString(R.string.date_sharedpref_file),Context.MODE_PRIVATE);
        if(date_type==1){

            last_date = sp.getLong(getApplicationContext().getResources().getString(R.string.sms_date), new Date().getTime());
        }
        else if(date_type==2){

            last_date = sp.getLong(getApplicationContext().getResources().getString(R.string.call_date), new Date().getTime());
        }
        else if(date_type==3) {

            last_date=sp.getLong(getApplicationContext().getResources().getString(R.string.browse_date), new Date().getTime());
        }

        return last_date;
    }

    public void StoreLastDate(int date_type, Date dt) {

        SharedPreferences sp = getApplicationContext().getSharedPreferences(getApplicationContext().getResources().getString(R.string.date_sharedpref_file), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();

        if(date_type==1){

            editor.putLong(getApplicationContext().getResources().getString(R.string.sms_date), dt.getTime()).apply();
            editor.commit();
        }
        else if(date_type==2){

            editor.putLong(getApplicationContext().getResources().getString(R.string.call_date), dt.getTime()).apply();
            editor.commit();
        }
        else if(date_type==3) {

            editor.putLong(getApplicationContext().getResources().getString(R.string.browse_date), dt.getTime()).apply();
            editor.commit();
        }
    }

    public String RetrieveFileCtr(int type) {

        String ctr="000000";

        try {

            Context con = getApplicationContext().createPackageContext(getApplicationContext().getResources().getString(R.string.ctr_pkg), Context.CONTEXT_IGNORE_SECURITY);
            SharedPreferences pref = con.getSharedPreferences(getApplicationContext().getResources().getString(R.string.ctr_sharedpref_file), Context.MODE_MULTI_PROCESS);

            if(type == 1) {

                ctr = pref.getString(getApplicationContext().getResources().getString(R.string.sms_ctr), "000000");
            }
            else if(type == 2) {

                ctr = pref.getString(getApplicationContext().getResources().getString(R.string.call_ctr), "000000");
            }
            else if(type == 3) {

                ctr = pref.getString(getApplicationContext().getResources().getString(R.string.browse_ctr), "000000");
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return ctr;
    }

    public void StoreFileCtr(int type, String file_ctr){

        SharedPreferences pref = getApplicationContext().getSharedPreferences(getApplicationContext().getResources().getString(R.string.ctr_sharedpref_file), Context.MODE_MULTI_PROCESS);
        SharedPreferences.Editor log_editor =pref.edit();

        if(type==1) {

            log_editor.putString(getApplicationContext().getResources().getString(R.string.sms_ctr), file_ctr);
            log_editor.commit();
        }
        else if(type==2) {

            log_editor.putString(getApplicationContext().getResources().getString(R.string.call_ctr), file_ctr);
            log_editor.commit();
        }
        else if(type==3) {

            log_editor.putString(getApplicationContext().getResources().getString(R.string.browse_ctr), file_ctr);
            log_editor.commit();
        }
    }

    public void WriteintoFile(StringBuffer sb,int type) {

        String imei_no, file_ctr, file_name, postfix=".txt";
        int file_size=0, file_size_threshold=0;

        File sdCardRoot = Environment.getExternalStorageDirectory();
        File dataDir = new File(sdCardRoot, getApplicationContext().getResources().getString(R.string.data_file_path));

        TelephonyManager telephonyManager = (TelephonyManager) getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
        imei_no = (String) telephonyManager.getDeviceId();

        file_ctr=RetrieveFileCtr(1);

        if(type==1){

            postfix=getApplicationContext().getResources().getString(R.string.sms_file_postfix);
            file_size_threshold = Integer.parseInt(getApplicationContext().getResources().getString(R.string.sms_file_size_limit));
        }
        else if(type==2){

            postfix=getApplicationContext().getResources().getString(R.string.call_file_postfix);
            file_size_threshold = Integer.parseInt(getApplicationContext().getResources().getString(R.string.call_file_size_limit));
        }
        else if(type==2){

            postfix=getApplicationContext().getResources().getString(R.string.browse_file_postfix);
            file_size_threshold = Integer.parseInt(getApplicationContext().getResources().getString(R.string.browse_file_size_limit));
        }

        file_name =  imei_no + "_" + file_ctr + postfix;

        String string_t = sb.toString();
        byte[] data = string_t.getBytes();

        FileOutputStream fos;
        File file = new File(dataDir, file_name);
        try {
            fos = new FileOutputStream(file,true);
            fos.write(data);
            fos.close();
        }
        catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }

        file_size = Integer.parseInt(String.valueOf(file.length() / 1024));
        System.out.println("File Size:" + file_size + ", File Threshold:" + file_size_threshold);

        if (file_size > file_size_threshold) {
            int ctr = Integer.parseInt(file_ctr) + 1;
            file_ctr=String.valueOf(ctr);
            file_ctr=String.format("%06d", Integer.parseInt(file_ctr));
            StoreFileCtr(type,file_ctr);

            move_file(file_name);
        }
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

            //Now delete the file from the DataFiles location
            if(sourceLocation.exists()) {
                sourceLocation.delete();
            }
        }
        catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }
}
