package research.sg.edu.edapp;

import android.content.ContentProviderClient;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.inputmethodservice.Keyboard;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
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


public class MoodRecorder extends AppCompatActivity {

    private RadioGroup radioMoodGroup, radioGroupUsrExp;
    private RadioButton radioMoodButton, radioUsrExpButton;
    private Button btnRecordMood;

    private static String mood_ctr ="000000";
    private boolean mood_rdy_to_record;

    private static String tap_ctr="000000";

    float x1,x2,y1,y2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        SharedPreferences pref=null;
        String tstamp=null;

        int mood_int_threshold;

        super.onCreate(savedInstanceState);
        setContentView(R.layout.mood_popup);
        //WriteMoodFiringTime();

        try {
            Context con = getApplicationContext().createPackageContext(getResources().getString(R.string.keyboard_pkg), Context.CONTEXT_IGNORE_SECURITY);
            pref = con.getSharedPreferences(getResources().getString(R.string.sharedpref_file), Context.MODE_MULTI_PROCESS);

            SharedPreferences.Editor seditor =pref.edit();
            seditor.putBoolean(getResources().getString(R.string.sharedpref_mood_rdy_to_record), false);
            seditor.apply();
            seditor.commit();
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        //setMoodCtr();
        //setTapCtr();

        addListenerOnButton();
    }

