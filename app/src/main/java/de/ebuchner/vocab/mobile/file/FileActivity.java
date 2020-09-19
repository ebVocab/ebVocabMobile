package de.ebuchner.vocab.mobile.file;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import de.ebuchner.toolbox.i18n.I18NContext;
import de.ebuchner.vocab.mobile.R;
import de.ebuchner.vocab.mobile.common.MessageDialogs;
import de.ebuchner.vocab.mobile.model.file.FileActivityBehaviour;
import de.ebuchner.vocab.mobile.model.file.FileActivityController;
import de.ebuchner.vocab.mobile.model.file.FileActivityMode;
import de.ebuchner.vocab.mobile.model.file.FileActivityModel;
import de.ebuchner.vocab.mobile.platform.MobileUIPlatform;
import de.ebuchner.vocab.nui.common.I18NLocator;

import java.io.File;
import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;

public class FileActivity extends Activity {

    public static final String FILE_ACTIVITY_MODE_PARAM = "de.ebuchner.vocab.mobile.file.FileActivityModeParam";
    public static final String FILE_ACTIVITY_RESULT_PARAM = "de.ebuchner.vocab.mobile.file.FileActivityResultParam";
    public static final String FILE_ACTIVITY_RESULT = "de.ebuchner.vocab.mobile.file.FileActivityResult";
    private I18NContext i18n = I18NLocator.locate();
    private FileActivityMode mode = FileActivityMode.OPEN;
    private Button btnOk;
    private EditText etFileDir;
    private Button btnDirUp;
    private FileActivityController controller;
    private ArrayAdapter<DirectoryContentItem> dirContentAdapter;
    private EditText etSelFile;
    private Button btnDirCreate;

    private String titleResourceKey() {
        return mode == FileActivityMode.OPEN ? "nui.file.open.title" : "nui.file.save.title";
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String modeString = getIntent().getExtras().getString(FILE_ACTIVITY_MODE_PARAM);
        if (modeString != null)
            mode = FileActivityMode.valueOf(modeString);

        controller = new FileActivityController(new FileActivityModel(mode), new MyBehaviour());

        setTitle(i18n.getString(titleResourceKey()));

        setContentView(R.layout.file);

        btnOk = (Button) findViewById(R.id.file_ok);
        btnOk.setText(i18n.getString(
                mode == FileActivityMode.OPEN ?
                        "nui.file.open" : "nui.file.save"
        ));
        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                controller.onOk();
            }
        });

        if (mode == FileActivityMode.SAVE)
            btnOk.setEnabled(true);

        Button btnCancel = (Button) findViewById(R.id.file_cancel);
        btnCancel.setText(i18n.getString("nui.cancel"));
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                controller.onCancel();
            }
        });

        TextView tvFileDirLabel = (TextView) findViewById(R.id.file_dir_label);
        tvFileDirLabel.setText(i18n.getString("nui.file.dir.label"));

        etFileDir = (EditText) findViewById(R.id.file_dir);

        btnDirUp = (Button) findViewById(R.id.file_dir_up);
        btnDirUp.setText(i18n.getString("nui.file.dir.up"));
        btnDirUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                controller.onDirUp();
            }
        });

        TextView tvDirContentLabel = (TextView) findViewById(R.id.file_dir_content_label);
        tvDirContentLabel.setText(i18n.getString("nui.file.dir.content.label"));

        ListView lvDirContent = (ListView) findViewById(R.id.file_dir_content);
        dirContentAdapter = new ArrayAdapter<DirectoryContentItem>(
                this, R.layout.file_item
        );
        lvDirContent.setAdapter(dirContentAdapter);
        lvDirContent.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                if (position == ListView.INVALID_POSITION)
                    return;
                controller.onDirectoryContentSelection(
                        dirContentAdapter.getItem(position).getFile()
                );
            }
        });
        lvDirContent.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);

        TextView tvSelFile = (TextView) findViewById(R.id.file_sel_file_label);
        tvSelFile.setText(i18n.getString("nui.file.sel.file.label"));

        etSelFile = (EditText) findViewById(R.id.file_sel_file);
        etSelFile.setEnabled(mode == FileActivityMode.SAVE);

        btnDirCreate = (Button) findViewById(R.id.file_dir_create);
        btnDirCreate.setText(i18n.getString("nui.file.dir.create"));
        btnDirCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MessageDialogs.showInputTextDialog(
                        FileActivity.this,
                        i18n.getString("nui.file.dir.create.title"),
                        new MessageDialogs.InputDialogListener() {
                            @Override
                            public void onInputOk(String input) {
                                if (controller.onDirectoryCreate(input))
                                    MobileUIPlatform.instance().getMobileNuiDirector().showToast(
                                            i18n.getString("nui.file.dir.created")
                                    );
                            }

                            @Override
                            public void onInputCanceled() {

                            }
                        }
                );
            }
        });

        controller.onUIReady();
    }

    private Iterable<File> sortFiles(List<File> files) {
        TreeSet<File> helper = new TreeSet<File>(new Comparator<File>() {
            @Override
            public int compare(File lhs, File rhs) {
                if (lhs.isFile() && rhs.isDirectory())
                    return 1;
                else if (lhs.isDirectory() && rhs.isFile())
                    return -1;
                else return lhs.getName().compareTo(rhs.getName());
            }
        });
        helper.addAll(files);
        return helper;
    }

    private class MyBehaviour implements FileActivityBehaviour {

        @Override
        public void onModelChanged(FileActivityModel model) {
            try {
                etFileDir.setText(model.getSelectedDir().getCanonicalPath());
            } catch (IOException e) {
                e.printStackTrace();
                etFileDir.setText(model.getSelectedDir().getAbsolutePath());
            }

            btnDirUp.setEnabled(model.isUpDirEnabled());
            if (mode == FileActivityMode.OPEN)
                btnOk.setEnabled(model.hasSelectedFile());

            btnDirCreate.setEnabled(model.isDirCreatedEnabled());

            dirContentAdapter.clear();
            for (File file : sortFiles(model.selectedDirFiles()))
                dirContentAdapter.add(new DirectoryContentItem(file));

            if (model.hasSelectedFile())
                etSelFile.setText(model.getSelectedFile().getName());
            else
                etSelFile.setText("");
        }

        @Override
        public void sendCancelToCallerAndExit() {
            setResult(Activity.RESULT_CANCELED, new Intent(FILE_ACTIVITY_RESULT));
            finish();
        }

        @Override
        public void sendFileToCallerAndExit(File file, FileActivityMode mode) {
            Intent result = new Intent(FILE_ACTIVITY_RESULT);
            try {
                result.putExtra(FILE_ACTIVITY_RESULT_PARAM, file.getCanonicalPath());
                result.putExtra(FILE_ACTIVITY_MODE_PARAM, mode.name());
            } catch (IOException e) {
                e.printStackTrace();
                result.putExtra(FILE_ACTIVITY_RESULT_PARAM, file.getAbsolutePath());
            }
            setResult(Activity.RESULT_OK, result);
            finish();
        }

        @Override
        public String getFileNameInput() {
            return etSelFile.getText().toString();
        }

        @Override
        public void showMessage(String message) {
            MobileUIPlatform.instance().getMobileNuiDirector().showToast(message);
        }
    }

    private class DirectoryContentItem {
        private final File file;

        private DirectoryContentItem(File file) {
            this.file = file;
        }

        public File getFile() {
            return file;
        }

        @Override
        public String toString() {
            if (file.isDirectory())
                return String.format("[%s]", file.getName());
            return file.getName();
        }
    }
}
