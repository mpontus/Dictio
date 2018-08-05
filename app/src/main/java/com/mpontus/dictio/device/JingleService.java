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

    private final int audioSessionId;

    private final MediaPlayer enterJingle;

    private final MediaPlayer exitJingle;

    private final MediaPlayer successJingle;

    @Inject
    public JingleService(AudioManager audioManager, Resources resources) {
        this.resources = resources;

        audioSessionId = audioManager.generateAudioSessionId();
        enterJingle = createMediaPlayer(R.raw.enter, 0.14f);
        exitJingle = createMediaPlayer(R.raw.exit, 0.04f);
        successJingle = createMediaPlayer(R.raw.success, 0.1f);
    }

    public void playSuccess() {
        successJingle.start();
    }

    public void playEnter() {
        enterJingle.start();
    }

    public void playExit() {
        exitJingle.start();
    }

    private MediaPlayer createMediaPlayer(int resId, float volume) {
        AssetFileDescriptor fd = resources.openRawResourceFd(resId);

        try {
            MediaPlayer mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioSessionId(audioSessionId);
            mediaPlayer.setDataSource(fd.getFileDescriptor(), fd.getStartOffset(), fd.getLength());
            mediaPlayer.setVolume(volume, volume);
            mediaPlayer.prepare();
            fd.close();

            return mediaPlayer;
        } catch (IOException e) {
            Timber.e(e);

            return null;
        } finally {
        }
    }
}
