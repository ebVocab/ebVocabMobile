package de.ebuchner.vocab.mobile.common;

import android.content.Context;
import android.content.Intent;
import de.ebuchner.vocab.model.nui.NuiWindowParameter;
import de.ebuchner.vocab.nui.AbstractNuiWindow;

public abstract class VocabBaseMobileWindow extends AbstractNuiWindow {

    public final void nuiWindowCreate() {
    }

    public final void nuiWindowShow(NuiWindowParameter parameter) {
    }

    public boolean attemptClosing() {
        return true;
    }

    public void mobileWindowCreate(Context context) {
        Intent intent = createIntent(context);
        context.startActivity(intent);
    }

    protected abstract Intent createIntent(Context context);
}
