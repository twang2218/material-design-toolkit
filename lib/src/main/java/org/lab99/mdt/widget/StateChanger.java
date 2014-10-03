package org.lab99.mdt.widget;

/**
 * Base class for State Changer
 */
public abstract class StateChanger implements OnStateChangedListener {
    protected final static long DEFAULT_DURATION = 300;
    protected long mDuration;
    private boolean mEnableReleaseAnimation = true;
    private boolean mEnableFocusedAnimation = true;
    private boolean mEnablePressedAnimation = true;


    @Override
    public boolean onStateChange(int[] prev_states, int[] next_states) {
        boolean prev_pressed = contains(prev_states, android.R.attr.state_pressed);
        boolean next_pressed = contains(next_states, android.R.attr.state_pressed);
        if (prev_pressed != next_pressed) {
            //  pressed status changed
            if (next_pressed) {
                onPressed();
            } else {
                onReleased();
            }
            return true;
        }

        boolean prev_focused = contains(prev_states, android.R.attr.state_focused);
        boolean next_focused = contains(next_states, android.R.attr.state_focused);
        if (prev_focused != next_focused) {
            //  focused status changed
            if (next_focused) {
                onFocused();
            } else {
                onReleased();
            }
            return true;
        }

        return false;
    }

    //  abstract

    @Override
    public abstract void onPressed();

    @Override
    public abstract void onReleased();

    @Override
    public abstract void onFocused();

    public abstract boolean isRunning();

    public abstract void cancel();


    //  getters/setters

    public long getDuration() {
        return mDuration;
    }

    public void setDuration(long duration) {
        mDuration = duration;
    }

    public boolean getEnableReleaseAnimation() {
        return mEnableReleaseAnimation;
    }

    public void setEnableReleaseAnimation(boolean value) {
        mEnableReleaseAnimation = value;
    }

    public boolean getEnableFocusedAnimation() {
        return mEnableFocusedAnimation;
    }

    public void setEnableFocusedAnimation(boolean value) {
        mEnableFocusedAnimation = value;
    }

    public boolean getEnablePressedAnimation() {
        return mEnablePressedAnimation;
    }

    public void setEnablePressedAnimation(boolean value) {
        mEnablePressedAnimation = value;
    }

    //  private

    private boolean contains(int[] array, int value) {
        for (int v : array) {
            if (v == value)
                return true;
        }
        return false;
    }

    private String convertStatesToString(int[] stateSet) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        for (int state : stateSet) {
            switch (state) {
                case android.R.attr.state_pressed:
                    sb.append(" pressed, ");
                    break;
                case android.R.attr.state_activated:
                    sb.append(" activated, ");
                    break;
                case android.R.attr.state_focused:
                    sb.append(" focused, ");
                    break;
                case android.R.attr.state_enabled:
                    sb.append(" enabled, ");
                    break;
                case android.R.attr.state_selected:
                    sb.append(" selected, ");
                    break;
                case android.R.attr.state_hovered:
                    sb.append(" hovered, ");
                    break;
                case android.R.attr.state_accelerated:
                    sb.append(" accelerated, ");
                    break;
                case android.R.attr.state_window_focused:
                    sb.append(" window_focused, ");
                    break;

                default:
                    sb.append(" " + state + ", ");
                    break;
            }
        }
        sb.append("}");
        return sb.toString();
    }
}
