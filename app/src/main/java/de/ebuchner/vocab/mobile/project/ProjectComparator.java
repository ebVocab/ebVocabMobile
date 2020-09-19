package de.ebuchner.vocab.mobile.project;

import de.ebuchner.vocab.model.project.ProjectDirectoryEntry;

import java.util.Comparator;

public class ProjectComparator implements Comparator<ProjectDirectoryEntry> {
    @Override
    public int compare(ProjectDirectoryEntry lhs, ProjectDirectoryEntry rhs) {
        return lhs.toString().compareTo(rhs.toString());
    }
}
