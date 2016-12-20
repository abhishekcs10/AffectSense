package research.sg.edu.edapp.kb;

import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import android.view.MotionEvent;
import android.view.GestureDetector;
import android.support.v4.view.GestureDetectorCompat;
import android.widget.RelativeLayout;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import research.sg.edu.edapp.R;

public class Swype extends AppCompatActivity implements GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener {

    private TextView ms;
    private GestureDetectorCompat gest;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_swype);
        ms = (TextView) findViewById(R.id.swype);
        this.gest = new GestureDetectorCompat(this, this);
        gest.setOnDoubleTapListener(this);
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }


    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {
        ms.setText("onSingleTapConfirmed");
        printSamples(e);
        return true;
    }

    @Override
    public boolean onDoubleTap(MotionEvent e) {
        ms.setText("onDoubleTap");
        printSamples(e);
        return true;
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent e) {
        ms.setText("onDoubleTapEvent");
        printSamples(e);
        return true;
    }

    @Override
    public boolean onDown(MotionEvent e) {
        ms.setText("onDown");
        printSamples(e);
        return true;
    }

    @Override
    public void onShowPress(MotionEvent e) {
        ms.setText("onShowPress");
        printSamples(e);
    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        ms.setText("onSingleTapUp");
        printSamples(e);
        return true;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        ms.setText("onScroll");
        printSamples(e1,e2);
        //printSamples(e2);
        return true;
    }

    @Override
    public void onLongPress(MotionEvent e) {
        ms.setText("onLongPress");
        printSamples(e);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        this.gest.onTouchEvent(event);
        return super.onTouchEvent(event);
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        ms.setText("onFling");
        printSamples(e1,e2);
        //printSamples(e2);
        return true;


    }

    void printSamples(MotionEvent ev) {
        final int historySize = ev.getHistorySize();
        final int pointerCount = ev.getPointerCount();
        setContentView(R.layout.activity_swype);
        TextView display=(TextView)findViewById(R.id.showMotion);
        String show="";
        for (int h = 0; h < historySize; h++) {
            //System.out.printf("At time %d:", ev.getHistoricalEventTime(h));

            for (int p = 0; p < pointerCount; p++) {
                show+=String.format("  pointer %d: (%f,%f)",
                        ev.getPointerId(p), ev.getHistoricalX(p, h), ev.getHistoricalY(p, h));
            }
        }
        show+=String.format("At time %d:", ev.getEventTime());
        for (int p = 0; p < pointerCount; p++) {
            show+=System.out.printf("  pointer %d: (%f,%f)",
                    ev.getPointerId(p), ev.getX(p), ev.getY(p));
        }
        display.setText(show);
    }

    void printSamples(MotionEvent ev,MotionEvent ev2) {
        final int historySize = ev.getHistorySize();
        final int pointerCount = ev.getPointerCount();
        setContentView(R.layout.activity_swype);
        TextView display=(TextView)findViewById(R.id.showMotion);
        String show="";
        for (int h = 0; h < historySize; h++) {
            //System.out.printf("At time %d:", ev.getHistoricalEventTime(h));

            for (int p = 0; p < pointerCount; p++) {
                show+=String.format("  pointer %d: (%f,%f)",
                        ev.getPointerId(p), ev.getHistoricalX(p, h), ev.getHistoricalY(p, h));
            }
        }
        show+=String.format("At time %d:", ev.getEventTime());
        for (int p = 0; p < pointerCount; p++) {
            show+=System.out.printf("  pointer %d: (%f,%f)",
                    ev.getPointerId(p), ev.getX(p), ev.getY(p));
        }
        show+="\n";
        for (int h = 0; h < historySize; h++) {
            //System.out.printf("At time %d:", ev.getHistoricalEventTime(h));

            for (int p = 0; p < pointerCount; p++) {
                show+=String.format("  pointer %d: (%f,%f)",
                        ev2.getPointerId(p), ev2.getHistoricalX(p, h), ev2.getHistoricalY(p, h));
            }
        }
        show+=String.format("At time %d:", ev.getEventTime());
        for (int p = 0; p < pointerCount; p++) {
            show+=System.out.printf("  pointer %d: (%f,%f)",
                    ev2.getPointerId(p), ev2.getX(p), ev2.getY(p));
        }
        display.setText(show);
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Swype Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://research.sg.edu.edapp.kb/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Swype Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://research.sg.edu.edapp.kb/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        client.disconnect();
    }
}
