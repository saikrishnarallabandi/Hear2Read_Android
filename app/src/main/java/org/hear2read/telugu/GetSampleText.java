package org.hear2read.telugu;

/**
 * Created by saikrishnalticmu on 7/6/17.
 */

import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.speech.tts.TextToSpeech;
import android.util.Log;

import org.hear2read.telugu.R;

import java.util.Locale;

/*
 * Returns the sample text string for the language requested
 */
public class GetSampleText extends Activity {
    private final static String LOG_TAG = "Flite_Java_" + GetSampleText.class.getSimpleName();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Locale locale = getLocaleFromIntent(getIntent());

        final String language = (locale == null) ? "eng" : locale.getISO3Language();

        int result = TextToSpeech.LANG_AVAILABLE;
        Intent returnData = new Intent();

        if (language.equals("eng")) {
            returnData.putExtra("sampleText", getString(R.string.eng_sample));
            Log.v(LOG_TAG, "Returned SampleText: " + getString(R.string.eng_sample));

        } else if  (language.equals("tel")) {
            returnData.putExtra("sampleText", getString(R.string.indic_sample));
            Log.v(LOG_TAG, "Returned SampleText: " + getString(R.string.indic_sample));

        } else {

            Log.v(LOG_TAG, "Unsupported Language: " + language);
            result = TextToSpeech.LANG_NOT_SUPPORTED;
            returnData.putExtra("sampleText", "");
        }

        setResult(result, returnData);

        finish();
    }

    private static Locale getLocaleFromIntent(Intent intent) {
        if (intent != null) {
            final String language = intent.getStringExtra("language");

            if (language != null) {
                return new Locale(language);
            }
        }

        return Locale.getDefault();
    }
}
