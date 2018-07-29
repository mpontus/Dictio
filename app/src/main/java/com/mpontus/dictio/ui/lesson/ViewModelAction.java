package com.mpontus.dictio.ui.lesson;

import javax.annotation.Nullable;

/**
 * ViewModel output which has to be handled once
 */

public abstract class ViewModelAction<T> {

    enum Permission {RECORD}

    enum Snackbar {PERMISSION_DENIED, LANGUAGE_UNAVAILABLE, VOLUME_DOWN}

    private final T content;
    private boolean isHandled = false;

    private ViewModelAction(T content) {
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

    class RequestPermission extends ViewModelAction<Permission> {
        RequestPermission(Permission permission) {
            super(permission);
        }
    }

    class ShowSnackbar extends ViewModelAction<Snackbar> {
        ShowSnackbar(Snackbar snackbar) {
            super(snackbar);
        }
    }


}
