package de.ebuchner.vocab.mobile.editor;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import de.ebuchner.vocab.config.fields.FieldFactory;
import de.ebuchner.vocab.mobile.R;
import de.ebuchner.vocab.mobile.common.MessageDialogs;
import de.ebuchner.vocab.mobile.common.VocabBaseActivity;
import de.ebuchner.vocab.mobile.file.FileActivity;
import de.ebuchner.vocab.mobile.model.file.FileActivityMode;
import de.ebuchner.vocab.mobile.platform.MobileUIPlatform;
import de.ebuchner.vocab.model.editor.*;
import de.ebuchner.vocab.model.lessons.entry.VocabEntry;
import de.ebuchner.vocab.model.nui.WindowType;
import de.ebuchner.vocab.nui.NuiDirector;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// todo 2nd step ersetzen und Entscheidung als Parameter mitgeben lassen (wie in clear repetition)

public class EditorActivity extends VocabBaseActivity {
    private MobileEditorController editorController;
    private EditText tfForeign;
    private EditText tfType;
    private EditText tfUser;
    private EditText tfComment;
    private ImageButton btnEntryDelete;
    private ImageButton btnEntryUp;
    private ImageButton btnEntryDown;
    private ImageButton btnEntryAccept;
    private ImageButton btnEntryRevert;
    private ListView vocabList;
    private ArrayAdapter<VocabListHolder> vocabListAdapter;
    private MenuItem miFileNew;
    private MenuItem miFileOpen;
    private MenuItem miFileSave;
    private MenuItem miFileSaveAs;
    private MenuItem miFileRevert;
    private TextView lbForeign;
    private TextView lbType;
    private TextView lbUser;
    private TextView lbComment;

    private Button btnSwapScreens;

    @Override
    protected String titleResourceKey() {
        return "nui.editor.title";
    }

