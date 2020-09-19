package de.ebuchner.vocab.mobile.project;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.view.*;
import android.widget.*;
import de.ebuchner.vocab.config.ProjectInfo;
import de.ebuchner.vocab.config.VocabEnvironment;
import de.ebuchner.vocab.config.VocabLanguage;
import de.ebuchner.vocab.config.VocabLanguages;
import de.ebuchner.vocab.mobile.R;
import de.ebuchner.vocab.mobile.common.MessageDialogs;
import de.ebuchner.vocab.mobile.common.VocabBaseActivity;
import de.ebuchner.vocab.mobile.model.project.DefaultProjects;
import de.ebuchner.vocab.mobile.platform.MobileUIPlatform;
import de.ebuchner.vocab.model.nui.WindowType;
import de.ebuchner.vocab.model.nui.platform.UIPlatformFactory;
import de.ebuchner.vocab.model.project.MobileProjectController;
import de.ebuchner.vocab.model.project.ProjectBean;
import de.ebuchner.vocab.model.project.ProjectDirectoryEntry;
import de.ebuchner.vocab.model.project.ProjectWindowBehaviour;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;

public class ProjectActivity extends VocabBaseActivity {

    private MobileProjectController projectController;
    private ProjectActivityController controller = new ProjectActivityController();

    private ListView lvProjects;
    private ArrayAdapter<ProjectDirectoryEntry> projectAdapter;

    @Override
    protected String titleResourceKey() {
        return "nui.project.title";
    }

