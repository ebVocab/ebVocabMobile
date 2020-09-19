package de.ebuchner.vocab.mobile.about;

import android.content.Context;
import android.content.Intent;
import de.ebuchner.vocab.mobile.common.VocabBaseMobileWindow;
import de.ebuchner.vocab.model.nui.WindowType;

public class AboutWindow extends VocabBaseMobileWindow {
    @Override
    protected Intent createIntent(Context context) {
        return new Intent(context, AboutActivity.class);
    }

    public WindowType windowType() {
        return WindowType.ABOUT_WINDOW;
    }
}
