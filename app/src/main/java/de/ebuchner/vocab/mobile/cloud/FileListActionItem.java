package de.ebuchner.vocab.mobile.cloud;

import de.ebuchner.toolbox.i18n.I18NContext;
import de.ebuchner.vocab.config.ProjectInfo;
import de.ebuchner.vocab.model.cloud.CloudActionI18NKey;
import de.ebuchner.vocab.model.cloud.CloudTransfer;
import de.ebuchner.vocab.model.cloud.FileListAction;
import de.ebuchner.vocab.nui.common.I18NLocator;

public class FileListActionItem {
    private final FileListAction action;
    private final ProjectInfo projectInfo;
    private final CloudTransfer cloudTransfer;
    private I18NContext i18n = I18NLocator.locate();

    FileListActionItem(ProjectInfo projectInfo, FileListAction action, CloudTransfer cloudTransfer) {
        this.projectInfo = projectInfo;
        this.action = action;
        this.cloudTransfer = cloudTransfer;
    }

    FileListAction getAction() {
        return action;
    }

    ProjectInfo getProjectInfo() {
        return projectInfo;
    }

    @Override
    public String toString() {
        String fileName = "";
        String path = "-";
        if (action.getLocalItem() != null) {
            fileName = action.getLocalItem().getFileName();
            path = action.getLocalItem().getRelativePath();
        } else if (action.getRemoteItem() != null) {
            fileName = action.getRemoteItem().getFileName();
            path = action.getRemoteItem().getRelativePath();
        }

        String actionName = i18n.getString(CloudActionI18NKey.findKeyFor(cloudTransfer, action.getActionType()));

        return String.format("%s: %s (in %s%s)", actionName, fileName, projectInfo.getName(), path);
    }
}
