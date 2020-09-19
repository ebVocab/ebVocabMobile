package de.ebuchner.vocab.mobile.model.file;

import de.ebuchner.vocab.config.Config;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FileActivityModel {
    private final FileActivityMode selectedMode;
    private final File rootDir;

    private File selectedDir;
    private File selectedFile;
    private FileActivityModelListener listener;

    public FileActivityModel(FileActivityMode selectedMode) {
        this.selectedMode = selectedMode;
        this.rootDir = rootDir();
        this.selectedDir = rootDir;
    }

    private File rootDir() {
        return Config.instance().getProjectInfo().getVocabDirectory();
    }

    public File getSelectedFile() {
        return selectedFile;
    }

    public File getSelectedDir() {
        return selectedDir;
    }

    public void setListener(FileActivityModelListener listener) {
        this.listener = listener;
    }

    private void fireModelChanged() {
        listener.onModelChanged(this);
    }

    public boolean isUpDirEnabled() {
        return !selectedDir.equals(rootDir);
    }

    public void onDirUp() {
        selectedDir = selectedDir.getParentFile();
        selectedFile = null;
        fireModelChanged();
    }

    public boolean hasSelectedFile() {
        return selectedFile != null;
    }

    public List<File> selectedDirFiles() {
        List<File> files = new ArrayList<>();
        if (selectedDir != null) {
            File[] filesListed = selectedDir.listFiles();
            if (filesListed != null)
                Collections.addAll(files, selectedDir.listFiles());
        }
        return files;
    }

    public void changeSelectedDirectory(File selectedDir) {
        this.selectedDir = selectedDir;
        fireModelChanged();
    }

    public void changeSelectedFile(File selectedFile) {
        this.selectedFile = selectedFile;
        fireModelChanged();
    }

    public FileActivityMode getSelectedMode() {
        return selectedMode;
    }

    public void onSubDirectoryCreated() {
        fireModelChanged();
    }

    public boolean isDirCreatedEnabled() {
        return selectedDir != null;
    }
}
