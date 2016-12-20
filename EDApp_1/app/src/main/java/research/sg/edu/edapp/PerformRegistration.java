package research.sg.edu.edapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class PerformRegistration extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perform_registration);

        addListenerOnButton();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void addListenerOnButton() {

        Button Registerbtn, Cancelbtn;

        Registerbtn = (Button) findViewById(R.id.button1);
        Cancelbtn = (Button) findViewById(R.id.button2);

        Registerbtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                if (v.getId() == R.id.button1) {

                    EditText et_name, et_email, et_contact;
                    RadioGroup rggender, rgage;
                    RadioButton radioAgeButton, radioGenderButton;

                    et_name = (EditText) findViewById(R.id.etOne);
                    et_email = (EditText) findViewById(R.id.etTwo);
                    et_contact = (EditText) findViewById(R.id.etThree);

                    rggender = (RadioGroup) findViewById(R.id.radiogenderGroup);
                    rgage = (RadioGroup) findViewById(R.id.radioageGroup);

                    int selectedgender = rggender.getCheckedRadioButtonId();
                    int selectedage = rgage.getCheckedRadioButtonId();

                    String name, email, contact = null;
                    String gender = "Unknown";
                    String age = "Unknown";

                    if(selectedgender >=0) {
                        radioGenderButton = (RadioButton) findViewById(selectedgender);
                        gender=(String)radioGenderButton.getText();
                    }

                    if(selectedage >=0 ) {
                        radioAgeButton = (RadioButton) findViewById(selectedage);
                        age=(String)radioAgeButton.getText();
                    }

                    name = et_name.getText().toString();
                    email = et_email.getText().toString();
                    contact = et_contact.getText().toString();

                    if(name.isEmpty()||email.isEmpty() ){

                        Toast.makeText(PerformRegistration.this, "Please enter your name and email", Toast.LENGTH_SHORT).show();
                    }
                    else {

                        StoreRegistrationDetails(name, email, contact, gender, age);
                        finish();
                        SetDefaultKeyboard();
                        ProvideUsageAccess();
                        StartMasterService();
                    }
                }
            }
        });

        Cancelbtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                if (v.getId() == R.id.button2) {

                    StoreRunningStatus(false);
                    finish();
                    StartMainActivity();
                }
            }
        });
    }

    public void StoreRegistrationDetails(String name,String email,String contact,String gender, String age){

        String imei_no,version;

        TelephonyManager telephonyManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
        imei_no = (String)telephonyManager.getDeviceId();

        version = Build.VERSION.RELEASE;

        SimpleDateFormat sdf = new SimpleDateFormat(getResources().getString(R.string.time_format));
        String currentDateandTime = sdf.format(new Date());

        File sdCardRoot = Environment.getExternalStorageDirectory();
        File dataDir = new File(sdCardRoot, getResources().getString(R.string.data_file_path));

        if(!dataDir.exists()) {
            dataDir.mkdirs();
        }

        String registration_file_name = imei_no + getResources().getString(R.string.registration_file_postfix);

        String registration_dtls=imei_no+","+version+","+name+","+email+","+contact+","+gender+","+age+","+currentDateandTime+"\n";
        byte[] registration_data = registration_dtls.getBytes();

        File registration_file = new File(dataDir, registration_file_name);
        try {

            FileOutputStream fos;
            fos = new FileOutputStream(registration_file,true);
            fos.write(registration_data);
            fos.close();

            StoreRegistrationFlag(true);
        }
        catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }

        move_file(registration_file_name);
    }

    public void StoreRegistrationFlag(boolean flag) {

        SharedPreferences pref = getApplicationContext().getSharedPreferences(getResources().getString(R.string.mood_sharedpref_file), Context.MODE_MULTI_PROCESS);
        SharedPreferences.Editor stat_editor =pref.edit();
        stat_editor.putBoolean(getResources().getString(R.string.sharedpref_registration_flag), flag);
        stat_editor.apply();
        stat_editor.commit();
    }

    public void StoreRunningStatus(boolean flag){

        SharedPreferences pref = getApplicationContext().getSharedPreferences(getResources().getString(R.string.mood_sharedpref_file), Context.MODE_MULTI_PROCESS);
        SharedPreferences.Editor run_editor =pref.edit();
        run_editor.putBoolean(getResources().getString(R.string.sharedpref_running_status), flag);
        run_editor.apply();
        run_editor.commit();
    }

    public void StartMainActivity() {

        Intent intent = new Intent(PerformRegistration.this, MainActivity.class);
        startActivity(intent);
    }

    public void SetDefaultKeyboard() {
        InputMethodManager imeManager = (InputMethodManager) getApplicationContext().getSystemService(INPUT_METHOD_SERVICE);
        List<InputMethodInfo> InputMethods = imeManager.getEnabledInputMethodList();
        imeManager.showInputMethodPicker();
    }

    public void ProvideUsageAccess() {

        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {

            Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
            startActivity(intent);
        }
    }

    public void StartMasterService() {

        Intent intent = new Intent(PerformRegistration.this,MasterService.class);
        this.startService(intent);
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
