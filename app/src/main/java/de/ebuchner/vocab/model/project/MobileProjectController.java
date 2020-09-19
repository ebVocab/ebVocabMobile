package de.ebuchner.vocab.model.project;

import de.ebuchner.vocab.model.nui.WindowType;
import de.ebuchner.vocab.model.nui.platform.UIPlatformFactory;

public class MobileProjectController extends ProjectController {

    public MobileProjectController(ProjectWindowBehaviour projectWindow) {
        super(projectWindow);
    }

    public void onUseRecentProject(ProjectDirectoryEntry entry) {
        if (entry != null)
            onUseProject(entry.getDirectory());
    }

    public void onCloudAccess(ProjectDirectoryEntry entry, WindowType windowType) {
        if (entry != null) {
            onUseProjectAndDoNotClose(entry.getDirectory());
        }
        onOpenWindowType(windowType);
    }

    public void onReset() {
        UIPlatformFactory.getUIPlatform().getNuiDirector().shutDown();
    }
}
