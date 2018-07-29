package com.mpontus.dictio.domain;

import com.mpontus.dictio.data.model.Prompt;
import com.mpontus.dictio.device.PlaybackService;
import com.mpontus.dictio.device.VoiceService;

import java.util.ArrayList;
import java.util.Collection;

import javax.inject.Inject;
import javax.inject.Singleton;

import au.com.ds.ef.AsyncExecutor;
import au.com.ds.ef.EasyFlow;
import au.com.ds.ef.EventEnum;
import au.com.ds.ef.FlowBuilder;
import au.com.ds.ef.StateEnum;
import au.com.ds.ef.StatefulContext;
import au.com.ds.ef.call.DefaultErrorHandler;
import timber.log.Timber;

import static au.com.ds.ef.FlowBuilder.on;

@Singleton
public class LessonService {

    enum States implements StateEnum {
        INITIAL,
        SPEAKER_READY,
        READY,
        IDLE,
        PLAYING_TRANSITION,
        PLAYING,
        RECORDING_TRANSITION,
        RECORDER_PREPARE,
        RECORDING,
    }

    // Events cover any external influence
    enum Events implements EventEnum {
        cardShown,
        cardHidden,
        cardPressed,
        playButtonPressed,
        recordButtonPressed,
        speakerInitialized,
        languageAvailable,
        languageUnavailable,
        recordPermissionGranted,
        recordPermissionDenied,
        recorderInitialized,
        speechEnd,
        recordEnd,
    }

    class FlowContext extends StatefulContext {
        private boolean permissionGranted;

        private boolean permissionDenied;

        private boolean speakerInitialized;

        private boolean recorderInitialized;
    }

    private final FlowContext context = new FlowContext();

    private final ArrayList<Listener> listeners = new ArrayList<>();

    private final PlaybackService.Listener playbackServiceListener = new PlaybackService.Listener() {
        public void onReady() {
            context.speakerInitialized = true;

            flow.safeTrigger(Events.speakerInitialized, context);
        }

        @Override
        public void onEnd() {
            flow.safeTrigger(Events.speechEnd, context);
        }

        @Override
        public void onError(Throwable t) {
            for (Listener listener : listeners) {
                listener.onError(t);
            }
        }
    };

    private final VoiceService.Listener voiceServiceListener = new VoiceService.Listener() {
        @Override
        public void onReady() {
            context.recorderInitialized = true;

            flow.safeTrigger(Events.recorderInitialized, context);
        }

        @Override
        public void onVoiceStart() {
            for (Listener listener : listeners) {
                listener.onRecognitionStart();
            }
        }

        @Override
        public void onRecognition(Collection<String> alternatives) {
            for (Listener listener : listeners) {
                listener.onRecognition(alternatives);
            }

        }

        @Override
        public void onVoiceEnd() {
            flow.safeTrigger(Events.recordEnd, context);

            for (Listener listener : listeners) {
                listener.onRecognitionEnd();
            }
        }

        @Override
        public void onError(Throwable t) {
            for (Listener listener : listeners) {
                listener.onError(t);
            }

        }
    };

    private final EasyFlow<StatefulContext> flow = FlowBuilder.from(States.INITIAL).transit(
            on(Events.speakerInitialized).to(States.SPEAKER_READY).transit(
                    on(Events.languageUnavailable).to(States.IDLE).transit(
                            on(Events.cardHidden).to(States.INITIAL),
                            on(Events.languageAvailable).to(States.PLAYING),
                            on(Events.playButtonPressed).to(States.PLAYING_TRANSITION).transit(
                                    on(Events.cardHidden).to(States.INITIAL),
                                    on(Events.languageAvailable).to(States.PLAYING).transit(
                                            on(Events.cardHidden).to(States.INITIAL),
                                            on(Events.speechEnd).to(States.IDLE),
                                            on(Events.playButtonPressed).to(States.IDLE),
                                            on(Events.cardPressed).to(States.IDLE),
                                            on(Events.recordButtonPressed).to(States.RECORDING_TRANSITION)
                                    ),
                                    on(Events.languageUnavailable).to(States.IDLE)
                            ),
                            on(Events.recordButtonPressed).to(States.RECORDING_TRANSITION).transit(
                                    on(Events.cardHidden).to(States.INITIAL),
                                    on(Events.recordPermissionGranted).to(States.RECORDER_PREPARE).transit(
                                            on(Events.recorderInitialized).to(States.RECORDING).transit(
                                                    on(Events.cardHidden).to(States.INITIAL),
                                                    on(Events.recordEnd).to(States.IDLE),
                                                    on(Events.recordButtonPressed).to(States.IDLE),
                                                    on(Events.cardPressed).to(States.IDLE),
                                                    on(Events.playButtonPressed).to(States.PLAYING_TRANSITION)
                                            )
                                    ),
                                    on(Events.recordPermissionDenied).to(States.IDLE)
                            ),
                            on(Events.cardPressed).to(States.PLAYING_TRANSITION)
                    ),
                    on(Events.languageAvailable).to(States.PLAYING)
            )
    ).executor(new AsyncExecutor());

