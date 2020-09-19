package de.ebuchner.vocab.mobile.model.project;

import android.app.Activity;
import android.content.SharedPreferences;
import de.ebuchner.vocab.mobile.platform.MobileUIPlatform;
import de.ebuchner.vocab.model.project.ProjectBean;
import de.ebuchner.vocab.model.project.ProjectConfiguration;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MobileProjectBean extends ProjectBean {

    private static final String SHARED_PREFERENCES_NAME = "de.ebuchner.vocab.project";
    private static final String CURRENT_HOME_DIRECTORY = "home.directory";

    @Override
    public void loadProjectBean() {

        Activity currentActivity = MobileUIPlatform.instance().getMobileNuiDirector().getCurrentActivity();

        setCurrentHomeDirectory(null);
        String chdName =
                currentActivity.getSharedPreferences(
                        SHARED_PREFERENCES_NAME,
                        Activity.MODE_PRIVATE
                ).getString(
                        CURRENT_HOME_DIRECTORY,
                        null
                );
        File chd = null;
        if (chdName != null) {
            chd = new File(chdName);
        } else {
            List<File> projectDirs = DefaultProjects.findExistingProjectDirectories();
            if (projectDirs.size() > 0)
                chd = projectDirs.get(0);
        }
        if (chd != null && ProjectConfiguration.isValidProjectDirectory(chd))
            setCurrentHomeDirectory(chd.getAbsolutePath());
    }

    @Override
    public void saveProjectBean() {
        Activity currentActivity = MobileUIPlatform.instance().getMobileNuiDirector().getCurrentActivity();
        SharedPreferences.Editor editor = currentActivity.getSharedPreferences(
                SHARED_PREFERENCES_NAME,
                Activity.MODE_PRIVATE
        ).edit();

        editor.putString(CURRENT_HOME_DIRECTORY, getCurrentHomeDirectory());

        editor.apply();
    }

    @Override
    public Set<String> getRecentHomeDirectories() {
        Set<String> directories = new HashSet<>();
        for (File vocabDirectory : new DefaultProjects().loadVocabDirectories()) {
            directories.add(vocabDirectory.getAbsolutePath());
        }
        return directories;
    }
}