    @Override
    protected void onCreateImpl(Bundle savedInstanceState) {
        setContentView(R.layout.project);
        projectController = new MobileProjectController(new MyBehaviour());

        ProjectBean projectBean = UIPlatformFactory.getUIPlatform().getProjectBean();

        TextView lbInfo = (TextView) findViewById(R.id.project_lbInfo);
        lbInfo.setText(VocabEnvironment.APP_TITLE_SHORT);

        TextView lbInfoVersion = (TextView) findViewById(R.id.project_lbInfoVersion);
        if (lbInfoVersion != null)
            lbInfoVersion.setText(
                    i18n.getString(
                            "nui.project.info.version",
                            Collections.singletonList(
                                    VocabEnvironment.APP_VERSION
                            )
                    )
            );

        TextView lbSelectProject = (TextView) findViewById(R.id.project_lbSelect);
        lbSelectProject.setText(i18n.getString("nui.project.select"));

        lvProjects = (ListView) findViewById(R.id.project_lvProjects);
        createProjectSelection();

        lvProjects.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @TargetApi(Build.VERSION_CODES.HONEYCOMB)
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (lvProjects.getCheckedItemCount() != 1)
                    return;
                MobileUIPlatform.instance().getMobileNuiDirector().showToast(
                        i18n.getString("project.info.loading")
                );

                int pos = lvProjects.getCheckedItemPosition();
                projectController.onUseRecentProject((ProjectDirectoryEntry) lvProjects.getItemAtPosition(pos));
            }
        });

        if (projectBean.isEmpty())
            showNoProjectHint();
        else {
            lvProjects.setVisibility(View.VISIBLE);
            lbSelectProject.setVisibility(View.VISIBLE);
        }

        Button btnCreateProject = (Button) findViewById(R.id.project_btnCreateProject);
        btnCreateProject.setText(i18n.getString("nui.project.create"));
        btnCreateProject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                controller.showCreateProjectDialog();
            }
        });
    }

    @Override
    protected void onResumeImpl() {
        super.onResumeImpl();

        updateProjectSelection();
    }

    private void showNoProjectHint() {
        String hint;
        try {
            hint = i18n.getString(
                    "nui.project.no.project.hint",
                    Collections.singletonList(DefaultProjects.vocabRootDir().getCanonicalPath())
            );
        } catch (IOException e) {
            hint = i18n.getString(
                    "nui.project.no.project.hint",
                    Collections.singletonList(DefaultProjects.vocabRootDir().getAbsolutePath())
            );
        }

        MessageDialogs.showMessageBox(
                ProjectActivity.this,
                hint
        );
    }

    private void createProjectSelection() {
        projectAdapter = new ArrayAdapter<ProjectDirectoryEntry>(
                this,
                R.layout.project_item
        );
        lvProjects.setAdapter(projectAdapter);

        updateProjectSelection();
    }

    private void updateProjectSelection() {
        ProjectBean projectBean = UIPlatformFactory.getUIPlatform().getProjectBean();

        projectAdapter.clear();

        Set<ProjectDirectoryEntry> sortedProjects = new TreeSet<>(new ProjectComparator());
        for (String projectDirName : projectBean.getRecentHomeDirectories()) {
            ProjectDirectoryEntry projectDirectoryEntry =
                    new ProjectDirectoryEntry(
                            new File(projectDirName)
                    );

            sortedProjects.add(projectDirectoryEntry);
        }

        for (ProjectDirectoryEntry project : sortedProjects)
            projectAdapter.add(project);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.project_option_menu, menu);

        MenuItem miCloud = menu.findItem(R.id.prj_open_cloud);
        miCloud.setTitle(i18n.getString("nui.project.cloud"));

        MenuItem miReset = menu.findItem(R.id.prj_reset);
        miReset.setTitle(i18n.getString("nui.project.reset"));

        MenuItem miAbout = menu.findItem(R.id.prj_open_about);
        miAbout.setTitle(i18n.getString("nui.menu.tools.about"));

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.prj_open_about:
                projectController.onOpenWindowType(WindowType.ABOUT_WINDOW);
                return true;
            case R.id.prj_reset:
                projectController.onReset();
                MobileUIPlatform.instance().getMobileNuiDirector().showToast(
                        i18n.getString("nui.project.reset.ok")
                );
                return true;
            case R.id.prj_open_cloud:
                projectController.onUseRecentProject((ProjectDirectoryEntry) lvProjects.getSelectedItem());
                projectController.onCloudAccess(
                        (ProjectDirectoryEntry) lvProjects.getSelectedItem(),
                        WindowType.CLOUD_WINDOW
                );
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    class MyBehaviour implements ProjectWindowBehaviour {
        public void doClose() {
        }

        public void sendNewProjectDirInvalidMessage() {
        }

        public void sendExistingProjectDirInvalidMessage() {
        }

        public File doOpenProjectDirectory() {
            throw new UnsupportedOperationException();
        }
    }

    class ProjectActivityController {
        private PopupWindow createProjectPopup;
        private EditText etProjectName;
        private VocabLanguage selectedLanguage;

        public void showCreateProjectDialog() {

            View createProjectView = getLayoutInflater().inflate(
                    R.layout.project_create,
                    null
            );

            createProjectPopup = new PopupWindow(createProjectView,
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT,
                    true);

            createProjectPopup.setTouchable(true);
            createProjectPopup.setFocusable(true);

            TextView tvProjectName = (TextView) createProjectView.findViewById(R.id.projectCreate_tvName);
            tvProjectName.setText(i18n.getString("nui.project.create.name"));

            etProjectName = (EditText) createProjectView.findViewById(R.id.projectCreate_etName);
            etProjectName.setHint(i18n.getString("nui.project.create.name.hint"));

            TextView lbLanguage = (TextView) createProjectView.findViewById(R.id.projectCreate_lbLanguage);
            lbLanguage.setText(i18n.getString("nui.project.create.language"));

            ListView lvLanguages = (ListView) createProjectView.findViewById(R.id.projectCreate_lvLanguage);
            ArrayAdapter<VocabLanguage> languagesAdapter = new ArrayAdapter<>(
                    createProjectView.getContext(), R.layout.project_create_item
            );
            lvLanguages.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
            lvLanguages.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    selectedLanguage = (VocabLanguage) parent.getItemAtPosition(position);
                    etProjectName.setText(selectedLanguage.getDisplayName());
                }
            });

            TreeSet<VocabLanguage> sortedLanguages = new TreeSet<>(new Comparator<VocabLanguage>() {
                @Override
                public int compare(VocabLanguage lhs, VocabLanguage rhs) {
                    return lhs.toString().compareTo(rhs.toString());
                }
            });
            sortedLanguages.addAll(VocabLanguages.loadVocabLanguages());

            for (VocabLanguage language : sortedLanguages)
                languagesAdapter.add(language);
            lvLanguages.setAdapter(languagesAdapter);

            Button btnCreate = (Button) createProjectView.findViewById(R.id.projectCreate_btnCreate);
            btnCreate.setText(i18n.getString("nui.ok"));
            btnCreate.setOnClickListener(new View.OnClickListener() {
                public void onClick(View arg0) {
                    onOk();
                }

            });

            Button btnCancel = (Button) createProjectView.findViewById(R.id.projectCreate_btnCancel);
            btnCancel.setText(i18n.getString("nui.cancel"));
            btnCancel.setOnClickListener(new View.OnClickListener() {
                public void onClick(View arg0) {
                    createProjectPopup.dismiss();

                }
            });

            createProjectPopup.showAtLocation(createProjectView, Gravity.CENTER, 0, 0);
        }

        private void onOk() {
            if (selectedLanguage == null) {
                MobileUIPlatform.instance().getMobileNuiDirector().showToast(
                        i18n.getString("nui.project.create.error.selection")
                );
                return;
            }
            String name = etProjectName.getText().toString().trim();
            if (name.length() == 0) {
                MobileUIPlatform.instance().getMobileNuiDirector().showToast(
                        i18n.getString("nui.project.create.error.name")
                );
                return;
            }

            File projectDir = DefaultProjects.vocabProjectDir(name);
            if (projectDir.exists()) {
                MobileUIPlatform.instance().getMobileNuiDirector().showToast(
                        i18n.getString("nui.project.create.error.exists")
                );
                return;
            }

            ProjectInfo info = DefaultProjects.createProjectDir(projectDir, selectedLanguage.getCode());
            if (info == null) {
                MobileUIPlatform.instance().getMobileNuiDirector().showToast(
                        i18n.getString("nui.project.create.error")
                );
                return;
            }

            MobileUIPlatform.instance().getMobileNuiDirector().showToast(
                    i18n.getString("nui.project.create.ok")
            );

            createProjectSelection();

            createProjectPopup.dismiss();
        }
    }

}
