package com.mpontus.dictio.domain;

import com.mpontus.dictio.device.JingleService;
import com.mpontus.dictio.device.PlaybackService;
import com.mpontus.dictio.domain.model.Prompt;

import java.util.ArrayList;

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

        // Before card is shown
        INITIAL,

        // After card is shown
        CARD_SHOWN,

        // Speaker is initialized, should we speak or wait for user input?
        SPEAKER_READY,

        // Waiting for user input
        IDLE,

        // User wants to hear TTS, find out if its possible
        PLAYING_TRANSITION,

        // TTS is speaking
        PLAYING,

        // User wants to speak, ask them for the permission to record
        RECORDING_TRANSITION,

        // User has granted permission, initialize STT
        RECORDER_PREPARE,

        // User is speaking
        RECORDING,
    }

    // Events cover any external influence
    enum Events implements EventEnum {

        // Card is shown
        cardShown,

        // Card is hidden
        reset,

        // App put in the background
        background,

        // User clicks on the card
        cardPressed,

        // User clicks on Play button
        playButtonPressed,

        // User clicks on Record button
        recordButtonPressed,

        // TTS initialized
        speakerInitialized,

        // Language supported & Volume is up
        canSpeak,

        // Language unsupported or volume is down
        cannotSpeak,

        // TTS Done speaking
        speechEnd,

        // User has granted record persmission
        recordPermissionGranted,

        // User has denied record permission
        recordPermissionDenied,

        // STT initialized
        recorderInitialized,

        // STT ended
        recordEnd
    }

    class FlowContext extends StatefulContext {

        // Currently shown prompt
        private Prompt prompt;

        // User has granted recording permission
        private boolean permissionGranted;

        // User has denied recording permission
        private boolean permissionDenied;

        // Speaker has been initialized
        private boolean speakerInitialized;

        // Recorder has been initialized
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


    private final MatchService.Listener matchServiceListener = new MatchService.Listener() {

        private boolean isCompleteMatch;

        @Override
        public void onReady() {
            context.recorderInitialized = true;

            flow.safeTrigger(Events.recorderInitialized, context);
        }

        @Override
        public void onStart() {
            isCompleteMatch = false;

            for (Listener listener : listeners) {
                listener.onRecognitionStart();
            }
        }

        @Override
        public void onMatch(PhraseMatcher.Result result) {
            if (result.isComplete()) {
                isCompleteMatch = true;
            }

            for (Listener listener : listeners) {
                listener.onMatch(result);
            }
        }

        @Override
        public void onEnd() {
            if (isCompleteMatch) {
                jingleService.playSuccess();
            } else {
                jingleService.playExit();
            }

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
            on(Events.cardShown).to(States.CARD_SHOWN).transit(
                    on(Events.reset).to(States.INITIAL),
                    on(Events.speakerInitialized).to(States.SPEAKER_READY).transit(
                            on(Events.reset).to(States.INITIAL),
                            on(Events.canSpeak).to(States.PLAYING),
                            on(Events.cannotSpeak).to(States.IDLE).transit(
                                    on(Events.reset).to(States.INITIAL),
                                    on(Events.canSpeak).to(States.PLAYING),
                                    on(Events.cardPressed).to(States.PLAYING_TRANSITION),
                                    on(Events.playButtonPressed).to(States.PLAYING_TRANSITION).transit(
                                            on(Events.reset).to(States.INITIAL),
                                            on(Events.background).to(States.IDLE),
                                            on(Events.cannotSpeak).to(States.IDLE),
                                            on(Events.canSpeak).to(States.PLAYING).transit(
                                                    on(Events.reset).to(States.INITIAL),
                                                    on(Events.background).to(States.IDLE),
                                                    on(Events.speechEnd).to(States.IDLE),
                                                    on(Events.playButtonPressed).to(States.IDLE),
                                                    on(Events.cardPressed).to(States.IDLE),
                                                    on(Events.recordButtonPressed).to(States.RECORDING_TRANSITION)
                                            )
                                    ),
                                    on(Events.recordButtonPressed).to(States.RECORDING_TRANSITION).transit(
                                            on(Events.reset).to(States.INITIAL),
                                            on(Events.background).to(States.IDLE),
                                            on(Events.recordPermissionDenied).to(States.IDLE),
                                            on(Events.recordPermissionGranted).to(States.RECORDER_PREPARE).transit(
                                                    on(Events.reset).to(States.INITIAL),
                                                    on(Events.background).to(States.IDLE),
                                                    on(Events.recorderInitialized).to(States.RECORDING).transit(
                                                            on(Events.reset).to(States.INITIAL),
                                                            on(Events.background).to(States.IDLE),
                                                            on(Events.recordEnd).to(States.IDLE),
                                                            on(Events.recordButtonPressed).to(States.IDLE),
                                                            on(Events.cardPressed).to(States.IDLE),
                                                            on(Events.playButtonPressed).to(States.PLAYING_TRANSITION)
                                                    )
                                            )
                                    )
                            )
                    )
            )
    ).executor(new AsyncExecutor());

    private final PlaybackService playbackService;
    private final MatchService matchService;
    private final JingleService jingleService;

    @Inject
    public LessonService(PlaybackService playbackService,
                         MatchService matchService,
                         JingleService jingleService) {
        this.playbackService = playbackService;
        this.matchService = matchService;
        this.jingleService = jingleService;

        playbackService.addListener(playbackServiceListener);
        matchService.addListener(matchServiceListener);

        flow.whenEnter(States.CARD_SHOWN, (FlowContext context) -> {
            if (!context.speakerInitialized) {
                playbackService.init();
            } else {
                flow.trigger(Events.speakerInitialized, context);
            }
        });

        flow.whenEnter(States.SPEAKER_READY, (FlowContext context) -> {
            if (playbackService.getVolume() > 0 &&
                    playbackService.isLanguageAvailable(context.prompt.getLanguage())) {
                flow.trigger(Events.canSpeak, context);
            } else {
                flow.trigger(Events.cannotSpeak, context);
            }
        });

        flow.whenEnter(States.PLAYING_TRANSITION, (FlowContext context) -> {
            if (playbackService.getVolume() == 0) {
                for (Listener listener : listeners) {
                    listener.onVolumeDown();
                }

                flow.trigger(Events.cannotSpeak, context);

                return;
            }

            if (!playbackService.isLanguageAvailable(context.prompt.getLanguage())) {
                for (Listener listener : listeners) {
                    listener.onLanguageUnavailable(context.prompt.getLanguage());
                }

                flow.trigger(Events.cannotSpeak, context);

                return;
            }

            flow.trigger(Events.canSpeak, context);
        });

        flow.whenEnter(States.PLAYING, (FlowContext context) -> {
            playbackService.speak(context.prompt.getLanguage(), context.prompt.getText());

            for (Listener listener : listeners) {
                listener.onSpeechStart();
            }
        });

        flow.whenLeave(States.PLAYING, context -> {
            playbackService.stopSpeaking();

            for (Listener listener : listeners) {
                listener.onSpeechEnd();
            }
        });

        flow.whenEnter(States.RECORDING_TRANSITION, (FlowContext context) -> {
            if (context.permissionGranted) {
                flow.trigger(Events.recordPermissionGranted, context);

                return;
            }

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

        flow.whenEnter(States.RECORDER_PREPARE, (FlowContext context) -> {
            if (!context.recorderInitialized) {
                matchService.init();
            } else {
                flow.trigger(Events.recorderInitialized, context);
            }
        });

        flow.whenEnter(States.RECORDING, (FlowContext context) -> {
            jingleService.playEnter();
            matchService.start(context.prompt);

            for (Listener listener : listeners) {
                listener.onRecordingStart();
            }
        });

        flow.whenLeave(States.RECORDING, context -> {
            matchService.stop();

            for (Listener listener : listeners) {
                listener.onRecordingEnd();
            }
        });

        flow.whenError(new DefaultErrorHandler());

        flow.whenEnter((state, context) -> Timber.d("State: %s", state.name()));

        flow.start(context);
    }

    public void release() {
        flow.safeTrigger(Events.reset, context);
        playbackService.release();
        matchService.release();
    }

    public void onCardShown(Prompt prompt) {
        context.prompt = prompt;

        flow.safeTrigger(Events.cardShown, context);
    }

    public void onCardHidden() {
        context.prompt = null;

        flow.safeTrigger(Events.reset, context);
    }

    public void onBackground() {
        flow.safeTrigger(Events.background, context);
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

        void onMatch(PhraseMatcher.Result match);

        void onRequestRecordingPermission();

        void onLanguageUnavailable(String language);

        void onVolumeDown();

        void onPermissionDenied();

        void onError(Throwable t);
    }
}
