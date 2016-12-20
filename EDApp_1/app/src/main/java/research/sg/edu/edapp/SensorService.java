package research.sg.edu.edapp;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Environment;
import android.os.PowerManager;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SensorService extends IntentService implements SensorEventListener {

    private SensorManager sensorManager;

    private float x_ac,y_ac,z_ac;
    private float x_gy,y_gy,z_gy;
    private float x_ln_ac,y_ln_ac,z_ln_ac;
    private float intn_light;
    private float proximity_val;

    private NoiseLevel mSensor;
    private double noise_amp;



    public SensorService() {
        super("SensorService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        register_listeners();
        getsensor_readings();
        deregister_listeners();
    }

    public void register_listeners(){

        System.out.println("[SensorService]:Registering listeners...");

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        // Register listeners

        Sensor AcSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if(AcSensor != null) {

            sensorManager.registerListener(this,
                    sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                    SensorManager.SENSOR_DELAY_NORMAL);
        }

        Sensor GyroSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        if(GyroSensor != null) {

            sensorManager.registerListener(this,
                    sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE),
                    SensorManager.SENSOR_DELAY_NORMAL);
        }

        Sensor LinAcSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        if(LinAcSensor != null) {

            sensorManager.registerListener(this,
                    sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION),
                    SensorManager.SENSOR_DELAY_NORMAL);
        }


        Sensor LightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        if(LightSensor != null) {

            sensorManager.registerListener(this,
                    sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT),
                    SensorManager.SENSOR_DELAY_NORMAL);
        }

        Sensor ProximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        if(ProximitySensor != null) {

            sensorManager.registerListener(this,
                    sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY),
                    SensorManager.SENSOR_DELAY_NORMAL);
        }

        mSensor = new NoiseLevel();
        mSensor.start();
    }

    public void deregister_listeners(){

        System.out.println("[SensorService]:De-registering listeners...");

        sensorManager.unregisterListener(this);
        mSensor.stop();
    }

    public void getsensor_readings(){

        long interval;
        long collection_duration;

        interval=250;               // Start collecting sensor data, one sample per 250 milliseconds

        long sensor_collection_interval = Integer.parseInt(getResources().getString(R.string.sensor_collection_interval));
        collection_duration= 1000 * 60 * sensor_collection_interval;


        int counter=0;
        long max_no_of_sensor_reading=0;

        max_no_of_sensor_reading=collection_duration / interval;

        while (true) {

            if (counter == max_no_of_sensor_reading) {
                break;
            }

            try {

                noise_amp=mSensor.getAmplitude();
                counter = counter + 1;
                WriteSensorReading(counter);
                System.out.println("[Sensor]: counter="+counter);
                Thread.sleep(interval);

            } catch (InterruptedException e) {

                e.printStackTrace();
            }
        }
    }

    public void WriteSensorReading(int seq_no) {

        String file_ctr;

        File sdCardRoot = Environment.getExternalStorageDirectory();
        File dataDir = new File(sdCardRoot, getResources().getString(R.string.data_file_path));

        String string_t = null, imei_no, sensor_file_name;

        if (!dataDir.exists()) {
            dataDir.mkdirs();
        }

        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        imei_no = (String) telephonyManager.getDeviceId();

        file_ctr=RetrieveSensorCtr();

        sensor_file_name = imei_no + "_" + file_ctr + getResources().getString(R.string.sensor_file_postfix);

        File sensor_file = new File(dataDir, sensor_file_name);

        try {
            SimpleDateFormat sdf = new SimpleDateFormat(getResources().getString(R.string.time_format));
            String currentDateandTime = sdf.format(new Date());

            string_t = seq_no + "," + currentDateandTime + "," + x_ac + "," + y_ac + "," + z_ac + "," + x_gy + "," + y_gy + "," + z_gy + "," + x_ln_ac + "," + y_ln_ac + "," + z_ln_ac + "," + intn_light + "," + proximity_val + "," + noise_amp + "\n";
            FileOutputStream fos = new FileOutputStream(sensor_file, true);
            fos.write(string_t.getBytes());
            fos.close();
        } catch (Exception e) {
            Log.d("EXCEPTION", e.getMessage());
        }

        int sensor_file_size = Integer.parseInt(String.valueOf(sensor_file.length() / 1024));
        int sensor_file_size_threshold = Integer.parseInt(getResources().getString(R.string.sensor_file_size_limit));


        if (sensor_file_size > sensor_file_size_threshold) {
            int ctr = Integer.parseInt(file_ctr) + 1;
            file_ctr=String.valueOf(ctr);
            file_ctr=String.format("%06d", Integer.parseInt(file_ctr));

            move_file(sensor_file_name);
        }
        StoreSensorCtr(file_ctr);
    }

    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public void onSensorChanged(SensorEvent event) {

        // check sensor type
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {

            // assign directions
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            x_ac = x;
            y_ac = y;
            z_ac = z;

            //System.out.println("X=" + x + ", Y=" + y + ", Z=" + z);

            //xCoor.setText("X: "+x);
            //yCoor.setText("Y: "+y);
            //zCoor.setText("Z: "+z);
        }

        if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {

            // assign directions
            x_gy = event.values[0];
            y_gy = event.values[1];
            z_gy = event.values[2];

        }

        if (event.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {

            // assign directions
            x_ln_ac = event.values[0];
            y_ln_ac = event.values[1];
            z_ln_ac = event.values[2];

        }

        if (event.sensor.getType() == Sensor.TYPE_LIGHT) {

            intn_light = event.values[0];
        }

        if (event.sensor.getType() == Sensor.TYPE_PROXIMITY) {

            proximity_val = event.values[0];
        }
    }

    public void StoreSensorCtr(String ctr){

        SharedPreferences pref = getApplicationContext().getSharedPreferences(getResources().getString(R.string.ctr_sharedpref_file), Context.MODE_MULTI_PROCESS);
        SharedPreferences.Editor log_editor =pref.edit();
        log_editor.putString(getResources().getString(R.string.sensor_ctr), ctr);
        log_editor.apply();
        log_editor.commit();
    }

    public String RetrieveSensorCtr(){

        String ctr="000000";

        try {

            Context con = getApplicationContext().createPackageContext(getResources().getString(R.string.ctr_pkg), Context.CONTEXT_IGNORE_SECURITY);
            SharedPreferences pref = con.getSharedPreferences(getResources().getString(R.string.ctr_sharedpref_file), Context.MODE_MULTI_PROCESS);

            ctr = pref.getString(getResources().getString(R.string.sensor_ctr), "000000");
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return ctr;
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
