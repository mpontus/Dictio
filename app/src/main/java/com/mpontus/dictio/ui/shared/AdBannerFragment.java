package com.mpontus.dictio.ui.shared;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.mpontus.dictio.R;

public class AdBannerFragment extends Fragment {
    public AdBannerFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.ad_banner_fragment, container, false);

        AdView adView = root.findViewById(R.id.adView);

        adView.loadAd(new AdRequest.Builder().build());

        return root;
    }
}
