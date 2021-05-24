package ru.konovalovk.subtitle_parser.dictionary.remote;

import android.content.Context;

/**
 * Created by habib on 7/18/17.
 */

public class DictionaryApi {
    public static final String BASE_URL = "https://glosbe.com/";

    public static GlosbeService getGlosbeService(Context context){
        return RetrofitClient.getClient(context, BASE_URL).create(GlosbeService.class);

    }
}
