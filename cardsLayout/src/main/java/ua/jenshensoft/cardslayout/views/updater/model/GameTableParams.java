package ua.jenshensoft.cardslayout.views.updater.model;


import android.support.annotation.Nullable;

import ua.jenshensoft.cardslayout.util.DistributionState;

public class GameTableParams<Entity> implements ViewUpdaterParams {

    @Nullable
    private DistributionState<Entity> distributionState;

    public GameTableParams(@Nullable DistributionState<Entity> distributionState) {
        this.distributionState = distributionState;
    }

    @Nullable
    public DistributionState<Entity> getDistributionState() {
        return distributionState;
    }
}
