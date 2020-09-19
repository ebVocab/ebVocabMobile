package de.ebuchner.vocab.model.project;

import java.io.File;

public class ProjectDirectoryEntry {
    private final File directory;

    public ProjectDirectoryEntry(File directory) {
        this.directory = directory;
    }

    @Override
    public String toString() {
        return directory.getName();
    }

    public File getDirectory() {
        return directory;
    }
}
