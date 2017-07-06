package org.hear2read.telugu;

/**
 * Created by saikrishnalticmu on 7/6/17.
 */

import java.util.ArrayList;
import java.util.Locale;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.hear2read.telugu.R;


public class TTSDemo extends ListActivity implements OnClickListener, OnKeyListener, OnInitListener {
    private final static String LOG_TAG = "Flite_Java_" + TTSDemo.class.getSimpleName();

    private EditText mUserText;
    private ImageButton mSendButton;
    private ArrayAdapter<String> mAdapter;
    private ArrayAdapter<String> mVoiceAdapter;
    private ArrayAdapter<String> mRateAdapter;
    private ArrayList<Voice> mVoices;
    private ArrayList<String> mStrings = new ArrayList<String>();
    private ArrayList<String> mRates = new ArrayList<String>();
    private Spinner mVoiceSpinner;
    private Spinner mRateSpinner;
    private TextToSpeech mTts;
    private int mSelectedVoice;
    private static Context mContext;
    private String mDebugText;


    @TargetApi(17)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        ArrayList<Voice> allVoices = CheckVoiceData.getVoices();
        mVoices = new ArrayList<Voice>();
        for(Voice vox:allVoices) {
            if (vox.isAvailable()) {
                builder.setMessage("Adding voice");
                mVoices.add(vox);
                mDebugText = vox.getDebugText();
                builder.setMessage("Got debug data");

            }
        }
        //Toast toast = Toast.makeText(mContext, , Toast.LENGTH_LONG);
        //toast.show();

        if (mVoices.isEmpty()) {
            // We can't demo anything if there are no voices installed.

            builder.setMessage("Hear2Read telugu voices not installed. Please add voices in order to run the demo");
            builder.setNegativeButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                    finish();
                }
            });
            AlertDialog alert = builder.create();
            alert.show();
        }
        else {
            // Initialize the TTS
            if (android.os.Build.VERSION.SDK_INT >=
                    android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                mTts = new TextToSpeech(this, this, "org.hear2read.telugu");
            }
            else {
                mTts = new TextToSpeech(this, this);
            }
            mSelectedVoice = -1;

        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mTts != null)
            mTts.shutdown();
    }

    private void buildUI() {

        ArrayList<String> voiceNames = new ArrayList<String>();

        for (Voice vox: mVoices) {
            voiceNames.add(vox.getDisplayName()); // vox.getVariant());
        }

        mVoiceAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_dropdown_item,
                voiceNames);


        setContentView(R.layout.activity_tts_demo);
        mStrings.add(" హియర్ టు రీడ్ ఓపెన్ సోర్స్ సాఫ్ట్ వేర్ వాడుతున్నందుకు ధన్యవాదములు.  ");
        mStrings.add(" ఇది   హియర్ టు రీడ్   వాలంటీర్స్  మరియు  కార్నెగీ మెల్లోన్ విశ్వ విద్యాలయం  ద్వారా తయారు చేయబడింది. ");
        mStrings.add(" అభివృద్ధి దిశగా మీ అభిప్రాయాలు తెలియజేయండి. ");
        mStrings.add(" మీ సలహాలు మరియు సూచనలు ప్లే స్టోర్ రేటింగ్స్ మరియు రివ్యూస్ ద్వారా తెలియచేయగలరు.  ");

        mAdapter = new InputHistoryAdapter(this, R.layout.list_tts_history, mStrings);

        setListAdapter(mAdapter);

        mRates.add("Very Slow");
        mRates.add("Slow");
        mRates.add("Normal Speed");
        mRates.add("Fast");
        mRates.add("Very Fast");

        mRateAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_dropdown_item,
                mRates);


        mUserText = (EditText) findViewById(R.id.userText);
        mSendButton = (ImageButton) findViewById(R.id.sendButton);

        mVoiceSpinner = (Spinner) findViewById(R.id.voice);
        mVoiceSpinner.setAdapter(mVoiceAdapter);

        mRateSpinner = (Spinner) findViewById(R.id.speechrate);
        mRateSpinner.setAdapter(mRateAdapter);
        mRateSpinner.setSelection(2);

        mUserText.setOnClickListener(this);
        mSendButton.setOnClickListener(this);
        mUserText.setOnKeyListener(this);
    }

    public void onClick(View v) {
        sendText();
    }

    private void sendText() {
        String text = mUserText.getText().toString();
        if (text.isEmpty())
            return;
        mAdapter.add(text);
        mUserText.setText(null);
        //AlertDialog.Builder builder = new AlertDialog.Builder(this);
        //mDebugText = "Helo";
        //builder.setMessage(mDebugText);

        sayText(mDebugText);
    }

    private void sayText(String text) {
        Log.v(LOG_TAG, "Speaking: " + text);
        int currentVoiceID = mVoiceSpinner.getSelectedItemPosition();
        if (currentVoiceID != mSelectedVoice) {
            mSelectedVoice = currentVoiceID;
            Voice v = mVoices.get(currentVoiceID);
            mDebugText = v.getDebugText();
            mTts.setLanguage(v.getLocale());
        }

        int currentRate = mRateSpinner.getSelectedItemPosition();
        mTts.setSpeechRate((float)(currentRate + 1)/3);

        mTts.speak(mDebugText, TextToSpeech.QUEUE_FLUSH, null);
    }

    public boolean onKey(View v, int keyCode, KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            switch (keyCode) {
                case KeyEvent.KEYCODE_DPAD_CENTER:
                case KeyEvent.KEYCODE_ENTER:
                    sendText();
                    return true;
            }
        }
        return false;
    }

    private class InputHistoryAdapter extends ArrayAdapter<String> {
        private ArrayList<String> items;

        public InputHistoryAdapter(Context context,
                                   int textViewResourceId, ArrayList<String> items) {
            super(context, textViewResourceId, items);
            this.items = items;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                LayoutInflater vi = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = vi.inflate(R.layout.list_tts_history, null);
            }
            String s = items.get(position);
            TextView tt = (TextView) convertView.findViewById(R.id.inputText);
            tt.setText(s);
            return convertView;
        }

    }

    @SuppressWarnings("deprecation")
    @Override
    public void onInit(int status) {
        boolean success = true;
        if (status == TextToSpeech.ERROR) {
            success = false;
        }

        if (success &&
                (android.os.Build.VERSION.SDK_INT >=
                        android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH)) {
            status = mTts.setEngineByPackageName("org.hear2read.telugu");
        }

        if (status == TextToSpeech.ERROR) {
            success = false;
        }

        // REALLY check that it is flite engine that has been initialized
        // This is done using a hack, for now, since for API < 14
        // there seems to be no way to check which engine is being used.

        if (mTts.isLanguageAvailable(new Locale("eng", "USA", "is_flite_available"))
                != TextToSpeech.LANG_COUNTRY_VAR_AVAILABLE) {
            success = false;
        }

        if (!success) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Hear2Read telugu Engine could not be initialized. Check that Hear2Read telugu is enabled on your phone! " +
                    "In some cases, you may have to select Hear2Read telugu as the default engine.");
            builder.setNegativeButton("Open TTS Settings", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                    Intent intent = new Intent();
                    intent.setComponent(new ComponentName("com.android.settings", "com.android.settings.TextToSpeechSettings"));
                    startActivity(intent);
                    finish();
                }
            });
            AlertDialog alert = builder.create();
            alert.show();
        }
        else {
            buildUI();
        }
    }

    @Override
    public void onListItemClick(ListView parent, View view, int position, long id) {
        String text = (String) parent.getItemAtPosition(position);
        sayText(text);

    }
}
