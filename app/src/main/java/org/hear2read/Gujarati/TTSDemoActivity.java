/*************************************************************************/
/*                                                                       */
/*                  Language Technologies Institute                      */
/*                     Carnegie Mellon University                        */
/*                         Copyright (c) 2012                            */
/*                        All Rights Reserved.                           */
/*                                                                       */
/*  Permission is hereby granted, free of charge, to use and distribute  */
/*  this software and its documentation without restriction, including   */
/*  without limitation the rights to use, copy, modify, merge, publish,  */
/*  distribute, sublicense, and/or sell copies of this work, and to      */
/*  permit persons to whom this work is furnished to do so, subject to   */
/*  the following conditions:                                            */
/*   1. The code must retain the above copyright notice, this list of    */
/*      conditions and the following disclaimer.                         */
/*   2. Any modifications must be clearly marked as such.                */
/*   3. Original authors' names are not deleted.                         */
/*   4. The authors' names are not used to endorse or promote products   */
/*      derived from this software without specific prior written        */
/*      permission.                                                      */
/*                                                                       */
/*  CARNEGIE MELLON UNIVERSITY AND THE CONTRIBUTORS TO THIS WORK         */
/*  DISCLAIM ALL WARRANTIES WITH REGARD TO THIS SOFTWARE, INCLUDING      */
/*  ALL IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS, IN NO EVENT   */
/*  SHALL CARNEGIE MELLON UNIVERSITY NOR THE CONTRIBUTORS BE LIABLE      */
/*  FOR ANY SPECIAL, INDIRECT OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES    */
/*  WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN   */
/*  AN ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION,          */
/*  ARISING OUT OF OR IN CONNECTION WITH THE USE OR PERFORMANCE OF       */
/*  THIS SOFTWARE.                                                       */
/*                                                                       */
/*************************************************************************/
/*             Author:  Alok Parlikar (aup@cs.cmu.edu)                   */
/*               Date:  July 2012                                        */
/*************************************************************************/
package org.hear2read.Gujarati;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Locale;

public class TTSDemoActivity extends AppCompatActivity implements OnClickListener, OnKeyListener, OnInitListener {
	private final static String LOG_TAG = "Flite_Java_" + TTSDemoActivity.class.getSimpleName();

	private ListAdapter mAdapter;
	private ListView mList;
	private Handler mHandler = new Handler();
	private boolean mFinishedStart = false;

	private Runnable mRequestFocus = new Runnable() {
		public void run() {
			mList.focusableViewAvailable(mList);
		}
	};

	private EditText mUserText;
	private ImageButton mSendButton;
	private ArrayAdapter<String> mArrayAdapter;
	private ArrayAdapter<String> mVoiceAdapter;
	/*private ArrayAdapter<String> mRateAdapter;*/
	private ArrayList<Voice> mVoices;
	private ArrayList<String> mStrings = new ArrayList<String>();
	private ArrayList<String> mRates = new ArrayList<String>();
	/*private Spinner mVoiceSpinner;
    private Spinner mRateSpinner;*/
	private TextToSpeech mTts;
	private int mSelectedVoice;
	private String mDebugText;

	@TargetApi(17)
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ArrayList<Voice> allVoices = CheckVoiceData.getVoices();

		/*int localeno = Locale.getAvailableLocales().length;

		Log.d(LOG_TAG, "onCreate: No. of locales = " + localeno);*/

		mVoices = new ArrayList<Voice>();
		for(Voice vox:allVoices) {
			if (vox.isAvailable()) {
				mVoices.add(vox);
				mDebugText = vox.getDebugText();
				Log.d(LOG_TAG, "onCreate: Added voice: " + vox.getName());
			}
		}

