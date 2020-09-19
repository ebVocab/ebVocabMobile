package de.ebuchner.vocab.mobile.lessons;

import android.os.Bundle;
import android.view.View;
import android.widget.*;
import de.ebuchner.vocab.mobile.R;
import de.ebuchner.vocab.mobile.common.MessageDialogs;
import de.ebuchner.vocab.mobile.common.VocabBaseActivity;
import de.ebuchner.vocab.model.lessons.*;
import de.ebuchner.vocab.model.nui.NuiCloseEvent;
import de.ebuchner.vocab.model.nui.WindowType;
import de.ebuchner.vocab.model.nui.platform.UIPlatformFactory;

import java.io.File;
import java.util.*;

public class LessonActivity extends VocabBaseActivity {
    private static final String FILE_SEPARATOR = System.getProperty("file.separator");
    private LessonController lessonController;
    private LessonActivityController lessonActivityController = new LessonActivityController();

    private Button buttonSelectAllFiles;
    private Button buttonSelectNoFiles;

    private ListView directorySelection;
    private ArrayAdapter<LessonContainerHolder> directorySelectionAdapter;
    private ListView lessonSelection;
    private ArrayAdapter<LessonReferenceHolder> lessonSelectionAdapter;
    private TextView lbHeaderFiles;
    private LessonFilter lessonFilter;

    @Override
    protected boolean requiresConfig() {
        return true;
    }

    @Override
    protected String titleResourceKey() {
        return "nui.lesson.title";
    }

    @Override
    protected void onCreateImpl(Bundle savedInstanceState) {
        setContentView(R.layout.lesson);
    }

    protected void onResumeImpl() {
        lessonController = new LessonController(new MyBehaviour());

        createHeaderRow();

        createDirectorySelection();

        createLessonSelection();

        createFooter();

        lessonController.onWindowWasCreated();
    }

