package de.ebuchner.vocab.mobile.search;

import android.app.Activity;
import android.util.SparseBooleanArray;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import de.ebuchner.toolbox.i18n.I18NContext;
import de.ebuchner.vocab.config.fields.FieldFactory;
import de.ebuchner.vocab.mobile.R;
import de.ebuchner.vocab.mobile.platform.MobileUIPlatform;
import de.ebuchner.vocab.model.lessons.entry.VocabEntry;
import de.ebuchner.vocab.model.lessons.entry.VocabEntryRef;
import de.ebuchner.vocab.model.search.*;
import de.ebuchner.vocab.nui.common.I18NLocator;

import java.util.Collections;

public class SearchPopup {
    private Activity parent;
    private I18NContext i18n = I18NLocator.locate();
    private EditText etSearch;

    private SearchController controller = new SearchController(new MyBehaviour());
    private CheckBox cbCaseSensitive;
    private CheckBox cbWholeWords;
    private CheckBox cbRegEx;
    private CheckBox cbComments;
    private Button btnEditEntry;
    private ListView lvResult;
    private ArrayAdapter<ResultItem> lvResultAdapter;

    public void showPopup(Activity parent, String initialText) {
        this.parent = parent;

        View searchView = parent.getLayoutInflater().inflate(
                R.layout.search,
                null
        );

        PopupWindow searchPopup = new PopupWindow(searchView,
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT,
                true);

        TextView tvSearch = (TextView) searchView.findViewById(R.id.search_lbSearch);
        tvSearch.setText(i18n.getString("nui.search.text.to.find"));

        etSearch = (EditText) searchView.findViewById(R.id.search_etSearch);
        if (initialText != null)
            etSearch.setText(initialText);

        cbCaseSensitive = (CheckBox) searchView.findViewById(R.id.search_cbCaseSensitive);
        cbCaseSensitive.setText(i18n.getString("nui.search.option.case.sensitive"));

        cbWholeWords = (CheckBox) searchView.findViewById(R.id.search_cbWholeWords);
        cbWholeWords.setText(i18n.getString("nui.search.option.whole.words"));

        cbRegEx = (CheckBox) searchView.findViewById(R.id.search_cbRegEx);
        cbRegEx.setText(i18n.getString("nui.search.option.regular.expressions"));

        cbComments = (CheckBox) searchView.findViewById(R.id.search_cbComments);
        cbComments.setText(i18n.getString("nui.search.option.comments"));

        Button btnSearch = (Button) searchView.findViewById(R.id.search_btnSearch);
        btnSearch.setText(i18n.getString("nui.search.button"));

        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                controller.onSearchResult(
                        controller.onSearchStart(
                                etSearch.getText().toString(),
                                cbCaseSensitive.isChecked(),
                                cbWholeWords.isChecked(),
                                cbRegEx.isChecked(),
                                cbComments.isChecked()
                        )
                );
            }
        });

        btnEditEntry = (Button) searchView.findViewById(R.id.search_btnEditEntry);
        if (btnEditEntry != null) {
            btnEditEntry.setText(i18n.getString("nui.search.edit.result"));
            btnEditEntry.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    VocabEntryRef selectedRef = null;
                    SparseBooleanArray selection = lvResult.getCheckedItemPositions();
                    for (int i = 0; i < selection.size(); i++) {
                        selectedRef = lvResultAdapter.getItem(selection.keyAt(i)).getVocabRef();
                    }

                    if (selectedRef != null)
                        controller.onResultEdit(selectedRef);
                }
            });
        }

        Button btnClose = (Button) searchView.findViewById(R.id.search_btnClose);
        btnClose.setText(i18n.getString("nui.close"));
        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchPopup.dismiss();
            }
        });

        lvResult = (ListView) searchView.findViewById(R.id.search_lvResult);
        lvResultAdapter = new ArrayAdapter<ResultItem>(
                parent, R.layout.search_item
        );
        lvResult.setAdapter(lvResultAdapter);
        lvResult.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                controller.onTableSelectionChanged(
                        Collections.singletonList(
                                ((ResultItem) adapterView.getItemAtPosition(position)).getVocabEntry()
                        )
                );
            }
        });
        lvResult.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE);


        controller.onWindowWasCreated();

        searchPopup.showAtLocation(searchView, Gravity.CENTER, 0, 0);
    }

    private class MyBehaviour implements SearchWindowBehaviour {
        @Override
        public SearchResultModelBehaviour decorateModel(SearchResultModelBehaviour searchResultModel) {
            return new MyResultModel(searchResultModel);
        }

        @Override
        public void sendMessageNoSearchString() {
            MobileUIPlatform.instance().getMobileNuiDirector().showToast(
                    i18n.getString("nui.search.message.no.search.string")
            );
        }

        @Override
        public void setResultElementsSelectedCount(int selectedCount) {
            if (btnEditEntry != null)
                btnEditEntry.setEnabled(selectedCount == 1);
        }

        @Override
        public void sendMessageResultCount(int resultCount) {
            if (resultCount == 0)
                MobileUIPlatform.instance().getMobileNuiDirector().showToast(
                        i18n.getString("nui.search.message.no.result")
                );
            else
                MobileUIPlatform.instance().getMobileNuiDirector().showToast(
                        i18n.getString(
                                "nui.search.message.result.size",
                                Collections.singletonList(resultCount)
                        )
                );
        }

        @Override
        public void setSearchOptions(SearchOptions searchOptions, boolean searchInComments) {
            cbCaseSensitive.setChecked(searchOptions.isCaseSensitive());
            cbWholeWords.setChecked(searchOptions.isWholeWords());
            cbRegEx.setChecked(searchOptions.isRegularExpression());
            cbComments.setChecked(searchInComments);
        }
    }

    private class ResultItem {
        private final VocabEntryRef vocabRef;
        private final VocabEntry vocabEntry;

        private ResultItem(VocabEntry vocabEntry, VocabEntryRef vocabRef) {
            this.vocabRef = vocabRef;
            this.vocabEntry = vocabEntry;
        }

        public VocabEntryRef getVocabRef() {
            return vocabRef;
        }

        @Override
        public String toString() {
            return String.format(
                    "%s - %s - [%s]",
                    vocabEntry.getFieldValue(FieldFactory.FOREIGN),
                    vocabEntry.getFieldValue(FieldFactory.USER),
                    vocabRef.getFileRef().getName()
            );
        }

        public VocabEntry getVocabEntry() {
            return vocabEntry;
        }
    }

    private class MyResultModel implements SearchResultModelBehaviour {
        private final SearchResultModelBehaviour decoree;

        public MyResultModel(SearchResultModelBehaviour decoree) {
            this.decoree = decoree;
        }

        @Override
        public int getEntryCount() {
            return decoree.getEntryCount();
        }

        @Override
        public VocabEntryRef getEntryRef(int row) {
            return decoree.getEntryRef(row);
        }

        @Override
        public VocabEntry getEntry(int row) {
            return decoree.getEntry(row);
        }

        @Override
        public void setResult(VocabSearcher.Result result) {
            decoree.setResult(result);
            lvResultAdapter.clear();

            for (VocabEntryRef entryRef : result.getEntryRefs()) {
                VocabEntry entry = result.getEntry(entryRef);
                if (entry == null)
                    continue;
                lvResultAdapter.add(
                        new ResultItem(
                                entry,
                                entryRef
                        )
                );
            }
        }

        @Override
        public VocabSearchOptions getDefaultSearchOptions() {
            return decoree.getDefaultSearchOptions();
        }

        @Override
        public void setDefaultSearchOptions(VocabSearchOptions vocabSearchOptions) {
            decoree.setDefaultSearchOptions(vocabSearchOptions);
        }
    }
}
