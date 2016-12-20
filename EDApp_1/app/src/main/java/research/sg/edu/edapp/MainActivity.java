package research.sg.edu.edapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    Button ShowKb, Stopbtn, Statbtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if(!isAppRunning())
        {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);

            //Create various folders
            CreateFolders();

            Stopbtn=(Button)findViewById(R.id.button2);
            Stopbtn.setEnabled(false);

            //Listener for Start Application button
            ShowKb=(Button)findViewById(R.id.button1);
            ShowKb.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {

                    if(v.getId()==R.id.button1) {

                        ShowKb.setEnabled(false);
                        Stopbtn.setEnabled(true);

                        finish();

                        //RegisterUser();
                        StoreRunningStatus(true);
                        ReceiveConsent();
                    }
                }
            });

            //Listener for Stop Application button (if the user clicks stop button on the launching screen itself)
            Stopbtn=(Button)findViewById(R.id.button2);
            Stopbtn.setOnClickListener(new View.OnClickListener(){
                public void onClick(View v) {

                    if(v.getId()==R.id.button2) {

                        System.out.println("Stopping the application and all associated services");
                        //StopServices();
                        ShowKb.setEnabled(true);
                        Stopbtn.setEnabled(false);

                        StoreRunningStatus(false);
                        finish();
                    }
                }
            });

            Statbtn=(Button)findViewById(R.id.button3);

            if(!IsAlreadyRegistered()) {
                Statbtn.setEnabled(false);
            }
            else {

                Statbtn.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {

                        if (v.getId() == R.id.button3) {
                            DisplayDashboard();
                        }
                    }
                });
            }
        }
        else
        {
            if(!IsAlreadyRegistered()){

                super.onCreate(savedInstanceState);
                setContentView(R.layout.activity_main);

                Stopbtn=(Button)findViewById(R.id.button2);
                Stopbtn.setEnabled(false);

                Statbtn=(Button)findViewById(R.id.button3);
                Statbtn.setEnabled(false);

                ShowKb=(Button)findViewById(R.id.button1);
                ShowKb.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {

                        if(v.getId()==R.id.button1) {

                            ShowKb.setEnabled(false);
                            Stopbtn.setEnabled(true);

                            finish();

                            //RegisterUser();
                            StoreRunningStatus(true);
                            ReceiveConsent();
                        }
                    }
                });
            }
            else {

                super.onCreate(savedInstanceState);
                setContentView(R.layout.activity_main);

                ShowKb=(Button)findViewById(R.id.button1);
                ShowKb.setEnabled(false);

                Stopbtn=(Button)findViewById(R.id.button2);

                //Listener for Stop Application button

                Stopbtn.setOnClickListener(new View.OnClickListener(){
                    public void onClick(View v) {

                        if(v.getId()==R.id.button2) {

                            System.out.println("Stopping the application and all associated services");
                            //StopServices();
                            ShowKb.setEnabled(true);
                            Stopbtn.setEnabled(false);

                            StopServices();
                            StoreRunningStatus(false);
                            finish();
                        }
                    }
                });

                Statbtn=(Button)findViewById(R.id.button3);
                Statbtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        if(v.getId()==R.id.button3) {

                            finish();
                            DisplayDashboard();
                        }
                    }
                });
            }
        }
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

    public boolean isAppRunning(){
        boolean running_status= false;
        SharedPreferences mood_pref=null;

        //Sharedpreference based Running Status

        try {
            Context con = getApplicationContext().createPackageContext(getResources().getString(R.string.moodrecorder_pkg), Context.CONTEXT_IGNORE_SECURITY);
            mood_pref = con.getSharedPreferences(getResources().getString(R.string.mood_sharedpref_file), Context.MODE_MULTI_PROCESS);

            running_status = mood_pref.getBoolean(getResources().getString(R.string.sharedpref_running_status), false);
            System.out.println("App Running Status:" + running_status);
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return running_status;
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

    public void CreateFolders() {
        File sdCardRoot = Environment.getExternalStorageDirectory();

        File dataDir = new File(sdCardRoot, getResources().getString(R.string.data_file_path));
        File archiveDir = new File(sdCardRoot, getResources().getString(R.string.archive_file_path));
        File tobeuploadedDir = new File(sdCardRoot, getResources().getString(R.string.to_be_uploaded_file_path));

        if(!dataDir.exists()) {
            dataDir.mkdirs();
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        if(!archiveDir.exists()) {
            archiveDir.mkdirs();
        }

        if(!tobeuploadedDir.exists()) {
            tobeuploadedDir.mkdirs();
        }
    }

    public void ReceiveConsent()
    {
        Intent intent = new Intent(MainActivity.this, UserConsent.class);
        startActivity(intent);
    }

    public void StoreRunningStatus(boolean flag){

        SharedPreferences pref = getApplicationContext().getSharedPreferences(getResources().getString(R.string.mood_sharedpref_file), Context.MODE_MULTI_PROCESS);
        SharedPreferences.Editor run_editor =pref.edit();
        run_editor.putBoolean(getResources().getString(R.string.sharedpref_running_status), flag);
        run_editor.apply();
        run_editor.commit();
    }

    public void StopServices() {

        Intent intent = new Intent(MainActivity.this,MasterService.class);
        this.stopService(intent);

    }

    public void DisplayDashboard()
    {
        Intent intent = new Intent(MainActivity.this, DisplayDashboard.class);
        startActivity(intent);
    }
}