    @Inject
    public LessonService(PlaybackService playbackService,
                         VoiceService voiceService,
                         Prompt prompt) {

        playbackService.addListener(playbackServiceListener);
        voiceService.addListener(voiceServiceListener);

        flow.whenEnter(States.INITIAL, (FlowContext context) -> {
            if (!context.speakerInitialized) {
                playbackService.init();
            }
        });

        flow.whenEnter(States.SPEAKER_READY, (FlowContext context) -> {
            if (playbackService.isLanguageAvailable(prompt.getLanguage())) {
                flow.trigger(Events.languageAvailable, context);
            } else {
                flow.trigger(Events.languageUnavailable, context);
            }
        });

        flow.whenEnter(States.PLAYING_TRANSITION, (FlowContext context) -> {
            if (!context.speakerInitialized) {
                playbackService.init();

                return;
            }

            if (playbackService.isLanguageAvailable(prompt.getLanguage())) {
                flow.trigger(Events.languageAvailable, context);

                return;
            }

            for (Listener listener : listeners) {
                listener.onLanguageUnavailable();
            }

            flow.trigger(Events.languageUnavailable, context);
        });

        flow.whenEnter(States.PLAYING, context -> {
            playbackService.speak(prompt.getLanguage(), prompt.getText());
        });

        flow.whenLeave(States.PLAYING, context -> {
            playbackService.stopSpeaking();
        });

        flow.whenEnter(States.RECORDING_TRANSITION, (FlowContext context) -> {
            if (context.permissionGranted) {
                flow.trigger(Events.recordPermissionGranted, context);

                return;
            }

            if (context.permissionDenied) {
                flow.trigger(Events.recordPermissionDenied, context);

                return;
            }

            for (Listener listener : listeners) {
                listener.onRequestRecordingPermission();
            }
        });

        flow.whenEnter(States.RECORDER_PREPARE, (FlowContext context) -> {
            if (!context.recorderInitialized) {
                voiceService.init();
            } else {
                flow.trigger(Events.recorderInitialized, context);
            }
        });

        flow.whenLeave(States.RECORDING_TRANSITION, (FlowContext context) -> {
            if (context.permissionDenied) {
                for (Listener listener : listeners) {
                    listener.onPermissionDenied();
                }
            }
        });

        flow.whenEnter(States.RECORDING, context -> {
            voiceService.start(prompt.getLanguage());
        });

        flow.whenLeave(States.RECORDING, context -> {
            voiceService.stop();
        });

        flow.whenError(new DefaultErrorHandler());

        flow.whenEnter((state, context) -> Timber.d("State: %s", state.name()));

        flow.start(context);
    }

    public void onCardPressed() {
        flow.safeTrigger(Events.cardPressed, context);
    }

    public void onPlayButonPressed() {
        flow.safeTrigger(Events.playButtonPressed, context);
    }

    public void onRecordButtonPressed() {
        flow.safeTrigger(Events.recordButtonPressed, context);
    }

    public void onRecordPermissionGranted() {
        context.permissionDenied = false;
        context.permissionGranted = true;

        flow.safeTrigger(Events.recordPermissionGranted, context);
    }

    public void onRecordPermissionDenied() {
        context.permissionGranted = false;
        context.permissionDenied = true;

        flow.safeTrigger(Events.recordPermissionDenied, context);
    }

    public void addListener(Listener listener) {
        listeners.add(listener);
    }

    public void removeListener(Listener listener) {
        listeners.remove(listener);
    }

    public interface Listener {
        void onSpeechStart();

        void onSpeechEnd();

        void onRecordingStart();

        void onRecordingEnd();

        void onRecognitionStart();

        void onRecognitionEnd();

        void onRecognition(Collection<String> alternatives);

        void onRequestRecordingPermission();

        void onLanguageUnavailable();

        void onPermissionDenied();

        void onError(Throwable t);
    }
}
