package research.sg.edu.edapp;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.os.Environment;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class MonitorService extends IntentService {

    public MonitorService() {
        super("MonitorService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        String keyboard_name=extract_keyboard_details();

        write_monitored_data(keyboard_name);
    }

    public String extract_keyboard_details() {

        /*InputMethodManager imeManager = (InputMethodManager) getApplicationContext().getSystemService(INPUT_METHOD_SERVICE);
        List<InputMethodInfo> InputMethods = imeManager.getEnabledInputMethodList();
        imeManager.showInputMethodPicker();*/

        String keyboard_name = Settings.Secure.getString(getContentResolver(),Settings.Secure.DEFAULT_INPUT_METHOD);
        System.out.println("[KeyboardActivity]: Default IME"+keyboard_name);

        return keyboard_name;
    }

    public void write_monitored_data(String keyboard_name) {

        TelephonyManager telephonyManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
        String imei_no = (String)telephonyManager.getDeviceId();

        SimpleDateFormat sdf = new SimpleDateFormat(getResources().getString(R.string.time_format));
        String currentDateandTime = sdf.format(new Date());

        File sdCardRoot = Environment.getExternalStorageDirectory();
        File dataDir = new File(sdCardRoot, getResources().getString(R.string.data_file_path));

        if(!dataDir.exists()) {
            dataDir.mkdirs();
        }

        String monitor_file_name = imei_no + "_Monitored_param.txt";

        String monitored_param=currentDateandTime+","+keyboard_name+"\n";
        byte[] monitored_data = monitored_param.getBytes();

        File monitor_file = new File(dataDir, monitor_file_name);
        try {

            FileOutputStream fos;
            fos = new FileOutputStream(monitor_file,true);
            fos.write(monitored_data);
            fos.close();
        }
        catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }
}