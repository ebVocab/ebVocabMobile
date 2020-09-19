package de.ebuchner.vocab.mobile.editor;

import android.content.Context;
import android.content.Intent;
import de.ebuchner.vocab.mobile.common.VocabBaseMobileWindow;
import de.ebuchner.vocab.model.nui.WindowType;

public class EditorWindow extends VocabBaseMobileWindow {
    @Override
    protected Intent createIntent(Context context) {
        return new Intent(context, EditorActivity.class);
    }

    @Override
    public WindowType windowType() {
        return WindowType.EDITOR_WINDOW;
    }
}
