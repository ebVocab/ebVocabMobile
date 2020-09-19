package de.ebuchner.vocab.model.editor;

import de.ebuchner.vocab.model.nui.NuiWindow;
import de.ebuchner.vocab.model.nui.NuiWindowParameter;
import de.ebuchner.vocab.model.nui.WindowType;

public class MobileEditorController extends EditorController {

    public MobileEditorController(EditorWindowBehaviour editorWindow, EntryInEditWindowBehaviour entryInEditWindow) {
        super(editorWindow, entryInEditWindow);
    }

    @Override
    public NuiWindow onOpenWindowType(WindowType windowType) {
        if (checkModified() != CheckResult.CONTINUE)
            return null;
        return super.onOpenWindowType(windowType);
    }

    @Override
    public NuiWindow onOpenWindowType(WindowType windowType, NuiWindowParameter parameter) {
        if (checkModified() != CheckResult.CONTINUE)
            return null;
        return super.onOpenWindowType(windowType, parameter);
    }
}
