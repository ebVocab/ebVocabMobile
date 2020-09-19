package de.ebuchner.vocab.mobile.cloud;

import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.UnderlineSpan;
import android.util.SparseBooleanArray;
import android.view.*;
import android.widget.*;
import de.ebuchner.vocab.config.ProjectInfo;
import de.ebuchner.vocab.mobile.R;
import de.ebuchner.vocab.mobile.common.VocabBaseActivity;
import de.ebuchner.vocab.mobile.platform.MobileUIPlatform;
import de.ebuchner.vocab.model.cloud.*;
import de.ebuchner.vocab.model.nui.WindowType;
import de.ebuchner.vocab.model.nui.platform.UIPlatformFactory;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CloudActivity extends VocabBaseActivity {

    private static Logger logger = Logger.getLogger(CloudActivity.class.getName());
    private ConnectionParameters connectionParameters = new ConnectionParameters();
    private MobileCloudController cloudController;
    private CloudActivityController controller = new CloudActivityController();
    private ListView lvFileList;
    private Map<ProjectInfo, FileListDiffer> differMap = new HashMap<ProjectInfo, FileListDiffer>();
    private Button btnTransfer;
    private MobileCloudWindowBehaviour myBehaviour = new MyBehaviour();
    private Button btnRefresh;
    private MenuItem miProject;
    private TextView lbServerInfo;
    private ArrayAdapter<FileListActionItem> fileListAdapter;
    private Button btnAllNone;
    private RadioButton rbStreamUp;
    private RadioButton rbStreamDown;

    @Override
    protected String titleResourceKey() {
        return "nui.cloud.title";
    }

    @Override
    protected void onCreateImpl(Bundle savedInstanceState) {
        setContentView(R.layout.cloud);

        cloudController = new MobileCloudController(myBehaviour);

        TextView lbFileList = (TextView) findViewById(R.id.cloud_lbFileList);
        lbFileList.setText(i18n.getString("nui.cloud.fileList"));

        lbServerInfo = (TextView) findViewById(R.id.cloud_lbServerInfo);

        lvFileList = (ListView) findViewById(R.id.cloud_lvFileList);

        btnRefresh = (Button) findViewById(R.id.cloud_btnRefresh);
        btnRefresh.setText(i18n.getString("nui.refresh"));
        btnRefresh.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                controller.onRefresh();
            }
        });

        btnAllNone = (Button) findViewById(R.id.cloud_btnAllNone);
        btnAllNone.setText(i18n.getString("nui.cloud.all.none.all"));
        btnAllNone.setEnabled(false);
        btnAllNone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                controller.onAllNone();
            }
        });

        btnTransfer = (Button) findViewById(R.id.cloud_btnTransfer);
        btnTransfer.setText(i18n.getString("nui.cloud.transfer"));
        btnTransfer.setOnClickListener(
                new View.OnClickListener() {
                    public void onClick(View v) {
                        controller.onTransfer();
                    }
                }
        );

        View.OnClickListener rbClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                controller.onCloudTransferChanged();
            }
        };

        rbStreamUp = (RadioButton) findViewById(R.id.cloud_rbUpStream);
        rbStreamUp.setText(i18n.getString("nui.cloud.stream.up"));
        rbStreamUp.setEnabled(false);
        rbStreamUp.setOnClickListener(rbClickListener);

        rbStreamDown = (RadioButton) findViewById(R.id.cloud_rbDownStream);
        rbStreamDown.setText(i18n.getString("nui.cloud.stream.down"));
        rbStreamDown.setEnabled(false);
        rbStreamDown.setOnClickListener(rbClickListener);

        updateServerInfo();
    }

    private void updateServerInfo() {
        final boolean connectionAvailable = connectionParameters.hasValues();

        String info =
                i18n.getString(
                        connectionAvailable ?
                                "nui.cloud.server.info.connected" :
                                "nui.cloud.server.info.empty"
                );
        Spannable infoSpan = new SpannableString(info);
        infoSpan.setSpan(new UnderlineSpan(), 0, info.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        lbServerInfo.setText(infoSpan);
        lbServerInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                controller.showConnectDialog();
            }
        });

        btnRefresh.setEnabled(connectionAvailable);
        if (btnTransfer.isEnabled())
            btnTransfer.setEnabled(connectionAvailable);
        btnAllNone.setEnabled(connectionAvailable && lvFileList.getCount() > 0);
        rbStreamUp.setEnabled(connectionAvailable);
        rbStreamUp.setEnabled(connectionAvailable);
    }

    protected void onResumeImpl() {
        controller.restoreAuthenticationInput();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.cloud_option_menu, menu);

        miProject = menu.findItem(R.id.cloud_open_projects);
        miProject.setTitle(i18n.getString("nui.menu.tools.project"));

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.cloud_open_projects:
                UIPlatformFactory.getUIPlatform().getNuiDirector().showWindow(WindowType.PROJECT_WINDOW);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void mergeDiffer(ProjectInfo projectInfo, FileListDiffer newDiffer) {
        FileListDiffer oldDiffer = differMap.get(projectInfo);
        if (oldDiffer != null)
            oldDiffer.mergeWith(newDiffer);
        else
            differMap.put(projectInfo, newDiffer);

    }

    private CloudTransfer cloudTransfer() {
        if (rbStreamUp.isChecked())
            return CloudTransfer.UPLOAD;
        return CloudTransfer.DOWNLOAD;
    }

    private static class ConnectionParameters {
        String server;
        String user;
        String secret;

        boolean hasValues() {
            return notEmpty(server) && notEmpty(user) && notEmpty(secret);
        }

        private boolean notEmpty(String s) {
            return s != null && s.trim().length() > 0;
        }
    }

    class CloudActivityController {

        private PopupWindow connectPopup;

        private void activateIO(String msgKey) {
            MobileUIPlatform.instance().getMobileNuiDirector().showToast(i18n.getString(msgKey));
            btnTransfer.setEnabled(false);
            btnAllNone.setEnabled(false);
            btnRefresh.setEnabled(false);
            rbStreamUp.setEnabled(false);
            rbStreamDown.setEnabled(false);
            if (miProject != null)
                miProject.setEnabled(false);
        }

        private void deActivateIO() {
            btnRefresh.setEnabled(true);
            btnAllNone.setEnabled(lvFileList.getCount() > 0);
            rbStreamUp.setEnabled(lvFileList.getCount() > 0);
            rbStreamDown.setEnabled(lvFileList.getCount() > 0);
            if (miProject != null)
                miProject.setEnabled(true);
        }

        void onRefresh() {
            activateIO("nui.cloud.refreshing");
            try {
                cloudController.doRefresh();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        void onTransfer() {
            activateIO("nui.cloud.transferring");
            try {
                cloudController.doTransferCloud(cloudTransfer(), createSelectionMap());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        private Map<ProjectInfo, List<FileListAction>> createSelectionMap() {
            Map<ProjectInfo, List<FileListAction>> map = new HashMap<>();
            SparseBooleanArray selection = lvFileList.getCheckedItemPositions();
            for (int i = 0; i < selection.size(); i++) {
                FileListActionItem item =
                        fileListAdapter.getItem(selection.keyAt(i));

                List<FileListAction> actions = map.get(item.getProjectInfo());
                if (actions == null) {
                    actions = new ArrayList<>();
                    map.put(item.getProjectInfo(), actions);
                }
                actions.add(item.getAction());
            }
            return map;
        }

        private ArrayAdapter<FileListActionItem> createAdapter() {
            return new ArrayAdapter<FileListActionItem>(
                    CloudActivity.this,
                    R.layout.cloud_item
            );
        }

        void restoreAuthenticationInput() {
            VocabCloud.AuthenticationInput input = VocabCloud.restoreAuthenticationInput();
            connectionParameters.server = input.getServer();
            connectionParameters.user = input.getUser();
            connectionParameters.secret = input.getSecret();

            updateServerInfo();
        }

        void saveAuthenticationInput() {
            VocabCloud.saveAuthenticationInput(
                    connectionParameters.server,
                    connectionParameters.user,
                    connectionParameters.secret
            );
        }

        void showConnectDialog() {

            View connectView = getLayoutInflater().inflate(R.layout.cloud_connect, null, false);

            connectPopup = new PopupWindow(connectView,
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT,
                    true);

            connectPopup.setTouchable(true);
            connectPopup.setFocusable(true);

            connectPopup.showAtLocation(connectView, Gravity.CENTER, 0, 0);

            TextView lbServer = (TextView) connectView.findViewById(R.id.cloud_lbServer);
            lbServer.setText(i18n.getString("nui.cloud.server"));

            TextView tfServer = (TextView) connectView.findViewById(R.id.cloud_tfServer);
            if (connectionParameters.hasValues())
                tfServer.setText(connectionParameters.server);

            TextView lbUser = (TextView) connectView.findViewById(R.id.cloud_lbUser);
            lbUser.setText(i18n.getString("nui.cloud.user"));

            TextView tfUser = (TextView) connectView.findViewById(R.id.cloud_tfUser);
            if (connectionParameters.hasValues())
                tfUser.setText(connectionParameters.user);

            TextView lbPass = (TextView) connectView.findViewById(R.id.cloud_lbPass);
            lbPass.setText(i18n.getString("nui.cloud.secret"));

            TextView tfPass = (TextView) connectView.findViewById(R.id.cloud_tfPass);
            if (connectionParameters.hasValues())
                tfPass.setText(connectionParameters.secret);

            Button btnServerConnect = (Button) connectView.findViewById(R.id.cloud_btnServerConnect);
            btnServerConnect.setText(i18n.getString("nui.cloud.server.connect"));
            btnServerConnect.setOnClickListener(new View.OnClickListener() {
                public void onClick(View arg0) {
                    connectionParameters.server = tfServer.getText().toString();
                    connectionParameters.user = tfUser.getText().toString();
                    connectionParameters.secret = tfPass.getText().toString();

                    if (!connectionParameters.hasValues()) {
                        MobileUIPlatform.instance().getMobileNuiDirector().showToast(
                                i18n.getString("nui.cloud.connect.empty")
                        );
                        return;
                    }
                    closeConnectDialog();
                }
            });

            Button btnServerCancel = (Button) connectView.findViewById(R.id.cloud_btnServerCancel);
            btnServerCancel.setText(i18n.getString("nui.cancel"));
            btnServerCancel.setOnClickListener(new View.OnClickListener() {
                public void onClick(View arg0) {
                    closeConnectDialog();
                }
            });
        }

        private void closeConnectDialog() {
            updateServerInfo();
            connectPopup.dismiss();
        }

        void onFileItemSelectionChanged(FileListActionItem itemAtPosition) {
            btnTransfer.setEnabled(lvFileList.getCheckedItemCount() > 0);
            btnAllNone.setEnabled(lvFileList.getCount() > 0);
            rbStreamDown.setEnabled(lvFileList.getCount() > 0);
            rbStreamUp.setEnabled(lvFileList.getCount() > 0);

            refreshLabelBtnAllNone();
        }

        private void refreshLabelBtnAllNone() {
            String labelKey = "nui.cloud.all.none.all";
            if (lvFileList.getCheckedItemCount() > 0)
                labelKey = "nui.cloud.all.none.none";
            btnAllNone.setText(i18n.getString(labelKey));
        }

        void onAllNone() {
            if (lvFileList.getCheckedItemCount() > 0) {
                lvFileList.clearChoices();
                lvFileList.requestLayout();
            } else {
                int numEntries = fileListAdapter.getCount();
                for (int i = 0; i < numEntries; i++) {
                    lvFileList.setItemChecked(i, true);
                }
            }
            refreshLabelBtnAllNone();
            if (!btnTransfer.isEnabled())
                btnTransfer.setEnabled(connectionParameters.hasValues() && lvFileList.getCheckedItemCount() > 0);
        }

        void onCloudTransferChanged() {
            myBehaviour.refreshFinished();
        }
    }

    class MyBehaviour extends MobileCloudWindowBehaviour {
        private RefreshInfo refreshInfo;
        private ProjectStatusInfo projectStatusInfo;

        public boolean confirmOverwrite(CloudTransfer cloudTransfer) {
            // at the moment there is no need to preserve local files on mobile platforms
            return true;
        }

        public String getUserName() {
            return connectionParameters.user;
        }

        @Override
        public String getServerUrl() {
            return connectionParameters.server;
        }

        @Override
        public void exceptionOccurred(Exception exception) {
            logger.log(Level.WARNING, "", exception);
            MobileUIPlatform.instance().getMobileNuiDirector().showToast(
                    i18n.getString("nui.cloud.transfer.error")
            );
            controller.deActivateIO();
        }

        public String getSecret() {
            return connectionParameters.secret;
        }

        @Override
        protected void onNewDownloadDiffer(ProjectInfo projectInfo, FileListDiffer differ) {
            refreshInfo.put(projectInfo, differ);
            mergeDiffer(projectInfo, differ);
        }

        @Override
        protected void onUpdateProjectStatus(ProjectInfo projectInfo, ProjectStatus projectStatus) {
            projectStatusInfo.put(projectInfo, projectStatus);

        }

        @Override
        protected void onUpdateStatusMessage(ProjectInfo projectInfo, String statusMessage) {
            MobileUIPlatform.instance().getMobileNuiDirector().showToast(
                    String.format("%s: %s", projectInfo.getName(), statusMessage)
            );
        }

        private void updateStatusMessage() {
            StringBuilder statusText = new StringBuilder();
            for (ProjectInfo projectInfo : projectStatusInfo.projectInfoList())
                switch (projectStatusInfo.getStatus(projectInfo)) {
                    case ERROR:
                        if (statusText.length() > 0)
                            statusText.append(" - ");
                        statusText.append(i18n.getString(
                                "nui.cloud.project.status.error",
                                Collections.singletonList(projectInfo.getName())
                        ));
                        break;
                    case FILE_LIST_REFRESHED:
                        if (statusText.length() > 0)
                            statusText.append(" - ");
                        statusText.append(i18n.getString(
                                "nui.cloud.project.status.refreshed",
                                Collections.singletonList(projectInfo.getName())
                        ));
                        break;
                    case NOTHING_CHANGED:
                        break;
                    case PROJECT_UPDATED:
                        if (statusText.length() > 0)
                            statusText.append(" - ");
                        statusText.append(i18n.getString(
                                "nui.cloud.project.status.updated",
                                Collections.singletonList(projectInfo.getName())
                        ));
                        break;
                    default:
                        throw new RuntimeException("Unknown project status " + projectStatusInfo.getStatus(projectInfo));
                }
            controller.deActivateIO();
            if (statusText.length() > 0)
                MobileUIPlatform.instance().getMobileNuiDirector().showToast(statusText.toString());
            else
                MobileUIPlatform.instance().getMobileNuiDirector().showToast(
                        i18n.getString("nui.cloud.result.upload.differ.0")
                );
            controller.saveAuthenticationInput();
        }

        @Override
        protected void onNewUploadDiffer(ProjectInfo projectInfo, FileListDiffer differ) {
            mergeDiffer(projectInfo, differ);
        }

        @Override
        public void refreshBegin() {
            initInfo();
        }

        @Override
        public void downloadBegin() {
            initInfo();
        }

        private void initInfo() {
            projectStatusInfo = new ProjectStatusInfo();
            refreshInfo = new RefreshInfo();
        }

        @Override
        public void refreshFinished() {
            fileListAdapter = controller.createAdapter();

            int actionCount = 0;
            for (ProjectInfo projectInfo : refreshInfo.projectInfoList()) {
                FileListDiffer differ = refreshInfo.getDiffer(projectInfo);
                actionCount += differ.getActions().size();

                for (FileListAction action : differ.getActions()) {
                    fileListAdapter.add(new FileListActionItem(projectInfo, action, cloudTransfer()));
                }
            }
            lvFileList.setAdapter(fileListAdapter);
            lvFileList.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
            lvFileList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                    controller.onFileItemSelectionChanged(
                            (FileListActionItem) adapterView.getItemAtPosition(position)
                    );
                }
            });

            controller.saveAuthenticationInput();
            updateStatusMessage();
        }

        @Override
        public void downloadFinished() {
            updateStatusMessage();
        }
    }

    class ProjectStatusInfo {
        Map<ProjectInfo, ProjectStatus> info =
                new HashMap<ProjectInfo, ProjectStatus>();

        public void put(ProjectInfo projectInfo, ProjectStatus projectStatus) {
            info.put(projectInfo, projectStatus);
        }

        public Iterable<ProjectInfo> projectInfoList() {
            return info.keySet();
        }

        public ProjectStatus getStatus(ProjectInfo projectInfo) {
            return info.get(projectInfo);
        }
    }

    class RefreshInfo {
        Map<ProjectInfo, FileListDiffer> info = new HashMap<ProjectInfo, FileListDiffer>();

        public void put(ProjectInfo projectInfo, FileListDiffer differ) {
            info.put(projectInfo, differ);
        }

        public Iterable<ProjectInfo> projectInfoList() {
            return info.keySet();
        }

        public FileListDiffer getDiffer(ProjectInfo projectInfo) {
            return info.get(projectInfo);
        }
    }
}
