package research.sg.edu.edapp;

import android.provider.BaseColumns;

/**
 * Created by a on 20-05-2016.
 */
public final class EsmDetail {
    // To prevent someone from accidentally instantiating the contract class,
    // give it an empty constructor.
    public EsmDetail() {
    }

    /* Inner class that defines the table contents */
    public static abstract class EsmEntry implements BaseColumns {

        public static final String ESM_TABLE_NAME = "EsmDetail";
        public static final String ESM_APP_NAME = "AppName";
        public static final String ESM_TIMESTAMP = "TimeStamp";
        //public static final String ESM_KEY = "Key";
    }
}
