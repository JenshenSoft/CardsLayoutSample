package ua.jenshensoft.cardslayout.views;

public interface ValidateViewBlocker {
    boolean isCanInvalidateView();

    void setCanInvalidateView(boolean canInvalidateView);
}
