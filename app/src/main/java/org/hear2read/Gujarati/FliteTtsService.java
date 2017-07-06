/*************************************************************************/
/*                                                                       */
/*                  Language Technologies Institute                      */
/*                     Carnegie Mellon University                        */
/*                         Copyright (c) 2010                            */
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
/*               Date:  June 2012                                        */
/*************************************************************************/

package org.hear2read.Gujarati;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioFormat;
import android.os.Build;
import android.speech.tts.SynthesisCallback;
import android.speech.tts.SynthesisRequest;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeechService;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

/**
 * Implements the Flite Engine as a TextToSpeechService
 *
 */

/*@TargetApi(17)*/
@SuppressLint("NewApi")
public class FliteTtsService extends TextToSpeechService {
	public static final String FLITE_INITIALIZED = "org.hear2read.Gujarati.FLITE_INITIALIZED";
	private final static String LOG_TAG = "Flite_Java_" + FliteTtsService.class.getSimpleName();
	private NativeFliteTTS mEngine = null;//change

	private static final String DEFAULT_LANGUAGE = "eng";
	private static final String DEFAULT_COUNTRY = "USA";
	private static final String DEFAULT_VARIANT = "male,rms";

	private String mCountry = DEFAULT_COUNTRY;
	private String mLanguage = DEFAULT_LANGUAGE;
	private String mVariant = DEFAULT_VARIANT;
	private ArrayList<Voice> mAvailableVoices;
	private SynthesisCallback mCallback;

	@Override
	public void onCreate() {
		initializeFliteEngine();

		// This calls onIsLanguageAvailable() and must run after Initialization
		super.onCreate();
	}

	private void initializeFliteEngine() {
		if (mEngine != null) {
			mEngine.stop();
			mEngine = null;
		}
		mEngine = new NativeFliteTTS(this, mSynthCallback);


        ArrayList<Voice> allVoices = CheckVoiceData.getVoices();

        mAvailableVoices = new ArrayList<Voice>();
        for(Voice vox:allVoices) {
            if (vox.isAvailable()) {
                mAvailableVoices.add(vox);
                Log.d(LOG_TAG, "onCreate: Added voice: " + vox.getName());
            }
        }

        final Intent intent = new Intent(FLITE_INITIALIZED);
        sendBroadcast(intent);
	}



	@Override
	protected String[] onGetLanguage() {
		Log.v(LOG_TAG, "onGetLanguage");
		return new String[] {
				mLanguage, mCountry, mVariant
		};
	}

	@Override
	protected int onIsLanguageAvailable(String language, String country, String variant) {
		Log.v(LOG_TAG, "onIsLanguageAvailable: " + language + country + variant);
		return mEngine.isLanguageAvailable(language, country, variant);

	}

	@Override
	protected int onLoadLanguage(String language, String country, String variant) {
		Log.v(LOG_TAG, "onIsLanguageAvailable: " + language + country + variant);
		return mEngine.isLanguageAvailable(language, country, variant);
/*
		Log.v(LOG_TAG, "onLoadLanguage: " + language + country + variant);
		int result = onIsLanguageAvailable(language, country, variant);
		if (result >= TextToSpeech.LANG_AVAILABLE) {
			mEngine.setLanguage(language, country, variant);
		}

		return result;
*/
		/*return mEngine.isLanguageAvailable(language, country, variant);*/
	}

    @Override
    protected Set<String> onGetFeaturesForLanguage(String lang, String country, String variant) {
        return new HashSet<String>();
    }

    @Override
    public String onGetDefaultVoiceNameFor(String language, String country, String variant) {
        if(mEngine.isLanguageAvailable(language, country, variant)>=TextToSpeech.LANG_AVAILABLE)
        {
            return (language + "-" + country + "-" + variant);
        }
        return null;
    }