    private void createFooter() {
        Button buttonOk = (Button) findViewById(R.id.lessons_buttonOk);
        buttonOk.setText(i18n.getString("nui.ok"));
        buttonOk.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                lessonActivityController.onOk();
            }
        });

        Button buttonCancel = (Button) findViewById(R.id.lessons_buttonCancel);
        buttonCancel.setText(i18n.getString("nui.cancel"));
        buttonCancel.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                lessonActivityController.onCancel();
            }
        });
    }

    private void createDirectorySelection() {
        directorySelection = (ListView) findViewById(R.id.lessons_directorySelection);
        directorySelectionAdapter = new ArrayAdapter<LessonContainerHolder>(
                this, R.layout.lesson_item
        );
        directorySelection.setAdapter(directorySelectionAdapter);
        directorySelection.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                lessonActivityController.onDirectorySelectionChanged(
                        (LessonContainerHolder) adapterView.getItemAtPosition(position)
                );
            }
        });
        directorySelection.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);

        TextView lbSelectDirectories = (TextView) findViewById(R.id.lessons_lbSelectDirectories);
        if (lbSelectDirectories != null)
            lbSelectDirectories.setText(i18n.getString("nui.lesson.select.directories"));

        Button buttonSelectAllDirectories = (Button) findViewById(R.id.lessons_buttonSelectAllDirectories);
        if (buttonSelectAllDirectories != null) {
            buttonSelectAllDirectories.setText(i18n.getString("nui.lesson.select.all.directories"));
            buttonSelectAllDirectories.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    lessonActivityController.onSelectAllDirectories();
                }
            });
        }

        Button buttonSelectNoDirectories = (Button) findViewById(R.id.lessons_buttonSelectNoDirectories);
        if (buttonSelectNoDirectories != null) {
            buttonSelectNoDirectories.setText(i18n.getString("nui.lesson.select.no.directories"));
            buttonSelectNoDirectories.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    lessonActivityController.onSelectNoDirectories();
                }
            });
        }

        Button buttonFilter = (Button) findViewById(R.id.lessons_buttonFilter);
        if (buttonFilter != null) {
            buttonFilter.setText(i18n.getString("nui.lesson.tree.filter"));
            buttonFilter.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    MessageDialogs.showInputTextDialog(
                            LessonActivity.this,
                            i18n.getString(
                                    "nui.lesson.filter.input",
                                    Collections.singletonList(
                                            lessonController.getLessonModelBehaviour().countAvailableLessons()
                                    )
                            ),
                            new MessageDialogs.InputDialogListener() {
                                @Override
                                public void onInputOk(String input) {
                                    try {
                                        int maxVal = Integer.parseInt(input);
                                        lessonFilter = new LessonFilter();
                                        lessonFilter.setNumberOfRecentFiles(maxVal);
                                        lessonController.onFilter();
                                    } catch (NumberFormatException e) {
                                        e.printStackTrace();
                                        lessonFilter = null;
                                    }
                                }

                                @Override
                                public void onInputCanceled() {

                                }
                            }
                    );
                }
            });
        }
    }

    private void createLessonSelection() {
        lessonSelection = (ListView) findViewById(R.id.lessons_lessonSelection);
        lessonSelectionAdapter = new ArrayAdapter<LessonReferenceHolder>(
                this, R.layout.lesson_item
        );
        lessonSelection.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        lessonSelection.setAdapter(lessonSelectionAdapter);
        lessonSelection.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                lessonActivityController.onLessonSelectionChanged(
                        (LessonReferenceHolder) adapterView.getItemAtPosition(position)
                );
            }
        });

        TextView lbSelectFiles = (TextView) findViewById(R.id.lessons_lbSelectFiles);
        if (lbSelectFiles != null)
            lbSelectFiles.setText(i18n.getString("nui.lesson.select.files"));

        buttonSelectAllFiles = (Button) findViewById(R.id.lessons_buttonSelectAllFiles);
        buttonSelectAllFiles.setText(i18n.getString("nui.lesson.select.all.files"));
        buttonSelectAllFiles.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                lessonActivityController.onSelectAllFiles();
            }
        });

        buttonSelectNoFiles = (Button) findViewById(R.id.lessons_buttonSelectNoFiles);
        buttonSelectNoFiles.setText(i18n.getString("nui.lesson.select.no.files"));
        buttonSelectNoFiles.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                lessonActivityController.onSelectNoFiles();
            }
        });
    }

    private void createHeaderRow() {
        TextView lbHeaderDirectories = (TextView) findViewById(R.id.lessons_lbHeaderDirectories);
        if (lbHeaderDirectories != null)
            lbHeaderDirectories.setText(i18n.getString("nui.lessons.header.directories"));

        lbHeaderFiles = (TextView) findViewById(R.id.lessons_lbHeaderFiles);
        if (lbHeaderFiles != null)
            lbHeaderFiles.setText(i18n.getString("nui.lessons.header.files"));
    }

    private interface LessonSortable {
        String sortableItem();
    }

    class LessonActivityController {

        void intializeDisplay() {
            LessonReference rootReference = lessonController.getLessonModelBehaviour().getRootReference();
            populateDirectorySelection(rootReference.getLesson().asContainer());
            if (directorySelectionAdapter.getCount() > 0)
                onDirectorySelectionChanged(directorySelectionAdapter.getItem(0));
            refreshLessonSelection();
        }

        private void populateDirectorySelection(LessonContainer parentContainer) {
            int numberOfDirectChildren = 0;

            List<LessonSortable> containers = new ArrayList<LessonSortable>();
            for (Lesson container : parentContainer.lessons()) {
                if (container.isContainer()) {
                    containers.add(new LessonContainerHolder(container.asContainer()));
                } else
                    numberOfDirectChildren++;
            }

            if (numberOfDirectChildren > 0)
                directorySelectionAdapter.add(new LessonContainerHolder(parentContainer));

            for (LessonSortable containerHolder : sortByName(containers)) {
                LessonContainer container = ((LessonContainerHolder) containerHolder).lessonContainer;
                populateDirectorySelection(container);
            }


        }

        void onDirectorySelectionChanged(LessonContainerHolder selectedContainerHolder) {
            updateDirectorySelection(selectedContainerHolder);
            updateLessonSelection(selectedContainerHolder);
        }

        private void updateLessonSelection(LessonContainerHolder selectedContainerHolder) {
            lessonSelectionAdapter.clear();
            int lessonCount = 0;

            List<LessonSortable> lessonList = new ArrayList<LessonSortable>();
            for (Lesson lesson : selectedContainerHolder.lessonContainer.lessons()) {
                if (lesson.isContainer())
                    continue;

                lessonList.add(new LessonReferenceHolder(new LessonReference(lesson)));
                lessonCount++;
            }
            for (LessonSortable lessonSortable : sortByName(lessonList))
                lessonSelectionAdapter.add((LessonReferenceHolder) lessonSortable);

            refreshLessonSelection();

            buttonSelectAllFiles.setEnabled(lessonCount > 0);
            buttonSelectNoFiles.setEnabled(lessonCount > 0);
        }

        private void updateDirectorySelection(LessonContainerHolder selectedContainerHolder) {
            int directoryCount = directorySelectionAdapter.getCount();

            int selectedPos = -1;
            for (int i = 0; i < directoryCount; i++) {
                LessonContainerHolder containerHolder = directorySelectionAdapter.getItem(i);
                if (containerHolder.lessonContainer.equals(selectedContainerHolder.lessonContainer)) {
                    directorySelection.setItemChecked(i, true);
                    selectedPos = i;
                } else
                    directorySelection.setItemChecked(i, false);
            }
            if (selectedPos >= 0)
                directorySelection.smoothScrollToPosition(selectedPos);
        }

        private void refreshLessonSelection() {
            final int maxPos = lessonSelectionAdapter.getCount();
            for (int pos = 0; pos < maxPos; pos++) {
                LessonReferenceHolder lessonReferenceHolder = lessonSelectionAdapter.getItem(pos);
                lessonSelection.setItemChecked(
                        pos,
                        lessonReferenceHolder.lessonReference.getLessonState() == LessonState.SELECTED
                );
            }
        }

        private Collection<LessonSortable> sortByName(Iterable<LessonSortable> lessonSortables) {
            TreeSet<LessonSortable> sortedLessons = new TreeSet<LessonSortable>(
                    new Comparator<LessonSortable>() {
                        public int compare(LessonSortable lhs, LessonSortable rhs) {
                            return lhs.sortableItem().compareTo(
                                    rhs.sortableItem()
                            );
                        }
                    }
            );

            for (LessonSortable lessonSortable : lessonSortables) {
                sortedLessons.add(lessonSortable);
            }

            return sortedLessons;
        }

        public void onSelectAllFiles() {
            onSelectFiles(true);
        }

        public void onSelectNoFiles() {
            onSelectFiles(false);
        }

        private void onSelectFiles(boolean selected) {
            final int maxPos = lessonSelectionAdapter.getCount();
            for (int pos = 0; pos < maxPos; pos++) {
                LessonReference child = ((LessonReferenceHolder) lessonSelection.getItemAtPosition(pos)).lessonReference;
                if (
                        (child.getLessonState() == LessonState.SELECTED && !selected) ||
                                (child.getLessonState() == LessonState.UN_SELECTED && selected)
                        ) {
                    lessonSelection.setItemChecked(pos, selected);
                    onLessonSelectionChanged(child);
                }
            }
        }

        public void onLessonSelectionChanged(LessonReferenceHolder selectedReferenceHolder) {
            onLessonSelectionChanged(selectedReferenceHolder.lessonReference);
        }

        private void onLessonSelectionChanged(LessonReference selectedReference) {
            if (selectedReference.getLessonState() == LessonState.UN_SELECTED)
                selectedReference.setLessonState(LessonState.SELECTED);
            else
                selectedReference.setLessonState(LessonState.UN_SELECTED);

            lessonController.onLessonSelectionChanged(selectedReference);
        }

        public void onSelectAllDirectories() {
            onSelectDirectories(true);
        }

        public void onSelectNoDirectories() {
            onSelectDirectories(false);
        }

        private void onSelectDirectories(boolean all) {
            onSelectFiles(all);

            LessonReference rootReference = lessonController.getLessonModelBehaviour().getRootReference();
            if (all)
                rootReference.setLessonState(LessonState.SELECTED);
            else
                rootReference.setLessonState(LessonState.UN_SELECTED);

            lessonController.onLessonSelectionChanged(rootReference);
        }

        public void onOk() {
            LessonController.CheckResult checkResult = lessonController.onWindowClosing(NuiCloseEvent.CloseType.OK);
            if (checkResult == LessonController.CheckResult.CANCEL)
                return;
            lessonController.onWindowClosed(NuiCloseEvent.CloseType.OK);
            doClose();
        }

        public void onCancel() {
            LessonController.CheckResult checkResult = lessonController.onWindowClosing(NuiCloseEvent.CloseType.CANCEL);
            if (checkResult == LessonController.CheckResult.CANCEL)
                return;
            lessonController.onWindowClosed(NuiCloseEvent.CloseType.CANCEL);
            doClose();
        }

        private void doClose() {
            UIPlatformFactory.getUIPlatform().getNuiDirector().showWindow(WindowType.PRACTICE_WINDOW);
        }

    }

    class MyBehaviour implements LessonWindowBehaviour {

        public void displayRoot(File rootDirectory) {
            lessonActivityController.intializeDisplay();
        }

        public void expandNonEmptyTreeRows() {
        }

        public void scrollToFirstSelectedLesson() {
            int directoryCount = directorySelectionAdapter.getCount();

            LessonContainerHolder holderWithSelection = null;
            directoryLoop:
            for (int i = 0; i < directoryCount; i++) {
                LessonContainerHolder containerHolder = directorySelectionAdapter.getItem(i);
                for (Lesson lesson : containerHolder.lessonContainer.lessons()) {
                    if (lesson.isContainer())
                        continue;
                    if (lesson.getState() == LessonState.SELECTED) {
                        holderWithSelection = containerHolder;
                        break directoryLoop;
                    }
                }
            }

            if (holderWithSelection != null)
                lessonActivityController.onDirectorySelectionChanged(holderWithSelection);
        }

        public void displaySelectedLessons(int availableLessons, int selectedLessons) {
            if (selectedLessons == 0)
                lbHeaderFiles.setText(
                        i18n.getString(
                                "nui.lesson.selected.info.0",
                                Collections.singletonList(selectedLessons)
                        )
                );
            else
                lbHeaderFiles.setText(
                        i18n.getString(
                                "nui.lesson.selected.info.N",
                                Collections.singletonList(selectedLessons)
                        )
                );
        }

        public void sendMessageSelectionEmpty() {
            MessageDialogs.showMessageBox(
                    LessonActivity.this,
                    i18n.getString("nui.lesson.empty.selection")
            );
        }

        public boolean confirmCancel() {
            return true; // there is no way to block the thread and wait for input
        }

        @Override
        public LessonFilter filterPrompt(int availableLessons) {
            return lessonFilter;
        }

        @Override
        public void collapseAll() {
            // not relevant
        }
    }

    private class LessonContainerHolder implements LessonSortable {
        private final LessonContainer lessonContainer;

        private LessonContainerHolder(LessonContainer lessonContainer) {
            this.lessonContainer = lessonContainer;
        }

        @Override
        public String toString() {
            List<String> pathElements = new ArrayList<String>();
            LessonContainer x = lessonContainer;
            do {
                if (!x.isRoot()) {
                    pathElements.add(x.getName());
                }
                x = x.getParent();
            } while (x != null);

            StringBuilder pathName = new StringBuilder();
            for (int i = pathElements.size() - 1; i >= 0; i--)
                pathName.append(FILE_SEPARATOR).append(pathElements.get(i));
            if (pathElements.isEmpty())
                pathName.append(FILE_SEPARATOR);

            return pathName.toString();
        }

        public String sortableItem() {
            return lessonContainer.getName();
        }
    }

    private class LessonReferenceHolder implements LessonSortable {
        private final LessonReference lessonReference;

        private LessonReferenceHolder(LessonReference lessonReference) {
            this.lessonReference = lessonReference;
        }

        @Override
        public String toString() {
            return lessonReference.getLesson().getName();
        }

        public String sortableItem() {
            return lessonReference.getLesson().getName();
        }
    }
}
