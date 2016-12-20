package research.sg.edu.edapp;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;


public class DisplayDashboard extends AppCompatActivity {

    Button Submitbtn, Sharebtn;
    public static final String JSON_URL = "http://10.5.18.202/AffectSense/get_emotion_data.php?id=353327065451494&s_dt=1460556722&e_dt=1460659000";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_dashboard);

        Submitbtn=(Button)findViewById(R.id.button1);
        Submitbtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                if(v.getId()==R.id.button1) {

                    finish();
                    sendRequest();

                    //RegisterUser();
                    //StoreRunningStatus(true);
                    //ReceiveConsent();
                }
            }
        });

        Sharebtn=(Button)findViewById(R.id.button2);
        Sharebtn.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {

                if(v.getId()==R.id.button2) {

                    //System.out.println("Stopping the application and all associated services");
                    //StopServices();
                    //ShowKb.setEnabled(true);
                    //Stopbtn.setEnabled(false);

                    ShowShareButtonActivity();
                    finish();
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_display_dashboard, menu);
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

    private void sendRequest(){

        StringRequest stringRequest = new StringRequest(JSON_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        showJSON(response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(DisplayDashboard.this,error.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

    private void showJSON(String json){
        ParseJSON pj = new ParseJSON(json);
        pj.parseJSON();
        //for (int i=0;i<pj.;i++){

        //}
        System.out.println(ParseJSON.ids[1]+ParseJSON.emotion_times[1]+ParseJSON.emotion[1]);
        //CustomList cl = new CustomList(this, ParseJSON.ids,ParseJSON.names,ParseJSON.emails);
        //listView.setAdapter(cl);
    }

    public void ShowShareButtonActivity() {

        Intent intent = new Intent(DisplayDashboard.this, ShareButtonActivity.class);
        startActivity(intent);
    }
}
