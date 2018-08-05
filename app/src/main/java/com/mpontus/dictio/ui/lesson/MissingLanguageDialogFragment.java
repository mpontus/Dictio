package com.mpontus.dictio.ui.lesson;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatDialogFragment;

import com.mpontus.dictio.R;

public class MissingLanguageDialogFragment extends AppCompatDialogFragment {

    private static final String ARG_LANGUAGE = "language";

    public static MissingLanguageDialogFragment newInstance(String language) {
        final MissingLanguageDialogFragment fragment = new MissingLanguageDialogFragment();
        final Bundle args = new Bundle();
        args.putString(ARG_LANGUAGE, language);
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        String language = getArguments().getString(ARG_LANGUAGE);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setMessage(getString(R.string.dialog_missing_language, language));

        builder.setPositiveButton(R.string.dialog_open_settings, (dialog, id) -> {
            Intent intent = new Intent();
            intent.setAction("com.android.settings.TTS_SETTINGS");
            startActivity(intent);
        });

        builder.setNegativeButton(R.string.dialog_cancel, (dialog, id) -> {
        });

        return builder.create();
    }
}
