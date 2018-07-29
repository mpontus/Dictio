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
import au.com.ds.ef.call.StateHandler;
import timber.log.Timber;

import static au.com.ds.ef.FlowBuilder.on;

@Singleton
public class LessonService {

    enum States implements StateEnum {
        INITIAL,
        READY,
        IDLE,
        PLAYING_TRANSITION,
        PLAYING,
        RECORDING_TRANSITION,
        RECORDING,
    }

    // Events cover any external influence
    enum Events implements EventEnum {
        cardShown,
        cardHidden,
        cardPressed,
        playButtonPressed,
        recordButtonPressed,
        languageAvailable,
        languageUnavailable,
        recordPermissionGranted,
        recordPermissionDenied,
        speechEnd,
        recordEnd,
    }

    class FlowContext extends StatefulContext {
        private boolean permissionGranted;

        private boolean permissionDenied;
    }

    private final FlowContext context = new FlowContext();

    private final ArrayList<Listener> listeners = new ArrayList<>();

    private final PlaybackService.Listener playbackServiceListener = new PlaybackService.Listener() {
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
                            on(Events.recordPermissionGranted).to(States.RECORDING).transit(
                                    on(Events.cardHidden).to(States.INITIAL),
                                    on(Events.recordEnd).to(States.IDLE),
                                    on(Events.recordButtonPressed).to(States.IDLE),
                                    on(Events.cardPressed).to(States.IDLE),
                                    on(Events.playButtonPressed).to(States.PLAYING_TRANSITION)
                            ),
                            on(Events.recordPermissionDenied).to(States.IDLE)
                    ),
                    on(Events.cardPressed).to(States.PLAYING_TRANSITION)
            ),
            on(Events.languageAvailable).to(States.PLAYING)
    ).executor(new AsyncExecutor());

    @Inject
    public LessonService(PlaybackService playbackService,
                         VoiceService voiceService,
                         Prompt prompt) {

        playbackService.addListener(playbackServiceListener);
        voiceService.addListener(voiceServiceListener);

        flow.whenEnter(States.INITIAL, context -> {
            if (playbackService.isLanguageAvailable(prompt.getLanguage())) {
                flow.trigger(Events.languageAvailable, context);
            } else {
                flow.trigger(Events.languageUnavailable, context);
            }
        });

        flow.whenEnter(States.PLAYING_TRANSITION, context -> {
            if (playbackService.isLanguageAvailable(prompt.getLanguage())) {
                flow.trigger(Events.languageAvailable, context);
            } else {
                for (Listener listener : listeners) {
                    listener.onLanguageUnavailable();
                }

                flow.trigger(Events.languageUnavailable, context);
            }
        });

        flow.whenEnter(States.PLAYING, context -> {
            playbackService.speak(prompt.getLanguage(), prompt.getText());
        });

        flow.whenLeave(States.PLAYING, context -> {
            playbackService.stopSpeaking();
        });

        flow.whenEnter(States.RECORDING_TRANSITION, context -> {
            for (Listener listener : listeners) {
                listener.onRequestRecordingPermission();
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

        flow.whenEnter(new StateHandler<StatefulContext>() {
            @Override
            public void call(StateEnum state, StatefulContext context) throws Exception {
                Timber.d("State: %s", state.name());
            }
        });

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
