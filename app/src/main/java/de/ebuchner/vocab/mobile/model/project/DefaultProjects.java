package de.ebuchner.vocab.mobile.model.project;

import android.content.Context;
import android.os.Environment;
import android.util.Log;
import de.ebuchner.vocab.config.ProjectInfo;
import de.ebuchner.vocab.mobile.platform.MobileUIPlatform;
import de.ebuchner.vocab.model.cloud.ProjectItem;
import de.ebuchner.vocab.model.project.ProjectConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class DefaultProjects {
    public static final String DEFAULT_ROOT_DIR = "ebuchner.de" + System.getProperty("file.separator") + "vocab";
    private static final String TAG = DefaultProjects.class.getName();

    public static File vocabRootDir() {
        Context ctx = MobileUIPlatform.instance().getMobileNuiDirector().getCurrentActivity();
        File storage = ctx.getExternalFilesDir(null);
        return new File(storage, DEFAULT_ROOT_DIR);
    }

    public static File vocabProjectDir(String projectName) {
        return new File(vocabRootDir(), projectName);
    }

    public static ProjectInfo createProjectDir(ProjectItem remoteProject) {
        return createProjectDir(
                vocabProjectDir(remoteProject.getProjectName()),
                remoteProject.getProjectLocale()
        );
    }

    public static ProjectInfo createProjectDir(File projectDir, String projectLocale) {
        if (!projectDir.mkdirs()) {
            Log.e(TAG, "Could not create project directory " + projectDir);
            return null;
        }

        ProjectConfiguration.createProjectDir(projectDir, projectLocale);

        return new ProjectInfo(projectDir);
    }

    public static List<File> findExistingProjectDirectories() {
        List<File> projectDirectories = new ArrayList<>();
        File[] rootChilds = vocabRootDir().listFiles();
        if (rootChilds == null)
            return projectDirectories;

        for (File rootChild : rootChilds) {
            if (rootChild.isDirectory())
                projectDirectories.add(rootChild);
        }
        return projectDirectories;
    }

    public List<File> loadVocabDirectories() {
        List<File> vocabDirectories = new ArrayList<>();

        File vocabRootDir = vocabRootDir();
        if (!vocabRootDir.exists()) {
            // if this fails in emulator check for read-only file system!
            if (!vocabRootDir.mkdirs())
                throw new RuntimeException("Could not create vocab directory: " + vocabRootDir);
            return vocabDirectories;
        }

        if (!vocabRootDir.isDirectory()) {
            Log.e(TAG, "Not a directory: " + vocabRootDir);
            return vocabDirectories;
        }

        File[] directories = vocabRootDir.listFiles();
        if (directories != null) {
            for (File projectDirectory : directories) {
                if (!projectDirectory.isDirectory())
                    continue;

                if (!ProjectConfiguration.isValidProjectDirectory(projectDirectory)) {
                    Log.e(
                            TAG,
                            "Not a valid project directory " +
                                    projectDirectory
                    );
                    continue;
                }

                vocabDirectories.add(projectDirectory);
            }
        }

        return vocabDirectories;
    }
}
