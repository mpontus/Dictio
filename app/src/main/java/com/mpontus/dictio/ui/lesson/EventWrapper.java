package com.mpontus.dictio.ui.lesson;

import javax.annotation.Nullable;

public class EventWrapper<T> {
    private final T content;
    private boolean isHandled = false;

    EventWrapper(T content) {
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
}
