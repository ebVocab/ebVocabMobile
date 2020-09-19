package de.ebuchner.vocab.mobile.cloud;

import android.content.Context;
import android.content.Intent;
import de.ebuchner.vocab.mobile.common.VocabBaseMobileWindow;
import de.ebuchner.vocab.model.nui.WindowType;

public class CloudWindow extends VocabBaseMobileWindow {
    @Override
    protected Intent createIntent(Context context) {
        return new Intent(context, CloudActivity.class);
    }

    public WindowType windowType() {
        return WindowType.CLOUD_WINDOW;
    }
}
