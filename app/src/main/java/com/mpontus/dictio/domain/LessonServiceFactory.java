package com.mpontus.dictio.domain;

import com.mpontus.dictio.data.model.Prompt;
import com.mpontus.dictio.device.PlaybackService;
import com.mpontus.dictio.device.VoiceService;

public class LessonServiceFactory {

    private final PlaybackService playbackService;

    private final VoiceService voiceService;


    public LessonServiceFactory(PlaybackService playbackService, VoiceService voiceService) {
        this.playbackService = playbackService;
        this.voiceService = voiceService;
    }

    public LessonService createLessonService(Prompt prompt) {
        return new LessonService(playbackService, voiceService, prompt);
    }
}