    @Override
    public List<android.speech.tts.Voice> onGetVoices() {
        List<android.speech.tts.Voice> voices = new ArrayList<android.speech.tts.Voice>();
        for (Voice vox : mAvailableVoices) {
            int quality = android.speech.tts.Voice.QUALITY_HIGH;
            int latency = android.speech.tts.Voice.LATENCY_NORMAL;
            Locale locale = new Locale(vox.getLocale().getISO3Language(),
                                    vox.getLocale().getISO3Country(), vox.getLocale().getVariant());
            Set<String> features = onGetFeaturesForLanguage(locale.getLanguage(),
                                                    locale.getCountry(), locale.getVariant());
            voices.add(new android.speech.tts.Voice(vox.getName(), vox.getLocale(),
                                                    quality, latency, false, features));
        }
        return voices;
    }

    @Override
	public int onIsValidVoiceName(String name) {
		for (Voice vox : mAvailableVoices) {
			if (Objects.equals(name, vox.getName()))
				return TextToSpeech.SUCCESS;
		}
		return TextToSpeech.ERROR;

/*
		for (Voice vox : mAvailableVoices) {
            if (Objects.equals(name, vox.getName())) {
                boolean result = mEngine.setLanguage(vox.getLocale().getISO3Language(),
                                        vox.getLocale().getCountry(), vox.getLocale().getVariant());
                if (result)
                    return TextToSpeech.SUCCESS;
                return TextToSpeech.ERROR;
            }
        }
        Log.e(LOG_TAG, "onIsValidVoiceName: " + name + " not found!");
        return TextToSpeech.ERROR;
*/
	}

    @Override
    public int onLoadVoice(String name) {
        for (Voice vox : mAvailableVoices) {
            if (Objects.equals(name, vox.getName()))
                return TextToSpeech.SUCCESS;
        }
        return TextToSpeech.ERROR;
    }

    @Override
	protected void onStop() {
		Log.v(LOG_TAG, "onStop");
		if (mEngine == null) return;
		mEngine.stop();
	}

    @SuppressWarnings("deprecation")
    private String getRequestString(SynthesisRequest request) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return request.getCharSequenceText().toString();
        } else {
            return request.getText();
        }
    }

	@Override
	protected synchronized void onSynthesizeText(
			SynthesisRequest request, SynthesisCallback callback) {
		Log.v(LOG_TAG, "onSynthesize");

		String language = request.getLanguage();
		String country = request.getCountry();
		String variant = request.getVariant();
		String text = getRequestString(request);
		Integer speechrate = request.getSpeechRate();

		boolean result = true;
		/*if(variant == null || variant.length() == 0)
			variant = "female";*/
		if (! ((Objects.equals(mLanguage, language)) &&
				(Objects.equals(mCountry, country)) &&
				(Objects.equals(mVariant, variant)))) {
			result = mEngine.setLanguage(language, country, variant);
			mLanguage = language;
			mCountry = country;
			mVariant = variant;
		}
		else {
			result = true;
		}

		if (!result) {
			Log.e(LOG_TAG, "Could not set language for synthesis");
			return;
		}

		mEngine.setSpeechRate(speechrate);
		
		mCallback = callback;
        Integer rate = new Integer(mEngine.getSampleRate());
        Log.v(LOG_TAG, rate.toString());
		mCallback.start(mEngine.getSampleRate(), AudioFormat.ENCODING_PCM_16BIT, 1);
		mEngine.synthesize(text);
	}

	private final NativeFliteTTS.SynthReadyCallback mSynthCallback = new NativeFliteTTS.SynthReadyCallback() {
        @Override
        public void onSynthDataReady(byte[] audioData) {
            if ((audioData == null) || (audioData.length == 0)) {
                onSynthDataComplete();
                return;
            }

            final int maxBytesToCopy = mCallback.getMaxBufferSize();

            int offset = 0;

            while (offset < audioData.length) {
                final int bytesToWrite = Math.min(maxBytesToCopy, (audioData.length - offset));
                mCallback.audioAvailable(audioData, offset, bytesToWrite);
                offset += bytesToWrite;
            }
        }

        @Override
        public void onSynthDataComplete() {
            mCallback.done();
        }
	};

	/**
	 * Listens for language update broadcasts and initializes the flite engine.
	 */
	private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			initializeFliteEngine();
		}
	};
}
