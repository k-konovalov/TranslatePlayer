
package ru.konovalovk.subtitle_parser.dictionary.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Phrase {

    @SerializedName("text")
    @Expose
    private String text;
    @SerializedName("language")
    @Expose
    private String language;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

}
