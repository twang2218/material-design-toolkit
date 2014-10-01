package org.lab99.mdt.widget;

public interface OnStateChangedListener {
    public void onStateChange(int[] prev_states, int[] next_states);

    public void onPressed();

    public void onReleased();

    public void onFocused();
}
