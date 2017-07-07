package ua.jenshensoft.cardslayout.views.updater.model;


import android.support.annotation.Nullable;

import ua.jenshensoft.cardslayout.util.DistributionState;

public class GameTableParams implements ViewUpdaterParams {

    @Nullable
    private DistributionState distributionState;

    public GameTableParams(@Nullable DistributionState distributionState) {
        this.distributionState = distributionState;
    }

    @Nullable
    public DistributionState getDistributionState() {
        return distributionState;
    }
}
