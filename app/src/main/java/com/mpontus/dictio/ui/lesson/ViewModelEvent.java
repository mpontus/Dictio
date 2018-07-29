package com.mpontus.dictio.ui.lesson;

public abstract class ViewModelEvent {

    static class SpeechStart extends ViewModelEvent {

    }

    static class SpeechEnd extends ViewModelEvent {

    }

    static class RecordingStart extends ViewModelEvent {

    }

    static class RecordingEnd extends ViewModelEvent {

    }

    static class RecognitionStart extends ViewModelEvent {

    }

    static class RecognitionEnd extends ViewModelEvent {

    }

    static class RequestRecordingPermission extends ViewModelEvent {

    }

    static class LanguageUnavailable extends ViewModelEvent {

    }

    static class PermissionDenied extends ViewModelEvent {

    }

    static class Error extends ViewModelEvent {
        private final Throwable error;

        Error(Throwable error) {
            this.error = error;
        }

        public Throwable getError() {
            return error;
        }
    }
}
