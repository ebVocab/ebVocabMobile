package de.ebuchner.vocab.mobile.model.file;

import java.io.File;

public interface FileActivityBehaviour {
    void onModelChanged(FileActivityModel model);

    void sendCancelToCallerAndExit();

    void sendFileToCallerAndExit(File file, FileActivityMode mode);

    String getFileNameInput();

    void showMessage(String message);
}
