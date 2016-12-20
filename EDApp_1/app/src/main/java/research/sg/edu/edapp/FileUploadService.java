package research.sg.edu.edapp;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class FileUploadService extends IntentService {


    public FileUploadService() {
        super("FileUploadService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        UploadData();
    }

    public void UploadData() {

        if(checkInternetConnection()){
            boolean upload_status=new UploadtoFileServer(getApplicationContext()).folderUpload();
        }
    }

    private boolean checkInternetConnection() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mWifi = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        if (mWifi.isConnected()) {

            return true;
        }
        else {

            return false;
        }
    }
}
