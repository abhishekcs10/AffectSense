package research.sg.edu.edapp;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Environment;
import android.provider.ContactsContract;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.brickred.socialauth.android.DialogListener;
import org.brickred.socialauth.android.SocialAuthAdapter;
import org.brickred.socialauth.android.SocialAuthError;
import org.brickred.socialauth.android.SocialAuthListener;

import java.io.File;
import java.security.Provider;
import java.util.List;


public class ShareButtonActivity extends AppCompatActivity {

    // SocialAuth Component
    SocialAuthAdapter adapter;
    ContactsContract.Profile profileMap;
    List<ContactsContract.CommonDataKinds.Photo> photosList;

    // Android Components
    Button update;
    EditText edit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share_button);

        //super.onCreate(savedInstanceState);

        //setContentView(R.layout.main);

        // Welcome Message
        TextView textview = (TextView) findViewById(R.id.text);
        textview.setText("Welcome to SocialAuth Demo. Connect any provider and then press Update button to Share Update.");

        // Create Your Own Share Button
        Button share = (Button) findViewById(R.id.sharebutton);
        share.setText("Share");
        share.setTextColor(Color.WHITE);
        share.setBackgroundResource(R.drawable.button_gradient);

        // Add it to Library
        adapter = new SocialAuthAdapter(new ResponseListener());

        // Add providers
        adapter.addProvider(SocialAuthAdapter.Provider.FACEBOOK, R.drawable.facebook);
        adapter.addProvider(SocialAuthAdapter.Provider.TWITTER, R.drawable.twitter);
        adapter.addProvider(SocialAuthAdapter.Provider.LINKEDIN, R.drawable.linkedin);
        adapter.addProvider(SocialAuthAdapter.Provider.YAHOO, R.drawable.yahoo);
        adapter.addProvider(SocialAuthAdapter.Provider.YAMMER, R.drawable.yammer);
        adapter.addProvider(SocialAuthAdapter.Provider.EMAIL, R.drawable.email);
        adapter.addProvider(SocialAuthAdapter.Provider.MMS, R.drawable.mms);

        // Providers require setting user call Back url
        adapter.addCallBack(SocialAuthAdapter.Provider.TWITTER, "http://socialauth.in/socialauthdemo/socialAuthSuccessAction.do");
        adapter.addCallBack(SocialAuthAdapter.Provider.YAMMER, "http://socialauth.in/socialauthdemo/socialAuthSuccessAction.do");

        // Enable Provider
        adapter.enable(share);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_share_button, menu);
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

    private final class ResponseListener implements DialogListener {
        @Override
        public void onComplete(Bundle values) {

            Log.d("ShareButton", "Authentication Successful");

            // Get name of provider after authentication
            final String providerName = values.getString(SocialAuthAdapter.PROVIDER);
            Log.d("ShareButton", "Provider Name = " + providerName);
            Toast.makeText(ShareButtonActivity.this, providerName + " connected", Toast.LENGTH_LONG).show();

            update = (Button) findViewById(R.id.update);
            edit = (EditText) findViewById(R.id.editTxt);

            // Please avoid sending duplicate message. Social Media Providers
            // block duplicate messages.

            update.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    adapter.updateStatus(edit.getText().toString(), new MessageListener(), false);
                }
            });

            // Share via Email Intent
            if (providerName.equalsIgnoreCase("share_mail")) {
                // Use your own code here
                Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto",
                        "vineet.aggarwal@3pillarglobal.com", null));
                emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Test");
                File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM),
                        "image5964402.png");
                Uri uri = Uri.fromFile(file);
                emailIntent.putExtra(Intent.EXTRA_STREAM, uri);
                startActivity(Intent.createChooser(emailIntent, "Test"));
            }

            // Share via mms intent
            if (providerName.equalsIgnoreCase("share_mms")) {

                // Use your own code here
                File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM),
                        "image5964402.png");
                Uri uri = Uri.fromFile(file);

                Intent mmsIntent = new Intent(Intent.ACTION_SEND, uri);
                mmsIntent.putExtra("sms_body", "Test");
                mmsIntent.putExtra(Intent.EXTRA_STREAM, uri);
                mmsIntent.setType("image/png");
                startActivity(mmsIntent);
            }

        }

        @Override
        public void onError(SocialAuthError error) {
            Log.d("ShareButton", "Authentication Error: " + error.getMessage());
        }

        @Override
        public void onCancel() {
            Log.d("ShareButton", "Authentication Cancelled");
        }

        @Override
        public void onBack() {
            Log.d("Share-Button", "Dialog Closed by pressing Back Key");
        }

    }

    // To get status of message after authentication
    private final class MessageListener implements SocialAuthListener<Integer> {
        @Override
        public void onExecute(String provider, Integer t) {
            Integer status = t;
            if (status.intValue() == 200 || status.intValue() == 201 || status.intValue() == 204)
                Toast.makeText(ShareButtonActivity.this, "Message posted on " + provider, Toast.LENGTH_LONG).show();
            else
                Toast.makeText(ShareButtonActivity.this, "Message not posted on " + provider, Toast.LENGTH_LONG).show();
        }

        @Override
        public void onError(SocialAuthError e) {

        }
    }
}
