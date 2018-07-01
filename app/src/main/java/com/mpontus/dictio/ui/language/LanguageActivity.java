package com.mpontus.dictio.ui.language;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.mpontus.dictio.R;
import com.mpontus.dictio.ui.shared.LangaugeResources;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import dagger.android.support.DaggerAppCompatActivity;

public class LanguageActivity extends DaggerAppCompatActivity {
    public static final String EXTRA_LANGUAGE = "LANGUAGE";

    public static Intent createIntent(Context context, String language) {
        Intent intent = new Intent(context, LanguageActivity.class);

        intent.putExtra(EXTRA_LANGUAGE, language);

        return intent;
    }

    @Inject
    LangaugeResources langaugeResources;

    @BindView(R.id.languages)
    RecyclerView languagesView;

    TypedArray languageCodes;

    private String currentLanguage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_language);
        ButterKnife.bind(this);

        currentLanguage = getIntent().getStringExtra(EXTRA_LANGUAGE);

        Resources resources = getResources();

        languageCodes = resources.obtainTypedArray(R.array.language_codes);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);

        languagesView.setLayoutManager(layoutManager);
        languagesView.setAdapter(new Adapter());
    }

    private void onLangaugeSelected(int adapterPosition) {
        String code = languageCodes.getString(adapterPosition);

        Intent resultIntent = new Intent();

        resultIntent.putExtra(EXTRA_LANGUAGE, code);

        setResult(Activity.RESULT_OK, resultIntent);

        finish();
    }


    class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.icon)
        ImageView iconView;

        @BindView(R.id.name)
        TextView nameView;

        @BindView(R.id.checkmark)
        ImageView checkmarkView;

        ViewHolder(View itemView) {
            super(itemView);

            ButterKnife.bind(this, itemView);
        }

        @OnClick
        void handleClick() {
            LanguageActivity.this.onLangaugeSelected(getAdapterPosition());
        }

    }

    class Adapter extends RecyclerView.Adapter<ViewHolder> {
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.language_item, parent, false);

            return new ViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            String code = languageCodes.getString(position);
            boolean isCurrent = currentLanguage.equals(code);

            holder.iconView.setImageDrawable(langaugeResources.getIcon(code));
            holder.nameView.setText(langaugeResources.getName(code));
            holder.checkmarkView.setVisibility(isCurrent ? View.VISIBLE : View.GONE);
        }

        @Override
        public int getItemCount() {
            return languageCodes.length();
        }
    }
}
