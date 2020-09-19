package de.ebuchner.vocab.mobile.practice;

import android.content.Context;
import android.content.Intent;
import de.ebuchner.vocab.mobile.common.VocabBaseMobileWindow;
import de.ebuchner.vocab.model.nui.WindowType;

public class PracticeWindow extends VocabBaseMobileWindow {
    @Override
    protected Intent createIntent(Context context) {
        return new Intent(context, PracticeActivity.class);
    }

    public WindowType windowType() {
        return WindowType.PRACTICE_WINDOW;
    }
}
