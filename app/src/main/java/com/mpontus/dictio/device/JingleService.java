package com.mpontus.dictio.device;

import android.content.res.AssetFileDescriptor;
import android.content.res.Resources;
import android.media.AudioManager;
import android.media.MediaPlayer;

import com.mpontus.dictio.R;

import java.io.IOException;

import javax.inject.Inject;

import timber.log.Timber;

/**
 * A service for playing jingles: small sound bits that react to user action
 */
public class JingleService {
    private final MediaPlayer mediaPlayer = new MediaPlayer();

    private final Resources resources;

    @Inject
    public JingleService(AudioManager audioManager, Resources resources) {
        this.resources = resources;

        mediaPlayer.setAudioSessionId(audioManager.generateAudioSessionId());
    }

    public void playSuccess() {
        play(R.raw.victory, 0.10f);
    }

    public void playIn() {
        play(R.raw.in, 0.14f);
    }

    public void playOut() {
        play(R.raw.out, 0.04f);
    }

    private void play(int resId, float volume) {
        mediaPlayer.reset();
        AssetFileDescriptor fd = resources.openRawResourceFd(resId);

        try {
            mediaPlayer.setDataSource(fd.getFileDescriptor(), fd.getStartOffset(), fd.getLength());
            fd.close();

            mediaPlayer.setVolume(volume, volume);
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (IOException e) {
            Timber.e(e);
        }
    }
}
