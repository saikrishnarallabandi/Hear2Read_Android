package org.hear2read.telugu;

/**
 * Created by saikrishnalticmu on 7/6/17.
 */

import java.io.File;

import android.content.Context;
import android.util.Log;

public class NativeFliteTTS {
    private final static String LOG_TAG = "Flite_Java_" + NativeFliteTTS.class.getSimpleName();

    static {
        System.loadLibrary("ttsflite");
        nativeClassInit();
    }

    private final Context mContext;
    private final SynthReadyCallback mCallback;
    private final String mDatapath;
    private boolean mInitialized = false;

    public NativeFliteTTS(Context context, SynthReadyCallback callback) {

        // FliteManager fm = new FliteManager();
        // fm.copyFileOrDir("file:///android_asset/cg");

        mDatapath = new File(Voice.getDataStorageBasePath()).getParent();
        mContext = context;
        mCallback = callback;
        attemptInit();
    }

    @Override
    protected void finalize() {
        nativeDestroy();
    }

    public int isLanguageAvailable(String language, String country,	String variant) {
        return nativeIsLanguageAvailable(language, country, variant);
    }

    public boolean setLanguage(String language, String country, String variant) {
        attemptInit();
        return nativeSetLanguage(language, country, variant);
    }

    public int getSampleRate() {
        return nativeGetSampleRate();
    }

    public boolean setSpeechRate(int rate) {
        return nativeSetSpeechRate(rate);
    }

    public void synthesize(String text) {
        nativeSynthesize(text);
    }

    public void stop() {
        nativeStop();
    }

    public float getNativeBenchmark() {
        return nativeGetBenchmark();
    }

    private void nativeSynthCallback(byte[] audioData) {
        if (mCallback == null)
            return;

        if (audioData == null) {
            mCallback.onSynthDataComplete();
        } else {
            mCallback.onSynthDataReady(audioData);
        }
    }

    private void attemptInit() {
        if (mInitialized) {
            return;
        }

        if (!nativeCreate(mDatapath)) {
            Log.e(LOG_TAG, "Failed to initialize flite library");
            return;
        }
        Log.i(LOG_TAG, "Initialized Flite");
        mInitialized = true;

    }

    private int mNativeData;
    private static native final boolean nativeClassInit();
    private native final boolean nativeCreate(String path);
    private native final boolean nativeDestroy();
    private native final int nativeIsLanguageAvailable(String language, String country, String variant);
    private native final boolean nativeSetLanguage(String language, String country, String variant);
    private native final boolean nativeSetSpeechRate(int rate);
    private native final int nativeGetSampleRate();
    private native final boolean nativeSynthesize(String text);
    private native final boolean nativeStop();

    private native final float nativeGetBenchmark();


    public interface SynthReadyCallback {
        void onSynthDataReady(byte[] audioData);

        void onSynthDataComplete();
    }


}

