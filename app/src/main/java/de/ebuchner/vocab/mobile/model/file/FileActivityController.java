package de.ebuchner.vocab.mobile.model.file;

import de.ebuchner.vocab.config.ConfigConstants;

import java.io.File;
import java.util.logging.Logger;

public class FileActivityController implements FileActivityModelListener {

    private final FileActivityBehaviour activity;
    private final FileActivityModel model;
    private Logger logger = Logger.getLogger(FileActivityController.class.getName());

    public FileActivityController(FileActivityModel model, FileActivityBehaviour activity) {
        this.model = model;
        this.activity = activity;

        model.setListener(this);
    }

    public void onOk() {
        if (model.getSelectedMode() == FileActivityMode.SAVE) {
            File file = checkFileName(activity.getFileNameInput(), true);
            if (file == null) {
                return;
            }

            activity.sendFileToCallerAndExit(
                    file,
                    model.getSelectedMode()
            );
            return;
        }

        activity.sendFileToCallerAndExit(
                model.getSelectedFile(),
                model.getSelectedMode()
        );
    }

    private File checkFileName(String fileName, boolean appendExt) {
        if (fileName == null)
            return null;

        fileName = fileName.trim();
        if (fileName.length() == 0) {
            activity.showMessage("Invalid blank only file name of file name with length 0");
            return null;
        }

        if (fileName.contains("..")) {
            activity.showMessage("File name must not contain '..'");
            return null;
        }

        if (fileName.contains(System.getProperty("file.separator"))) {
            activity.showMessage("File name must not contain file separator character");
            return null;
        }

        if (fileName.contains(System.getProperty("path.separator"))) {
            activity.showMessage("File name must not contain path separator character");
            return null;
        }

        for (char c : fileName.toCharArray()) {
            if (Character.isISOControl(c)) {
                activity.showMessage("File name must not contain control character");
                return null;
            }
        }

        if (appendExt) {
            final String extension = String.format(".%s", ConfigConstants.FILE_EXTENSION);
            if (fileName.contains(".") && !fileName.endsWith(extension)) {
                activity.showMessage("No extension or vocab extension required");
                return null;
            }

            if (!fileName.endsWith(extension)) {
                fileName = String.format("%s%s", fileName, extension);
            }
        }

        return new File(model.getSelectedDir(), fileName);
    }

    public void onCancel() {
        activity.sendCancelToCallerAndExit();
    }

    @Override
    public void onModelChanged(FileActivityModel model) {
        activity.onModelChanged(model);
    }

    public void onDirUp() {
        model.onDirUp();
    }

    public void onUIReady() {
        activity.onModelChanged(model);
    }

    public void onDirectoryContentSelection(File file) {
        if (file.isDirectory())
            model.changeSelectedDirectory(file);
        else
            model.changeSelectedFile(file);
    }

    public boolean onDirectoryCreate(String input) {
        File subDir = checkFileName(input, false);
        if (subDir == null)
            return false;

        if (subDir.exists()) {
            activity.showMessage("Directory already exists");
            return false;
        }

        if (!subDir.mkdir()) {
            activity.showMessage("Directory creation failed");
            return false;
        }

        model.onSubDirectoryCreated();
        return true;
    }
}
