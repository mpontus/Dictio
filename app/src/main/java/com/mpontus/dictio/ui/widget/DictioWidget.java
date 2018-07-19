package com.mpontus.dictio.ui.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.widget.RemoteViews;

import com.mpontus.dictio.R;
import com.mpontus.dictio.data.PromptsRepository;
import com.mpontus.dictio.data.TranslationManager;
import com.mpontus.dictio.data.model.Prompt;
import com.mpontus.dictio.ui.lesson.LessonActivity;
import com.mpontus.dictio.ui.shared.LangaugeResources;

import javax.inject.Inject;

import dagger.android.AndroidInjection;

/**
 * Implementation of App Widget functionality.
 */
public class DictioWidget extends AppWidgetProvider {

    @Inject
    PromptsRepository promptsRepository;

    @Inject
    TranslationManager translationManager;

    @Inject
    LangaugeResources langaugeResources;

    void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                         int appWidgetId) {

        Prompt prompt = promptsRepository.getNextPrompt().blockingGet();

        if (prompt != null) {
            // Construct the RemoteViews object
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.dictio_widget);

            Intent intent =
                    LessonActivity.createIntent(context, prompt.getLanguage(), prompt.getCategory());

            views.setOnClickPendingIntent(R.id.root, PendingIntent.getActivity(context, 0, intent, 0));

            views.setTextViewText(R.id.prompt, prompt.getText());
            views.setTextViewText(R.id.translation, translationManager.getTranslation(prompt));

            BitmapDrawable languageFlag = (BitmapDrawable) langaugeResources.getIcon(prompt.getLanguage());
            views.setImageViewBitmap(R.id.flag, languageFlag.getBitmap());

            // Instruct the widget manager to update the widget
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        AndroidInjection.inject(this, context);

        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }
}