    public void addListenerOnButton() {

        radioMoodGroup = (RadioGroup) findViewById(R.id.radioMoodGroup);
        //radioGroupUsrExp = (RadioGroup) findViewById(R.id.radioGroupUsrExp);
        btnRecordMood = (Button) findViewById(R.id.btnRecordMood);

        btnRecordMood.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                // get selected radio button from radioGroup
                int selectedId = radioMoodGroup.getCheckedRadioButtonId();
                //int usrExpId = radioGroupUsrExp.getCheckedRadioButtonId();

                //System.out.println( selectedId + ":" +  ":" + usrExpId );

                try {
                    // find the radiobutton by returned id
                    radioMoodButton = (RadioButton) findViewById(selectedId);
                    //radioUsrExpButton = (RadioButton) findViewById(usrExpId);

                    //Toast.makeText(MainActivity.this,radioMoodButton.getText(), Toast.LENGTH_SHORT).show();
                    RecordMood(radioMoodButton);
                    RecordTapMood(radioMoodButton);
                }
                catch(Exception e) {
                    Toast.makeText(MoodRecorder.this,"Please select your emotion", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void RecordMood(RadioButton radioMoodButton) {

        String imei_no,mood_dtls;
        String currentDateandTime, mood_file_name;

        FileOutputStream fos;

        // Store current Mood in the sharedpref

        SharedPreferences pref = getApplicationContext().getSharedPreferences(getResources().getString(R.string.tap_mood_sharedpref_file), Context.MODE_WORLD_READABLE);
        //SharedPreferences pref = getApplicationContext().getSharedPreferences(getResources().getString(R.string.tap_mood_sharedpref_file), Context.MODE_MULTI_PROCESS);
        SharedPreferences.Editor mood_editor =pref.edit();
        mood_editor.putString(getResources().getString(R.string.sharedpref_current_mood), Integer.toString(getMoodId(radioMoodButton)));
        mood_editor.apply();
        mood_editor.commit();

        System.out.println("[MoodRecorder] Application Context, Package:"+ getApplicationContext()+getApplicationContext().getPackageName());

        // End of storing current Mood in the sharedpref

        TelephonyManager telephonyManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
        imei_no = (String)telephonyManager.getDeviceId();

        SimpleDateFormat sdf = new SimpleDateFormat(getResources().getString(R.string.time_format));
        currentDateandTime = sdf.format(new Date());

        //mood_dtls = currentDateandTime + "," + radioMoodButton.getText() + "," + getMoodId(radioMoodButton) + "," + usrexpbtn.getText() + ","  + getUsrExpId(usrexpbtn) + "\n";
        mood_dtls = currentDateandTime + "," + radioMoodButton.getText() + "," + getMoodId(radioMoodButton)  + "\n";
        byte[] mood_data = mood_dtls.getBytes();

        File sdCardRoot = Environment.getExternalStorageDirectory();
        //System.out.println(getResources().getString(R.string.data_file_path)+"  "+ R.string.data_file_path);

        File dataDir = new File(sdCardRoot, getResources().getString(R.string.data_file_path));

        if(!dataDir.exists()) {
            dataDir.mkdirs();
        }


        mood_ctr=RetrieveMoodCtr();
        mood_file_name = imei_no + "_" + mood_ctr + getResources().getString(R.string.mood_file_postfix);

        //mood_file_name = imei_no + getResources().getString(R.string.mood_file_postfix);
        File mood_file = new File(dataDir, mood_file_name);
        try {
            //fos=openFileOutput(, Context.MODE_PRIVATE);
            fos = new FileOutputStream(mood_file,true);
            fos.write(mood_data);
            fos.close();
        }
        catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }

        //Checks file size and move to ToBeUploaded folder
        int mood_file_size = Integer.parseInt(String.valueOf(mood_file.length() / 1024));
        //mood_file_size = mood_file_size /4;
        int mood_file_size_threshold = Integer.parseInt(getResources().getString(R.string.mood_file_size_limit));
        System.out.println("Mood File Size:" + mood_file_size + ", Mood File Threshold:" + mood_file_size_threshold );

        if (mood_file_size > mood_file_size_threshold) {
            int ctr = Integer.parseInt(mood_ctr) + 1;
            mood_ctr=String.valueOf(ctr);
            mood_ctr=String.format("%06d", Integer.parseInt(mood_ctr));
            //String.format("%05d", Integer.parseInt(mood_ctr));
            move_file(mood_file_name);
        }

        StoreMoodCtr(mood_ctr);
        //finish();
    }

    public void RecordTapMood(RadioButton radioMoodButton) {

        String imei_no;
        String moodRecordTimestamp, tap_file_name;
        String typing_session_no;

        File tap_file;

        TelephonyManager telephonyManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
        imei_no = (String)telephonyManager.getDeviceId();

        SimpleDateFormat sdf = new SimpleDateFormat(getResources().getString(R.string.time_format));
        moodRecordTimestamp = sdf.format(new Date());

        File sdCardRoot = Environment.getExternalStorageDirectory();
        File dataDir = new File(sdCardRoot, getResources().getString(R.string.data_file_path));

        if(!dataDir.exists()) {
            dataDir.mkdirs();
        }

        String string_t="";

        String PROVIDER_NAME = "research.sg.edu.edapp.kb.KbContentProvider";
        String URL = "content://" + PROVIDER_NAME + "/cte";
        Uri CONTENT_URI = Uri.parse(URL);
        ContentProviderClient CR = getContentResolver().acquireContentProviderClient(CONTENT_URI);

        try {
            Cursor tCursor = CR.query(CONTENT_URI, null, null, null, null);

            typing_session_no=RetrieveTypingSession();
            tap_ctr=RetrieveTapCtr();

            tap_file_name = imei_no + "_" + tap_ctr + getResources().getString(R.string.tap_file_postfix);
            tap_file = new File(dataDir, tap_file_name);

            process_meta_data_typing(tCursor,typing_session_no,getMoodId(radioMoodButton));

            tCursor.moveToFirst();
            while (!tCursor.isAfterLast()) {

                string_t="";
                //string_t=typing_session_no + ","+ tCursor.getString(1) + "," + tCursor.getString(2)+ "," + tCursor.getString(3) +"," + getMoodId(radioMoodButton) + "," + getUsrExpId(usrexpbtn) + "," + moodRecordTimestamp +"\n";
                string_t=typing_session_no + ","+ tCursor.getString(1) + "," + tCursor.getString(2)+ "," + tCursor.getString(3) +"," + getMoodId(radioMoodButton) + ","  + moodRecordTimestamp +"\n";
                System.out.println(typing_session_no + "," + tCursor.getLong(0) + "," + tCursor.getString(1) + "," + tCursor.getString(2) + "," + tCursor.getString(3) + "," + moodRecordTimestamp);

                try{
                    FileOutputStream fos = new FileOutputStream(tap_file,true);
                    fos.write(string_t.getBytes());
                    fos.close();
                }catch(Exception e) {
                    Log.d("EXCEPTION", e.getMessage());
                }

                //System.out.println(tCursor.getLong(0) + "," + tCursor.getString(1) + "," + tCursor.getString(2) + "," + tCursor.getString(3));
                tCursor.moveToNext();
            }
            tCursor.close();

            int tap_file_size = Integer.parseInt(String.valueOf(tap_file.length() / 1024));
            //mood_file_size = mood_file_size /4;
            int tap_file_size_threshold = Integer.parseInt(getResources().getString(R.string.tap_file_size_limit));
            System.out.println("Tap File Size:" + tap_file_size + ", Tap File Threshold:" + tap_file_size_threshold );

            if (tap_file_size > tap_file_size_threshold) {
                int ctr = Integer.parseInt(tap_ctr) + 1;
                tap_ctr=String.valueOf(ctr);
                tap_ctr=String.format("%06d", Integer.parseInt(tap_ctr));
                //String.format("%05d", Integer.parseInt(mood_ctr));
                move_file(tap_file_name);
            }

            int session_no=Integer.parseInt(typing_session_no)+1;
            typing_session_no=String.valueOf(session_no);
            typing_session_no=String.format("%06d", Integer.parseInt(typing_session_no));

            StoreTapCtr(tap_ctr);
            StoretypingSession(typing_session_no);
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        try{

            int deleted_rows = CR.delete(CONTENT_URI, null, null);
            System.out.println("Number of deleted entries:" +deleted_rows);
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        //Store additional features
        //mean_itd, #backspace_key,#splsymbol_key,#touch_count,#erased_text_length,typ_dur,time_of_day

        finish();
    }

    public void process_meta_data_typing(Cursor tCursor, String typing_session_no, int mood){

        Date t1=null,t2=null;
        float mean_itd=0;
        float secondsInMilli = 1000;

        SimpleDateFormat sdf = new SimpleDateFormat(getResources().getString(R.string.time_format));
        tCursor.moveToFirst();

        try{

            t1=sdf.parse(tCursor.getString(2));
        }
        catch (Exception e) {

            e.printStackTrace();
        }
        tCursor.moveToNext();

        while (!tCursor.isAfterLast()){

            try{

                t2=sdf.parse(tCursor.getString(2));
            }
            catch (Exception e) {

                e.printStackTrace();
            }

            mean_itd=mean_itd+(t2.getTime()-t1.getTime())/secondsInMilli;
            t1=t2;

            tCursor.moveToNext();
        }

        mean_itd=mean_itd/tCursor.getCount();

        int no_of_backspace=0;
        int no_of_spaces=0;
        int no_of_digit=0;
        int no_of_alphabet=0;
        int no_of_spl_char=0;

        tCursor.moveToFirst();
        while (!tCursor.isAfterLast()){

            if( (Integer.parseInt(tCursor.getString(3))== Keyboard.KEYCODE_DELETE)||(Integer.parseInt(tCursor.getString(3))== KeyEvent.KEYCODE_BACK)) {
                no_of_backspace=no_of_backspace+1;
            }
            else if(Integer.parseInt(tCursor.getString(3))==32) {
                no_of_spaces=no_of_spaces+1;
            }
            else if( (Integer.parseInt(tCursor.getString(3))>=48) && (Integer.parseInt(tCursor.getString(3))<=57)) {
                no_of_digit=no_of_digit+1;
            }
            else if( (Integer.parseInt(tCursor.getString(3))>=97) && (Integer.parseInt(tCursor.getString(3))<=122)) {
                no_of_alphabet=no_of_alphabet+1;
            }
            tCursor.moveToNext();
        }

        no_of_spl_char=tCursor.getCount()-(no_of_backspace+no_of_spaces+no_of_digit+no_of_alphabet);

        tCursor.moveToFirst();

        try{

            t1=sdf.parse(tCursor.getString(2));
        }
        catch (Exception e) {

            e.printStackTrace();
        }

        tCursor.moveToLast();
        try{

            t2=sdf.parse(tCursor.getString(2));
        }
        catch (Exception e) {

            e.printStackTrace();
        }

        float session_dur=(t2.getTime()-t1.getTime())/secondsInMilli;
        System.out.println("Mean ITD is:"+mean_itd+" No of backspaces:"+no_of_backspace + "Session duration:"+session_dur +"Total touch count:" + tCursor.getCount()+"Mood:"+mood);
        //tCursor.close();

        File session_file;
        String imei_no,session_file_ctr,session_file_name;

        TelephonyManager telephonyManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
        imei_no = (String)telephonyManager.getDeviceId();

        File sdCardRoot = Environment.getExternalStorageDirectory();
        File dataDir = new File(sdCardRoot, getResources().getString(R.string.data_file_path));

        if(!dataDir.exists()) {
            dataDir.mkdirs();
        }

        String string_t= typing_session_no + "," + mean_itd + "," + no_of_backspace + "," + no_of_spaces + "," + no_of_digit + "," + no_of_alphabet + "," + no_of_spl_char + "," + session_dur + "," + tCursor.getCount() + "," + mood + "\n" ;

        session_file_ctr=RetrieveSessionFileCtr();

        session_file_name = imei_no + "_" + session_file_ctr + getResources().getString(R.string.session_file_postfix);
        session_file = new File(dataDir, session_file_name);

        try{
            FileOutputStream fos = new FileOutputStream(session_file,true);
            fos.write(string_t.getBytes());
            fos.close();
        }catch(Exception e) {
            Log.d("EXCEPTION",e.getMessage());
        }

        int session_file_size = Integer.parseInt(String.valueOf(session_file.length() / 1024));

        int session_file_size_threshold = Integer.parseInt(getResources().getString(R.string.session_file_size_limit));
        System.out.println("Session File Size:" + session_file_size + ", Session File Threshold:" + session_file_size_threshold );

        if (session_file_size > session_file_size_threshold) {
            int ctr = Integer.parseInt(session_file_ctr) + 1;
            session_file_ctr=String.valueOf(ctr);
            session_file_ctr=String.format("%06d", Integer.parseInt(session_file_ctr));
            move_file(session_file_name);
        }

        StoreSessionFileCtr(session_file_ctr);
    }

    public int getMoodId(RadioButton radioMoodButton) {

        int mood_id=-99;
        String mood_string;

        mood_string = (String)radioMoodButton.getText();

        switch(mood_string.toUpperCase())
        {
            case "SAD / DEPRESSED": mood_id=-2;
                break;
            case "HAPPY / EXCITED": mood_id = 2;
                break;
            case "STRESSED": mood_id = 1;
                break;
            case "RELAXED": mood_id = 0;
                break;
            case "NO RESPONSE": mood_id = -99;
                break;

        }
        return mood_id;
    }

    public int getUsrExpId(RadioButton radioUsrExpButton) {

        int usrexp_id=-99;
        String mood_string;

        mood_string = (String)radioUsrExpButton.getText();

        switch(mood_string.toUpperCase())
        {
            case "NOT APPLICABLE": usrexp_id=0;
                break;
            case "NOT SATISFACTORY": usrexp_id = -1;
                break;
            case "SATISFACTORY": usrexp_id = 1;
                break;
        }
        return usrexp_id;
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

            //Now delete the mood file from the DataFiles location
            if(sourceLocation.exists()) {
                sourceLocation.delete();
            }
        }
        catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }

    public void WriteMoodFiringTime(){

        String currentDateandTime, mood_file_firing_tstmap, imei_no;

        FileOutputStream fos;

        SimpleDateFormat sdf = new SimpleDateFormat(getResources().getString(R.string.time_format));
        currentDateandTime = sdf.format(new Date());

        File sdCardRoot = Environment.getExternalStorageDirectory();
        File dataDir = new File(sdCardRoot, getResources().getString(R.string.data_file_path));

        if(!dataDir.exists()) {
            dataDir.mkdirs();
        }

        currentDateandTime = currentDateandTime + "\n";
        byte[] firing_tstamp = currentDateandTime.getBytes();

        TelephonyManager telephonyManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
        imei_no = (String)telephonyManager.getDeviceId();

        mood_file_firing_tstmap = imei_no + getResources().getString(R.string.mood_firing_file_postfix);

        //mood_file_name = imei_no + getResources().getString(R.string.mood_file_postfix);
        File mood_file = new File(dataDir, mood_file_firing_tstmap);
        try {
            //fos=openFileOutput(, Context.MODE_PRIVATE);
            fos = new FileOutputStream(mood_file,true);
            fos.write(firing_tstamp);
            fos.close();
        }
        catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }

    public boolean onTouchEvent(MotionEvent touchevent)
    {
        switch (touchevent.getAction())
        {
            // when user first touches the screen we get x and y coordinate
            case MotionEvent.ACTION_DOWN:
            {
                x1 = touchevent.getX();
                y1 = touchevent.getY();
                break;
            }
            case MotionEvent.ACTION_UP:
            {
                x2 = touchevent.getX();
                y2 = touchevent.getY();

                //if left to right sweep event on screen
                if (x1 < x2)
                {
                    //Toast.makeText(this, "Left to Right Swap Performed", Toast.LENGTH_LONG).show();
                    finish();
                }

                // if right to left sweep event on screen
                if (x1 > x2)
                {
                    //Toast.makeText(this, "Right to Left Swap Performed", Toast.LENGTH_LONG).show();
                    finish();
                }

                // if UP to Down sweep event on screen
                if (y1 < y2)
                {
                    //Toast.makeText(this, "UP to Down Swap Performed", Toast.LENGTH_LONG).show();
                    finish();
                }

                //if Down to UP sweep event on screen
                if (y1 > y2)
                {
                    //Toast.makeText(this, "Down to UP Swap Performed", Toast.LENGTH_LONG).show();
                    finish();
                }
                break;
            }
        }
        return false;
    }

    public void StoreTapCtr(String ctr){

        SharedPreferences pref = getApplicationContext().getSharedPreferences(getResources().getString(R.string.ctr_sharedpref_file), Context.MODE_MULTI_PROCESS);
        SharedPreferences.Editor log_editor =pref.edit();
        log_editor.putString(getResources().getString(R.string.tap_ctr), ctr);
        log_editor.apply();
        log_editor.commit();
    }

    public String RetrieveTapCtr(){

        String ctr="000000";

        try {

            Context con = getApplicationContext().createPackageContext(getResources().getString(R.string.ctr_pkg), Context.CONTEXT_IGNORE_SECURITY);
            SharedPreferences pref = con.getSharedPreferences(getResources().getString(R.string.ctr_sharedpref_file), Context.MODE_MULTI_PROCESS);

            ctr = pref.getString(getResources().getString(R.string.tap_ctr), "000000");
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return ctr;
    }

    public void StoreMoodCtr(String ctr){

        SharedPreferences pref = getApplicationContext().getSharedPreferences(getResources().getString(R.string.ctr_sharedpref_file), Context.MODE_MULTI_PROCESS);
        SharedPreferences.Editor log_editor =pref.edit();
        log_editor.putString(getResources().getString(R.string.mood_ctr), ctr);
        log_editor.apply();
        log_editor.commit();
    }

    public String RetrieveMoodCtr(){

        String ctr="000000";

        try {

            Context con = getApplicationContext().createPackageContext(getResources().getString(R.string.ctr_pkg), Context.CONTEXT_IGNORE_SECURITY);
            SharedPreferences pref = con.getSharedPreferences(getResources().getString(R.string.ctr_sharedpref_file), Context.MODE_MULTI_PROCESS);

            ctr = pref.getString(getResources().getString(R.string.mood_ctr), "000000");
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return ctr;
    }

    public void StoreSessionFileCtr(String ctr){

        SharedPreferences pref = getApplicationContext().getSharedPreferences(getResources().getString(R.string.ctr_sharedpref_file), Context.MODE_MULTI_PROCESS);
        SharedPreferences.Editor log_editor =pref.edit();
        log_editor.putString(getResources().getString(R.string.session_file_ctr), ctr);
        log_editor.apply();
        log_editor.commit();
    }

    public String RetrieveSessionFileCtr(){

        String ctr="000000";

        try {

            Context con = getApplicationContext().createPackageContext(getResources().getString(R.string.ctr_pkg), Context.CONTEXT_IGNORE_SECURITY);
            SharedPreferences pref = con.getSharedPreferences(getResources().getString(R.string.ctr_sharedpref_file), Context.MODE_MULTI_PROCESS);

            ctr = pref.getString(getResources().getString(R.string.session_file_ctr), "000000");
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return ctr;
    }

    public void StoretypingSession(String session_no){

        SharedPreferences pref = getApplicationContext().getSharedPreferences(getResources().getString(R.string.typing_session_sharedpref_file), Context.MODE_MULTI_PROCESS);
        SharedPreferences.Editor log_editor =pref.edit();
        log_editor.putString(getResources().getString(R.string.typing_session_no), session_no);
        log_editor.apply();
        log_editor.commit();
    }

    public String RetrieveTypingSession(){

        String session="000001";

        try {

            Context con = getApplicationContext().createPackageContext(getResources().getString(R.string.typing_session_pkg), Context.CONTEXT_IGNORE_SECURITY);
            SharedPreferences pref = con.getSharedPreferences(getResources().getString(R.string.typing_session_sharedpref_file), Context.MODE_MULTI_PROCESS);

            session = pref.getString(getResources().getString(R.string.typing_session_no), "000001");
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return session;
    }

    public float find_time_diff (Date dt1, Date dt2){

        float diff = dt1.getTime() - dt2.getTime();

        long secondsInMilli = 1000;
        long minutesInMilli = secondsInMilli * 60;

        float diff_in_min = diff / minutesInMilli;

        System.out.println("Time Diff in Minutes:"+diff_in_min);

        return diff_in_min;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_mood_recorder, menu);
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
