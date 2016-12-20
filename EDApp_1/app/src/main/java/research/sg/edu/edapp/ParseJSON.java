package research.sg.edu.edapp;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ParseJSON {
    public static String[] ids;
    public static String[] emotion_times;
    public static String[] emotion;

    public static final String JSON_ARRAY = "result";
    public static final String KEY_ID = "imei_number";
    public static final String KEY_EMOTION_TIME = "emotion_time";
    public static final String KEY_EMOTION = "emotion";

    private JSONArray users = null;

    private String json;

    public ParseJSON(String json){
        this.json = json;
    }

    protected void parseJSON(){
        JSONObject jsonObject=null;
        try {
            jsonObject = new JSONObject(json);
            users = jsonObject.getJSONArray(JSON_ARRAY);

            ids = new String[users.length()];
            emotion_times = new String[users.length()];
            emotion = new String[users.length()];

            for(int i=0;i<users.length();i++){
                JSONObject jo = users.getJSONObject(i);
                ids[i] = jo.getString(KEY_ID);
                emotion_times[i] = jo.getString(KEY_EMOTION_TIME);
                emotion[i] = jo.getString(KEY_EMOTION);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}