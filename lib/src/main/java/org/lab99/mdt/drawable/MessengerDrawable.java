package org.lab99.mdt.drawable;

import android.content.res.Resources;
import android.graphics.drawable.Drawable;

/**
 * A special {@link Drawable} for passing the touch event to the wrapped Drawable, as a messenger.
 */
public class MessengerDrawable extends ProxyDrawable {
    public MessengerDrawable(Drawable original, Drawable receiver) {
        this(original, receiver, null);
    }

    MessengerDrawable(Drawable original, Drawable receiver, ProxyState state) {
        super(original, state);
        ((MessengerState) getConstantState()).mReceiver = receiver;
    }

    MessengerDrawable(ProxyState state, Resources res) {
        super(state, res);
    }

    @Override
    public boolean setState(int[] stateSet) {
        MessengerState state = (MessengerState) getConstantState();
        if (state.mReceiver != null) {
            state.mReceiver.setState(stateSet);
        }
        return super.setState(stateSet);
    }

    @Override
    protected ProxyState createConstantState(ProxyState orig, Resources res) {
        return new MessengerState(orig, res);
    }

    static class MessengerState extends ProxyState {
        Drawable mReceiver;

        MessengerState(ProxyState orig, Resources res) {
            super(orig, res);
        }

        @Override
        public Drawable newDrawable(Resources res) {
            return new MessengerDrawable(this, res);
        }

        @Override
        public Drawable newDrawable() {
            return new MessengerDrawable(this, null);
        }

        @Override
        protected void initWithState(ProxyState orig, Resources res) {
            super.initWithState(orig, res);
            MessengerState state = (MessengerState) orig;
            mReceiver = state.mReceiver;
        }

        @Override
        protected void initWithoutState(Resources res) {
            super.initWithoutState(res);
        }
    }
}
