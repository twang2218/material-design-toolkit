package org.lab99.mdt.drawable;

public interface OnStateChangedListener {
    boolean onStateChange(int[] prev_states, int[] next_states);

    void onPressed();

    void onReleased();

    void onFocused();

    boolean isEnabled();

    void setEnabled(boolean enabled);
}
