package de.ebuchner.vocab.mobile.lessons;

import android.content.Context;
import android.content.Intent;
import de.ebuchner.vocab.mobile.common.VocabBaseMobileWindow;
import de.ebuchner.vocab.model.nui.WindowType;

public class LessonWindow extends VocabBaseMobileWindow {
    @Override
    protected Intent createIntent(Context context) {
        return new Intent(context, LessonActivity.class);
    }

    public WindowType windowType() {
        return WindowType.LESSONS_WINDOW;
    }
}
