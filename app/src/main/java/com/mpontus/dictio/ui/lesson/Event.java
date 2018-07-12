package com.mpontus.dictio.ui.lesson;

import com.mpontus.dictio.data.model.Prompt;

abstract class Event {

    static class PermissionGranted extends Event {

    }

    static class Ready extends Event {

    }

    static class PromptShown extends Event {
        private final Prompt prompt;

        PromptShown(Prompt prompt) {
            this.prompt = prompt;
        }

        public Prompt getPrompt() {
            return prompt;
        }
    }

    static class PromptHidden extends Event {
        private final Prompt prompt;

        PromptHidden(Prompt prompt) {
            this.prompt = prompt;
        }

        public Prompt getPrompt() {
            return prompt;
        }
    }

    static class ToggleSynthesis extends Event {
        private final boolean value;

        ToggleSynthesis(boolean value) {
            this.value = value;
        }

        public boolean isValue() {
            return value;
        }
    }

    static class SynthesisStart extends Event {
    }

    static class SynthesisEnd extends Event {
    }

    static class RecognitionStart extends Event {
    }

    static class RecognitionEnd extends Event {
    }

    static class Recognition extends Event {
        private final Iterable<String> alternatives;

        Recognition(Iterable<String> alternatives) {
            this.alternatives = alternatives;
        }

        public Iterable<String> getAlternatives() {
            return alternatives;
        }
    }

    static class Error extends Event {
        private final Throwable error;

        Error(Throwable error) {
            this.error = error;
        }

        public Throwable getError() {
            return error;
        }
    }
}
