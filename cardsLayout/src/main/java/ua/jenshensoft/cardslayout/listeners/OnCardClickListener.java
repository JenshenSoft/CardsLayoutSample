package ua.jenshensoft.cardslayout.listeners;

import android.support.annotation.Nullable;

@FunctionalInterface
public interface OnCardClickListener<Entity> {
    void onCardAction(@Nullable Entity entity);
}