package de.ebuchner.vocab.mobile.project;

import android.content.Context;
import android.content.Intent;
import de.ebuchner.vocab.mobile.common.VocabBaseMobileWindow;
import de.ebuchner.vocab.model.nui.WindowType;

public class ProjectWindow extends VocabBaseMobileWindow {
    public WindowType windowType() {
        return WindowType.PROJECT_WINDOW;
    }

    @Override
    protected Intent createIntent(Context context) {
        return new Intent(context, ProjectActivity.class);
    }
}
