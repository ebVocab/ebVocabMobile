package de.ebuchner.vocab.model.cloud;

import android.os.AsyncTask;
import android.util.Log;
import de.ebuchner.vocab.config.ProjectInfo;
import de.ebuchner.vocab.mobile.model.project.DefaultProjects;
import de.ebuchner.vocab.model.nui.platform.UIPlatformFactory;

import java.io.File;
import java.util.*;

public class MobileCloudController extends CloudController {
    private static final String TAG = MobileCloudController.class.getName();
    MobileCloudWindowBehaviour mobileCloudWindow;

    public MobileCloudController(MobileCloudWindowBehaviour cloudWindow) {
        super(cloudWindow);
        this.mobileCloudWindow = cloudWindow;
    }

    @Override
    protected LocalFileStrategy shouldDeleteLocalOnlyFiles() {
        return LocalFileStrategy.UPLOAD;
    }

    @Override
    protected boolean usePostForFileUpload() {
        return true;
    }

    public void doRefresh() throws Exception {
        new RefreshTask().execute();
    }

    public void doTransferCloud(CloudTransfer cloudTransfer, Map<ProjectInfo, List<FileListAction>> actionMap) {
        new TransferTask(cloudTransfer, actionMap).execute();
    }

    class TaskResult {
        Map<ProjectInfo, CloudResult> resultMap = new HashMap<>();
        Exception exception;
    }

    class TransferTask extends AsyncTask<String, Void, TaskResult> {

        private final Map<ProjectInfo, List<FileListAction>> actionMap;
        private final CloudTransfer cloudTransfer;

        public TransferTask(CloudTransfer cloudTransfer, Map<ProjectInfo, List<FileListAction>> actionMap) {
            this.cloudTransfer = cloudTransfer;
            this.actionMap = actionMap;
        }

        @Override
        protected void onPostExecute(TaskResult result) {
            if (result.exception != null) {
                mobileCloudWindow.exceptionOccurred(result.exception);
                return;
            }
            mobileCloudWindow.downloadBegin();
            for (ProjectInfo projectInfo : result.resultMap.keySet())
                cloudWindow.updateResult(
                        projectInfo,
                        result.resultMap.get(projectInfo)
                );
            mobileCloudWindow.downloadFinished();
        }

        @Override
        protected TaskResult doInBackground(String... params) {
            TaskResult result = new TaskResult();
            try {
                for (ProjectInfo projectInfo : actionMap.keySet()) {
                    result.resultMap.put(
                            projectInfo,
                            MobileCloudController.super.doTransferNoNotify(
                                    mobileCloudWindow.getServerUrl(),
                                    projectInfo,
                                    actionMap.get(projectInfo),
                                    cloudTransfer
                            )
                    );
                }
            } catch (Exception e) {
                result.exception = e;
            }
            return result;
        }
    }

    class RefreshTask extends AsyncTask<String, Void, TaskResult> {

        private ProjectList remoteProjectList() throws Exception {
            CloudResult cloudResult = MobileCloudController.super.doFetchProjectList(
                    mobileCloudWindow.getServerUrl()
            );
            if (cloudResult.getRemoteResult().getType() != CloudResult.RemoteResult.Type.PROJECT_LIST)
                return null;
            return cloudResult.getRemoteResult().getRemoteProjectList();
        }

        private List<File> localProjectList() {
            Set<String> localProjectPaths = UIPlatformFactory.getUIPlatform().getProjectBean().getRecentHomeDirectories();
            List<File> localProjects = new ArrayList<File>();
            for (String path : localProjectPaths)
                localProjects.add(new File(path));

            return localProjects;
        }

        @Override
        protected TaskResult doInBackground(String... params) {
            TaskResult result = new TaskResult();
            try {
                ProjectList remoteProjects = remoteProjectList();
                if (remoteProjects == null)
                    return result;

                List<File> localProjects = localProjectList();

                addExistingProjects(result.resultMap, remoteProjects, localProjects);

                addLocallyMissingProjects(result.resultMap, remoteProjects, localProjects);
            } catch (Exception e) {
                result.exception = e;
            }
            return result;
        }

        private void addLocallyMissingProjects(Map<ProjectInfo, CloudResult> cloudResultMap, ProjectList remoteProjects, List<File> localProjects) throws Exception {
            searchLocalDir:
            for (ProjectItem remoteProject : remoteProjects.getProjectItems()) {
                for (File localProject : localProjects) {
                    if (localProject.getName().equals(remoteProject.getProjectName())) {
                        continue searchLocalDir;
                    }
                }

                ProjectInfo newProject = DefaultProjects.createProjectDir(remoteProject);
                if (newProject == null)
                    continue;

                cloudResultMap.put(
                        newProject,
                        MobileCloudController.super.doRefreshNoNotify(
                                mobileCloudWindow.getServerUrl(),
                                newProject
                        )
                );
            }
        }

        private void addExistingProjects(Map<ProjectInfo, CloudResult> cloudResultMap, ProjectList remoteProjects, List<File> localProjects) throws Exception {
            for (File dir : localProjects) {
                if (!remoteProjects.containsProjectName(dir.getName())) {
                    Log.i(TAG, "Found unknown project: " + dir);
                    continue;
                }

                ProjectInfo projectInfo = new ProjectInfo(dir);
                cloudResultMap.put(
                        projectInfo,
                        MobileCloudController.super.doRefreshNoNotify(
                                mobileCloudWindow.getServerUrl(),
                                projectInfo
                        )
                );
            }
        }

        @Override
        protected void onPostExecute(TaskResult result) {
            if (result.exception != null) {
                mobileCloudWindow.exceptionOccurred(result.exception);
                return;
            }
            mobileCloudWindow.refreshBegin();
            for (ProjectInfo projectInfo : result.resultMap.keySet())
                cloudWindow.updateResult(projectInfo, result.resultMap.get(projectInfo));
            mobileCloudWindow.refreshFinished();
        }
    }

}
