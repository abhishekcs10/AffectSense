package research.sg.edu.edapp.kb;

import android.provider.BaseColumns;

public final class KbTouchEvent {
    // To prevent someone from accidentally instantiating the contract class,
    // give it an empty constructor.
    public KbTouchEvent() {
    }

    /* Inner class that defines the table contents */
    public static abstract class TouchEntry implements BaseColumns {

        public static final String TE_TABLE_NAME = "TouchEvent";
        public static final String TE_APP_NAME = "AppName";
        public static final String TE_TIMESTAMP = "TimeStamp";
        public static final String TE_KEY = "Key";
    }
}