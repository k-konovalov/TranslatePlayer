package ru.konovalovk.subtitle_parser.habib;

import java.io.IOException;

/**
 * Created by habib on 7/27/17.
 */

public class NoConnectivityException extends IOException {

    @Override
    public String getMessage() {
        return "No connectivity exception";
    }

}