		if (mVoices.isEmpty()) {
			// We can't demo anything if there are no voices installed.
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage("Gujarati Hear2Read voices not installed. Please add voices in order to run the demo");
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
			/* Unnecessary
			if (android.os.Build.VERSION.SDK_INT >=
					android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH) {*/
				Log.v(LOG_TAG, "onCreate: Attempting to create new TextToSpeech instance org.hear2read.Gujarati");
				mTts = new TextToSpeech(getApplicationContext(), this, "org.hear2read.Gujarati");
			/*}
			else {
				mTts = new TextToSpeech(getApplicationContext(), this);
			}*/
			mSelectedVoice = -1;

		}
	}

	@Override
	protected void onRestoreInstanceState(Bundle state) {
		ensureList();
		super.onRestoreInstanceState(state);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		mHandler.removeCallbacks(mRequestFocus);
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
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.hear2read_logo);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
		mStrings.add("હીયર-ટુ-રીડ, ટેક્સ્ટ-ટુ-સ્પીચ, સાર્વજનિક સોફ્ટવેરનો ઉપયોગ કરવા બદલ આભાર.");
		mStrings.add("આ સોફ્ટવેર હીયર-ટુ-રીડના સ્વયંસેવકો અને કાર્નેગી મૅલોન યુનિવર્સિટીએ વિકસાવ્યું છે.");
		mStrings.add("આ સોફ્ટવેરમાં સુધારાવધારા માટેનાં સૂચનોને અમે આવકારીએ છીએ.");
		mStrings.add("પ્લે સ્ટોર મૂલ્યાંકન અને સમીક્ષા વ્યવસ્થાનો ઉપયોગ કરી તમે તમારા અભિપ્રાયમાં અન્ય વ્યક્તિઓને સહભાગી બનાવી શકો છો.");

				mArrayAdapter = new InputHistoryAdapter(this, R.layout.list_tts_history, mStrings);

		setListAdapter(mArrayAdapter);

		/*mRates.add("Very Slow");
		mRates.add("Slow");
		mRates.add("Normal Speed");
		mRates.add("Fast");
		mRates.add("Very Fast");

		mRateAdapter = new ArrayAdapter<String>(this,
							android.R.layout.simple_spinner_dropdown_item,
							mRates);*/


		mUserText = (EditText) findViewById(R.id.userText);
		mSendButton = (ImageButton) findViewById(R.id.sendButton);

		/*mVoiceSpinner = (Spinner) findViewById(R.id.voice);
		mVoiceSpinner.setAdapter(mVoiceAdapter);*/

		/*mRateSpinner = (Spinner) findViewById(R.id.speechrate);
		mRateSpinner.setAdapter(mRateAdapter);
		mRateSpinner.setSelection(2);*/

		mUserText.setOnClickListener(this);
		mSendButton.setOnClickListener(this);
		mUserText.setOnKeyListener(this);
	}

	/**
	 * Updates the screen state (current list and other views) when the
	 * content changes.
	 *
	 * @see Activity#onContentChanged()
	 */
	@Override
	public void onContentChanged() {
		super.onContentChanged();
		mList = (ListView)findViewById(android.R.id.list);
		mList.setOnItemClickListener(mOnClickListener);
		if (mFinishedStart) {
			setListAdapter(mAdapter);
		}
		mHandler.post(mRequestFocus);
		mFinishedStart = true;
	}

	private void setListAdapter(ListAdapter adapter) {
		synchronized (this) {
			ensureList();
			mAdapter = adapter;
			mList.setAdapter(adapter);
		}
	}

	private void ensureList() {
		if (mList != null) {
			return;
		}
		setContentView(R.layout.activity_tts_demo);
	}

	public void onClick(View v) {
		sendText();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Switch to Browse folders / Browse by type
		// Future: browse and open from GDrive...
		getMenuInflater().inflate(R.menu.manager_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int i = item.getItemId();
		if (i == R.id.action_contact) {
			sendEmail("feedback@Hear2Read.org", getString(R.string.email_subject), "");
			return true;
		}
		else if (i == R.id.action_about) {
			Intent intent = new Intent(this, FliteInfoActivity.class);
			startActivity(intent);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private void sendEmail(String emailTo, String emailSubject, String emailContent)
	{
		Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
				"mailto",emailTo, null));
		emailIntent.putExtra(Intent.EXTRA_SUBJECT, emailSubject);
		emailIntent.putExtra(Intent.EXTRA_TEXT, emailContent);
		startActivity(Intent.createChooser(emailIntent, getString(R.string.email_client)));
	}

	private void sendText() {
		String text = mUserText.getText().toString();
		if (text.isEmpty())
			return;
		mArrayAdapter.add(text);
		mUserText.setText(null);
		sayText(text);
	}

	private void sayText(String text) {
		Log.v(LOG_TAG, "Speaking: " + text);
		/*int currentVoiceID = mVoiceSpinner.getSelectedItemPosition();
		if (currentVoiceID != mSelectedVoice) {*/
		int currentVoiceID = 0;
		mSelectedVoice = currentVoiceID;
			Voice v = mVoices.get(currentVoiceID);
			mTts.setLanguage(v.getLocale());
		/*}*/

		/*int currentRate = mRateSpinner.getSelectedItemPosition();
		mTts.setSpeechRate((float)(currentRate + 1)/3);*/

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
			Log.d(LOG_TAG, "onInit: First if statement");
			success = false;
		}

		if (success &&
				(android.os.Build.VERSION.SDK_INT >=
				android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH)) {
			status = mTts.setEngineByPackageName("org.hear2read.Gujarati");
			Log.v(LOG_TAG, "Flite set as TTS engine by package name? " + status);
		}

		if (status == TextToSpeech.ERROR) {
			success = false;
		}

		// REALLY check that it is flite engine that has been initialized
		// This is done using a hack, for now, since for API < 14
		// there seems to be no way to check which engine is being used.

		if (mTts.isLanguageAvailable(new Locale("guj", "IND"))
				!= TextToSpeech.LANG_COUNTRY_AVAILABLE) {
			Log.d(LOG_TAG, "The hack for checking flite engine through is_flite_available fails");
			success = false;
		}

		Log.d(LOG_TAG, "onInit: Do logs from this region get displayed at all?");

		if (!success) {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage("Gujarati Hear2Read Engine could not be initialized. Check that Gujarati Hear2Read is enabled on your phone! " +
					"In some cases, you may have to select Gujarati Hear2Read as the default engine.");
			builder.setNegativeButton("Open TTS Settings", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
//					dialog.cancel();
//					Intent intent = new Intent();
//					intent.setComponent(new ComponentName("com.android.settings", "com.android.settings.TextToSpeechSettings"));
//			        startActivity(intent);
					// GKochaniak modified for Android 4+
					Intent intent = new Intent();
					intent.setAction("com.android.settings.TTS_SETTINGS");
					intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
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

    private AdapterView.OnItemClickListener mOnClickListener = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView<?> parent, View v, int position, long id)
        {
            String text = (String) parent.getItemAtPosition(position);
            sayText(text);
        }
    };
}
