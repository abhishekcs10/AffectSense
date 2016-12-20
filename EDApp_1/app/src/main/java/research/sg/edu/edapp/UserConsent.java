package research.sg.edu.edapp;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.provider.Settings;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;

import java.util.List;


public class UserConsent extends ActionBarActivity {

    Button Consent_btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_consent);

        Consent_btn=(Button)findViewById(R.id.usr_ok);
        Consent_btn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                if (v.getId() == R.id.usr_ok) {

                    Consent_btn.setEnabled(false);
                    Consent_btn.setVisibility(View.INVISIBLE);

                    RegisterUser();
                    ShowKeyBoards();
                    StartServices();
                    finish();
                }
            }
        });
    }

    public void RegisterUser(){

        if(!IsAlreadyRegistered())
        {
            Intent intent = new Intent(UserConsent.this, PerformRegistration.class);
            startActivity(intent);
        }
    }

    public boolean IsAlreadyRegistered(){

        boolean registration_flag= false;
        SharedPreferences mood_pref=null;

        //Sharedpreference based Registration Status

        try {
            Context con = getApplicationContext().createPackageContext(getResources().getString(R.string.moodrecorder_pkg), Context.CONTEXT_IGNORE_SECURITY);
            mood_pref = con.getSharedPreferences(getResources().getString(R.string.mood_sharedpref_file), Context.MODE_PRIVATE);

            registration_flag = mood_pref.getBoolean(getResources().getString(R.string.sharedpref_registration_flag), false);
            System.out.println("Registration Status:" + registration_flag);
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return registration_flag;
    }

    public void ShowKeyBoards()
    {
        String id = Settings.Secure.getString(getContentResolver(),Settings.Secure.DEFAULT_INPUT_METHOD);
        System.out.println("[KeyboardActivity]: Default IME"+id);

        if(id.contains("research.sg.edu.edapp")) {


        }
        else{
            Intent intent = new Intent(UserConsent.this, KeyboardActivity.class);
            startActivity(intent);
        }
    }

    public void StartServices() {

        if(IsAlreadyRegistered()){

            SetAlarms();
        }

    }

    public void SetAlarms() {

        int common_interval=1000 * Integer.parseInt(getResources().getString(R.string.common_interval));

        Intent alarmIntent = new Intent(UserConsent.this, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(UserConsent.this, 0, alarmIntent, 0);

        AlarmManager manager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {

            manager.set(AlarmManager.RTC_WAKEUP,System.currentTimeMillis()+common_interval,pendingIntent);
        }
        else {

            manager.setExact(AlarmManager.RTC_WAKEUP,System.currentTimeMillis()+common_interval,pendingIntent);
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_user_consent, menu);
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
}