    protected void onCreateImpl(Bundle savedInstanceState) {
        setContentView(R.layout.editor);

        btnSwapScreens = (Button) findViewById(R.id.editor_btnSwapScreens);
        if (btnSwapScreens != null)
            btnSwapScreens.setOnClickListener(new SwapScreens());
        TextView tfListTitle = (TextView) findViewById(R.id.editor_tfListTitle);
        if (tfListTitle != null)
            tfListTitle.setText(i18n.getString("nui.editor.list.title"));

        EditorWindowBehaviour myBehaviour = new MyBehaviour();
        EntryInEditWindowBehaviour myInEditBehaviour = new MyInEditBehaviour();
        editorController = new MobileEditorController(myBehaviour, myInEditBehaviour);

        Typeface typeface = getDefaultTypeface();
        NuiDirector nuiDirector = MobileUIPlatform.instance().getNuiDirector();

        lbForeign = (TextView) findViewById(R.id.editor_lbForeign);
        lbForeign.setText(nuiDirector.uiDescription(FieldFactory.getGenericField(FieldFactory.FOREIGN)));

        tfForeign = (EditText) findViewById(R.id.editor_tfForeign);
        if (typeface != null)
            tfForeign.setTypeface(typeface);

        lbType = (TextView) findViewById(R.id.editor_lbType);
        lbType.setText(nuiDirector.uiDescription(FieldFactory.getGenericField(FieldFactory.TYPE)));

        tfType = (EditText) findViewById(R.id.editor_tfType);
        if (typeface != null)
            tfType.setTypeface(typeface);

        lbUser = (TextView) findViewById(R.id.editor_lbUser);
        lbUser.setText(nuiDirector.uiDescription(FieldFactory.getGenericField(FieldFactory.USER)));

        tfUser = (EditText) findViewById(R.id.editor_tfUser);
        if (typeface != null)
            tfUser.setTypeface(typeface);

        lbComment = (TextView) findViewById(R.id.editor_lbComment);
        lbComment.setText(nuiDirector.uiDescription(FieldFactory.getGenericField(FieldFactory.COMMENT)));

        tfComment = (EditText) findViewById(R.id.editor_tfComment);
        if (typeface != null)
            tfComment.setTypeface(typeface);

        ImageButton btnEntryAdd = (ImageButton) findViewById(R.id.editor_btnAdd);
        //btnEntryAdd.setText(i18n.getString("nui.editor.entry.add"));
        btnEntryAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editorController.onEntryAdd();
            }
        });

        btnEntryDelete = (ImageButton) findViewById(R.id.editor_btnDelete);
        //btnEntryDelete.setText(i18n.getString("nui.editor.entry.delete"));
        btnEntryDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editorController.onEntriesDelete();
            }
        });

        btnEntryUp = (ImageButton) findViewById(R.id.editor_btnUp);
        //btnEntryUp.setText(i18n.getString("nui.editor.entry.up"));
        btnEntryUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editorController.onEntryMove(EditorFileModel.MoveEntryDirection.UP);
            }
        });

        btnEntryDown = (ImageButton) findViewById(R.id.editor_btnDown);
        //btnEntryDown.setText(i18n.getString("nui.editor.entry.down"));
        btnEntryDown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editorController.onEntryMove(EditorFileModel.MoveEntryDirection.DOWN);
            }
        });

        btnEntryAccept = (ImageButton) findViewById(R.id.editor_btnAccept);
        //btnEntryAccept.setText(i18n.getString("nui.editor.entry.accept"));
        btnEntryAccept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editorController.onEntryAccept();
            }
        });
        btnEntryAccept.setEnabled(false);

        btnEntryRevert = (ImageButton) findViewById(R.id.editor_btnRevert);
        //btnEntryRevert.setText(i18n.getString("nui.editor.entry.revert"));
        btnEntryRevert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editorController.onEntryRevert();
            }
        });
        btnEntryRevert.setEnabled(false);

        vocabList = (ListView) findViewById(R.id.editor_vocabList);
        vocabListAdapter = new ArrayAdapter<VocabListHolder>(
                this, R.layout.editor_item
        );
        vocabList.setAdapter(vocabListAdapter);
        vocabList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                List<VocabEntry> selectedEntries = new ArrayList<VocabEntry>();

                SparseBooleanArray selection = vocabList.getCheckedItemPositions();
                for (int i = 0; i < selection.size(); i++) {
                    selectedEntries.add(
                            vocabListAdapter.getItem(selection.keyAt(i)).getVocabEntry()
                    );
                }

                editorController.onTableSelectionChanged(selectedEntries);
            }
        });
        vocabList.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE);
    }

    private String titleFromFile(File currentFile) {
        if (currentFile == null)
            return generateActivityTitle();

        String fileName = i18n.getString(
                "nui.editor.title.some.file",
                Collections.singletonList(currentFile.getName())
        );

        return generateActivityTitleExtra(fileName);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.editor_option_menu, menu);

        MenuItem miProjects = menu.findItem(R.id.ed_open_projects);
        miProjects.setTitle(i18n.getString("nui.menu.tools.project"));

        MenuItem miPractice = menu.findItem(R.id.ed_open_practice);
        miPractice.setTitle(i18n.getString("nui.menu.tools.practice"));

        MenuItem miFile = menu.findItem(R.id.editor_file);
        miFile.setTitle(i18n.getString("nui.menu.file"));

        miFileNew = menu.findItem(R.id.editor_new);
        miFileNew.setTitle(i18n.getString("nui.menu.file.new"));

        miFileOpen = menu.findItem(R.id.editor_open);
        miFileOpen.setTitle(i18n.getString("nui.menu.file.open"));

        miFileSave = menu.findItem(R.id.editor_save);
        miFileSave.setTitle(i18n.getString("nui.menu.file.save"));

        miFileSaveAs = menu.findItem(R.id.editor_save_as);
        miFileSaveAs.setTitle(i18n.getString("nui.menu.file.save.as"));

        miFileRevert = menu.findItem(R.id.editor_revert);
        miFileRevert.setTitle(i18n.getString("nui.menu.file.revert"));

        editorController.onWindowWasCreated();

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.editor_new:
                editorController.onFileNew();
                return true;
            case R.id.editor_open:
                editorController.onFileOpen();
                return true;
            case R.id.editor_save:
                if (editorController.onFileSave())
                    MobileUIPlatform.instance().getMobileNuiDirector().showToast(
                            i18n.getString("nui.editor.file.saved")
                    );
                return true;
            case R.id.editor_save_as:
                if (editorController.onFileSaveAs()) {
                    MobileUIPlatform.instance().getMobileNuiDirector().showToast(
                            i18n.getString("nui.editor.file.saved")
                    );
                }
                return true;
            case R.id.editor_revert:
                editorController.onFileRevert();
                return true;
            case R.id.ed_open_practice:
                editorController.onOpenWindowType(WindowType.PRACTICE_WINDOW);
                return true;
            case R.id.ed_open_projects:
                editorController.onOpenWindowType(WindowType.PROJECT_WINDOW);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent returnIntent) {
        if (resultCode != RESULT_OK)
            return;

        FileActivityMode mode = FileActivityMode.valueOf(
                returnIntent.getExtras().getString(FileActivity.FILE_ACTIVITY_MODE_PARAM)
        );

        String fileStringFromFileActivity =
                returnIntent.getExtras().getString(FileActivity.FILE_ACTIVITY_RESULT_PARAM);

        File file = new File(fileStringFromFileActivity);

        if (mode == FileActivityMode.OPEN)
            handleOpenResult(file);
        else
            handleSaveResult(file);
    }

    private void handleSaveResult(File file) {
        if (file.isDirectory()) {
            MessageDialogs.showMessageBox(
                    this,
                    i18n.getString(
                            "nui.file.open.error",
                            Collections.singletonList(file.getAbsolutePath())
                    )
            );
            return;
        }

        if (file.exists()) {
            MessageDialogs.showConfirmBox(
                    this,
                    i18n.getString("nui.editor.ask.overwrite"),
                    new MessageDialogs.ConfirmDialogListener() {
                        @Override
                        public void onConfirmAccepted() {
                            if (editorController.onFileSave2ndStep(file))
                                showToast(i18n.getString("nui.editor.file.saved"));
                        }

                        @Override
                        public void onConfirmCanceled() {

                        }
                    }
            );
        } else
            editorController.onFileSave2ndStep(file);
    }

    private void handleOpenResult(File file) {
        if (!file.exists() || !file.isFile()) {
            MessageDialogs.showMessageBox(
                    this,
                    i18n.getString(
                            "nui.file.open.error",
                            Collections.singletonList(file.getAbsolutePath())
                    )
            );
            return;
        }

        if (editorController.onFileOpen2ndStep(file))
            showToast(i18n.getString("nui.editor.file.loaded"));
    }

    private class SwapScreens implements View.OnClickListener {
        boolean showFields = true;

        SwapScreens() {
            updateUI();
        }

        @Override
        public void onClick(View v) {
            showFields = !showFields;
            updateUI();
        }

        private void updateUI() {
            findViewById(R.id.editor_tableFields).setVisibility(
                    showFields ? View.VISIBLE : View.GONE
            );
            findViewById(R.id.editor_vocabList).setVisibility(
                    showFields ? View.GONE : View.VISIBLE
            );
            findViewById(R.id.editor_tfListTitle).setVisibility(
                    showFields ? View.GONE : View.VISIBLE
            );

            btnSwapScreens.setText(
                    i18n.getString(
                            showFields ?
                                    "nui.editor.show.list" : "nui.editor.show.fields"
                    )
            );
        }
    }

    private class ModelDecorator implements EditorFileModelBehaviour {
        final EditorFileModelBehaviour fileModel;

        public ModelDecorator(EditorFileModelBehaviour fileModel) {
            this.fileModel = fileModel;
        }

        private void onEditorFileModelChanged() {
            vocabListAdapter.clear();
            int entryCount = fileModel.entryCount();
            for (int i = 0; i < entryCount; i++) {
                VocabEntry entry = fileModel.getEntry(i);
                vocabListAdapter.add(new VocabListHolder(entry));
            }
        }

        private void onEditorEntryChanged(int rowNumber, VocabEntry modifiedEntry) {
            // not supported by adapter?
            onEditorFileModelChanged();
        }

        // EditorFileModelBehaviour
        public void addEntry(VocabEntry entry) {
            fileModel.addEntry(entry);
            onEditorFileModelChanged();
        }

        // EditorFileModelBehaviour
        public File getFile() {
            return fileModel.getFile();
        }

        // EditorFileModelBehaviour
        public void openFile(File file) {
            fileModel.openFile(file);
            onEditorFileModelChanged();
        }

        // EditorFileModelBehaviour
        public boolean hasFile() {
            return fileModel.hasFile();
        }

        // EditorFileModelBehaviour
        public void clearFile() {
            fileModel.clearFile();
            onEditorFileModelChanged();
        }

        // EditorFileModelBehaviour
        public boolean isDirty() {
            return fileModel.isDirty();
        }

        // EditorFileModelBehaviour
        public boolean isEmpty() {
            return fileModel.isEmpty();
        }

        // EditorFileModelBehaviour
        public void saveFile(File file) {
            fileModel.saveFile(file);
            onEditorFileModelChanged();
        }

        // EditorFileModelBehaviour
        public VocabEntry updateEntry(String id, VocabEntry entry) {
            VocabEntry modelEntry = fileModel.updateEntry(id, entry);
            onEditorEntryChanged(fileModel.indexOf(modelEntry) + 1, modelEntry);
            return modelEntry;
        }

        // EditorFileModelBehaviour
        public int entryCount() {
            return fileModel.entryCount();
        }

        // EditorFileModelBehaviour
        public VocabEntry getEntry(int pos) {
            return fileModel.getEntry(pos);
        }

        // EditorFileModelBehaviour
        public void deleteEntries(List<VocabEntry> entries) {
            fileModel.deleteEntries(entries);
            onEditorFileModelChanged();
        }

        // EditorFileModelBehaviour
        public void moveEntry(VocabEntry entry, EditorFileModel.MoveEntryDirection direction) {
            fileModel.moveEntry(entry, direction);
            onEditorFileModelChanged();
        }

        // EditorFileModelBehaviour
        public int indexOf(VocabEntry entry) {
            return fileModel.indexOf(entry);
        }

        // EditorFileModelBehaviour
        public VocabEntry findEntry(String id) {
            return fileModel.findEntry(id);
        }

    }

    private class MyBehaviour implements EditorWindowBehaviour {
        @Override
        public EditorFileModelBehaviour decorateModel(EditorFileModelBehaviour fileModel) {
            return new ModelDecorator(fileModel);
        }

        @Override
        public void setWindowTitle(File file) {
            setTitle(titleFromFile(file));
        }

        @Override
        public void setEntrySelection(EntrySelectionBehaviour entrySelection) {
            int singleSelection = -1;
            if (entrySelection.selectionCount() == 1)
                singleSelection = entrySelection.selectedModelEntryRows().iterator().next();

            btnEntryDelete.setEnabled(entrySelection.selectionCount() > 0);
            btnEntryUp.setEnabled(singleSelection > 0);
            btnEntryDown.setEnabled((singleSelection >= 0 && singleSelection < entrySelection.allEntriesCount() - 1));
        }

        @Override
        public void setSaveOptionEnabled(boolean enabled) {
            miFileSave.setEnabled(enabled);
            miFileRevert.setEnabled(enabled);
        }

        @Override
        public void setSaveAsOptionEnabled(boolean enabled) {
            miFileSaveAs.setEnabled(enabled);
        }

        @Override
        public boolean askToRevert() {
            MessageDialogs.showConfirmBox(
                    EditorActivity.this,
                    i18n.getString("nui.editor.msg.ask.to.revert"),
                    new MessageDialogs.ConfirmDialogListener() {
                        @Override
                        public void onConfirmAccepted() {
                            editorController.onFileRevert2ndStep();
                        }

                        @Override
                        public void onConfirmCanceled() {

                        }
                    }
            );

            return false;
        }

        @Override
        public void removeSingleRowSelectionAndFireNoEvents() {
            vocabList.clearChoices();
        }

        @Override
        public void sendMessageOpenFileFailed(Throwable t, File fileToOpen) {
            MessageDialogs.showMessageBox(
                    EditorActivity.this,
                    i18n.getString(
                            "nui.editor.msg.open.failed",
                            Collections.singletonList(fileToOpen.getName())
                    )
            );
        }

        @Override
        public void sendMessageFileLockFailed(File fileToOpen) {
            MessageDialogs.showMessageBox(
                    EditorActivity.this,
                    i18n.getString(
                            "nui.editor.msg.file.locked",
                            Collections.singletonList(fileToOpen.getName())
                    )
            );
        }

        @Override
        public void sendMessageEmptyFile() {
            MessageDialogs.showMessageBox(
                    EditorActivity.this,
                    i18n.getString(
                            "nui.editor.msg.file.empty"
                    )
            );
        }

        @Override
        public File openFileDialog(File preferredDirectory, String extension) {
            Intent intent = new Intent(EditorActivity.this, FileActivity.class);
            intent.putExtra(FileActivity.FILE_ACTIVITY_MODE_PARAM, FileActivityMode.OPEN.name());
            startActivityForResult(intent, 0);
            // return null for now because the startActivity() does not block
            return null;
        }

        @Override
        public File saveFileDialog(File preferredDirectory, String extension) {
            Intent intent = new Intent(EditorActivity.this, FileActivity.class);
            intent.putExtra(FileActivity.FILE_ACTIVITY_MODE_PARAM, FileActivityMode.SAVE.name());
            startActivityForResult(intent, 0);
            // return null for now because the startActivity() does not block
            return null;
        }

        @Override
        public AskToSaveResult askToSave() {
            MessageDialogs.showConfirmBox(
                    EditorActivity.this,
                    i18n.getString("nui.editor.msg.ask.to.save"),
                    new MessageDialogs.ConfirmDialogListener() {
                        @Override
                        public void onConfirmAccepted() {
                            editorController.checkModified2ndStep(AskToSaveResult.DO_SAVE);
                        }

                        @Override
                        public void onConfirmCanceled() {
                            editorController.checkModified2ndStep(AskToSaveResult.DO_IGNORE);
                        }
                    }
            );
            return AskToSaveResult.DO_CANCEL;
        }

        @Override
        public void sendMessageClipboardEmpty() {

        }
    }

    private class MyInEditBehaviour implements EntryInEditWindowBehaviour {
        @Override
        public VocabEntry entryFromUI() {
            VocabEntry vocabEntry = new VocabEntry();
            addFieldToEntry(vocabEntry, tfForeign, FieldFactory.FOREIGN);
            addFieldToEntry(vocabEntry, tfType, FieldFactory.TYPE);
            addFieldToEntry(vocabEntry, tfUser, FieldFactory.USER);
            addFieldToEntry(vocabEntry, tfComment, FieldFactory.COMMENT);

            return vocabEntry;
        }

        private void addFieldToEntry(VocabEntry vocabEntry, TextView textView, String fieldName) {
            String value = null;
            if (textView.getText() != null && textView.getText().toString().trim().length() > 0)
                value = textView.getText().toString().trim();

            if (value != null && value.length() > 0)
                vocabEntry.putFieldValue(fieldName, value);

        }

        @Override
        public void sendMessageFieldMissing() {
            MessageDialogs.showMessageBox(
                    EditorActivity.this,
                    i18n.getString(
                            "nui.editor.msg.field.missing"
                    )
            );
        }

        @Override
        public void entryToUI(VocabEntry entry) {
            entryToField(entry, lbForeign, tfForeign, FieldFactory.FOREIGN);
            entryToField(entry, lbType, tfType, FieldFactory.TYPE);
            entryToField(entry, lbUser, tfUser, FieldFactory.USER);
            entryToField(entry, lbComment, tfComment, FieldFactory.COMMENT);

            btnEntryRevert.setEnabled(entry != null);
            btnEntryAccept.setEnabled(entry != null);
        }

        private void entryToField(VocabEntry entry, TextView label, EditText editField, String fieldName) {
            String text = null;
            if (entry != null)
                text = entry.getFieldValue(fieldName);

            editField.setText(text);
            editField.setEnabled(entry != null);

            int activeColor =
                    fieldName.equals(FieldFactory.FOREIGN) ||
                            fieldName.equals(FieldFactory.USER) ?
                            getResources().getColor(R.color.indiaGreen) :
                            getResources().getColor(R.color.indiaBlue);

            label.setTextColor(
                    entry != null ?
                            activeColor :
                            getResources().getColor(R.color.moreShadow)
            );
        }
    }

    private class VocabListHolder {
        private final VocabEntry vocabEntry;
        private final String text;

        private VocabListHolder(VocabEntry vocabEntry) {
            this.vocabEntry = vocabEntry;

            StringBuilder textBuffer = new StringBuilder();
            textBuffer.append(vocabEntry.getFieldValue(FieldFactory.FOREIGN));
            textBuffer.append(" (");
            textBuffer.append(vocabEntry.getFieldValue(FieldFactory.USER));
            textBuffer.append(")");

            text = textBuffer.toString();
        }

        public VocabEntry getVocabEntry() {
            return vocabEntry;
        }

        @Override
        public String toString() {


            return text;
        }
    }
}
