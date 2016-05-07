package edu.swust.weather.application;

import android.app.Activity;
import android.support.design.widget.FloatingActionButton;
import android.util.Log;

import com.baidu.speechsynthesizer.SpeechSynthesizer;
import com.baidu.speechsynthesizer.SpeechSynthesizerListener;
import com.baidu.speechsynthesizer.publicutility.SpeechError;

import butterknife.Bind;
import butterknife.ButterKnife;
import edu.swust.weather.R;
import edu.swust.weather.utils.SnackbarUtils;
import edu.swust.weather.utils.SystemUtils;

public class SpeechListener implements SpeechSynthesizerListener {
    private static final String TAG = "SpeechListener";
    private Activity activity;
    @Bind(R.id.fab_speech)
    FloatingActionButton fabSpeech;

    public SpeechListener(Activity activity) {
        this.activity = activity;
        ButterKnife.bind(this, this.activity);
    }

    @Override
    public void onStartWorking(SpeechSynthesizer speechSynthesizer) {
        Log.d(TAG, "onStartWorking");
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                fabSpeech.setEnabled(false);
                SystemUtils.voiceAnimation(fabSpeech, true);
            }
        });
    }

    @Override
    public void onSpeechStart(SpeechSynthesizer speechSynthesizer) {
        Log.d(TAG, "onSpeechStart");
    }

    @Override
    public void onNewDataArrive(SpeechSynthesizer speechSynthesizer, byte[] bytes, boolean b) {
    }

    @Override
    public void onBufferProgressChanged(SpeechSynthesizer speechSynthesizer, int i) {
    }

    @Override
    public void onSpeechProgressChanged(SpeechSynthesizer speechSynthesizer, int i) {
    }

    @Override
    public void onSpeechPause(SpeechSynthesizer speechSynthesizer) {
    }

    @Override
    public void onSpeechResume(SpeechSynthesizer speechSynthesizer) {
    }

    @Override
    public void onCancel(SpeechSynthesizer speechSynthesizer) {
    }

    @Override
    public void onSynthesizeFinish(SpeechSynthesizer speechSynthesizer) {
        Log.d(TAG, "onSynthesizeFinish");
    }

    @Override
    public void onError(SpeechSynthesizer speechSynthesizer, final SpeechError speechError) {
        Log.e(TAG, "SpeechError:" + speechError.errorCode + "," + speechError.errorDescription);
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                SnackbarUtils.show(fabSpeech, speechError.errorDescription);
            }
        });
    }

    @Override
    public void onSpeechFinish(SpeechSynthesizer speechSynthesizer) {
        Log.d(TAG, "onSpeechFinish");
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                fabSpeech.setEnabled(true);
                SystemUtils.voiceAnimation(fabSpeech, false);
            }
        });
    }

    public void release() {
        fabSpeech = null;
        activity = null;
    }
}
