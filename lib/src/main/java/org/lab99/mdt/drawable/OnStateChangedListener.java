package org.lab99.mdt.drawable;

public interface OnStateChangedListener {
    public boolean onStateChange(int[] prev_states, int[] next_states);

    public void onPressed();

    public void onReleased();

    public void onFocused();

    public boolean isEnabled();

    public void setEnabled(boolean enabled);
}
