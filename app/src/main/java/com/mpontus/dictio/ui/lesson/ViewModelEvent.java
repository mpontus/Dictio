package com.mpontus.dictio.ui.lesson;

import android.support.annotation.Nullable;

import com.mpontus.dictio.data.model.Prompt;

public abstract class ViewModelEvent<T> {

    enum Permission {RECORD}

    enum Dialog {LANGUAGE_UNAVAILABLE, VOLUME_DOWN, PERMISSION_DENIED}

    private final T content;

    private boolean isHandled = false;

    private ViewModelEvent(T content) {
        this.content = content;
    }

    @Nullable
    public T getContentIfNotHandled() {
        if (isHandled) {
            return null;
        }

        isHandled = true;

        return content;
    }

    static class AddPrompt extends ViewModelEvent<Prompt> {
        public AddPrompt(Prompt content) {
            super(content);
        }
    }

    static class RemovePrompt extends ViewModelEvent<Prompt> {
        public RemovePrompt(Prompt content) {
            super(content);
        }
    }

    static class RequestPermission extends ViewModelEvent<Permission> {
        public RequestPermission(Permission content) {
            super(content);
        }
    }

    static class ShowDialog extends ViewModelEvent<Dialog> {
        public ShowDialog(Dialog content) {
            super(content);
        }
    }

    static class ShowError extends ViewModelEvent<Throwable> {
        public ShowError(Throwable content) {
            super(content);
        }
    }
}
