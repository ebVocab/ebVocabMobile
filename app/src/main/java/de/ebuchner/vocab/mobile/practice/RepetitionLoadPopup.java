package de.ebuchner.vocab.mobile.practice;

import android.app.Activity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import de.ebuchner.toolbox.i18n.I18NContext;
import de.ebuchner.vocab.config.Config;
import de.ebuchner.vocab.config.ConfigConstants;
import de.ebuchner.vocab.mobile.R;
import de.ebuchner.vocab.model.practice.RepetitionLoadActivityBehaviour;
import de.ebuchner.vocab.model.practice.RepetitionLoadActivityController;
import de.ebuchner.vocab.model.practice.RepetitionLoadModel;
import de.ebuchner.vocab.nui.common.I18NLocator;
import de.ebuchner.vocab.nui.common.RepetitionItem;

import java.io.File;

public class RepetitionLoadPopup {

    private static final String TAG = RepetitionLoadPopup.class.getName();
    private final RepetitionLoadActivityBehaviour myBehaviour = new MyBehaviour();
    private final RepetitionLoadModel model = new RepetitionLoadModel();
    private final RepetitionLoadActivityController controller =
            new RepetitionLoadActivityController(model, myBehaviour);
    private I18NContext i18n = I18NLocator.locate();
    private Button btnOk;
    private ArrayAdapter<RepetitionItem> repetitionListAdapter;
    private Button btnDeleteSelected;
    private ListView lvRepetitionList;

    private PopupWindow popupWindow;
    private RepetitionLoadListener repetitionLoadListener;

    public void showPopup(Activity parent, RepetitionLoadListener repetitionLoadListener) {
        this.repetitionLoadListener = repetitionLoadListener;
        View popupView = parent.getLayoutInflater().inflate(
                R.layout.repetition_load,
                null
        );
        popupWindow = new PopupWindow(popupView,
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT,
                true);

        popupWindow.setTouchable(true);
        popupWindow.setFocusable(true);

        btnOk = (Button) popupView.findViewById(R.id.repetition_load_ok);
        btnOk.setText(i18n.getString("nui.ok"));
        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                controller.onOk();
            }
        });

        btnDeleteSelected = (Button) popupView.findViewById(R.id.repetition_load_delete_selected);
        btnDeleteSelected.setText(i18n.getString("nui.repetition.load.delete.selected"));
        btnDeleteSelected.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                controller.onDeleteSelected();
            }
        });

        Button btnCancel = (Button) popupView.findViewById(R.id.repetition_load_cancel);
        btnCancel.setText(i18n.getString("nui.cancel"));
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                controller.onCancel();
            }
        });

        TextView tvRepetitionListLabel = (TextView) popupView.findViewById(R.id.repetitionLoad_list_label);
        tvRepetitionListLabel.setText(i18n.getString("nui.repetition.load.list.label"));

        lvRepetitionList = (ListView) popupView.findViewById(R.id.repetition_load_list);
        repetitionListAdapter = new ArrayAdapter<>(
                popupView.getContext(), R.layout.repetition_load_item
        );
        lvRepetitionList.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        for (File repetitionFile :
                Config.instance().listAutoSavedFiles(
                        ConfigConstants.REPETITION_FILES_SUFFIX,
                        ConfigConstants.FILE_REF_EXTENSION
                ))
            try {
                repetitionListAdapter.add(new RepetitionItem(repetitionFile));
            } catch (Exception e) {
                boolean deleted = repetitionFile.delete();
                Log.i(TAG, String.format("Deleted invalid auto saved file %s: %s",
                        repetitionFile.getName(),
                        deleted ? "ok" : "failed"
                ));
            }
        lvRepetitionList.setAdapter(repetitionListAdapter);
        lvRepetitionList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                if (position == ListView.INVALID_POSITION)
                    controller.onRepetitionSelectionCleared();
                else
                    controller.onRepetitionSelected(
                            repetitionListAdapter.getItem(position).getRepetitionFile()
                    );
            }
        });
        lvRepetitionList.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);

        myBehaviour.onModelChanged(model);

        popupWindow.showAtLocation(popupView, Gravity.CENTER, 0, 0);
    }

    private class MyBehaviour implements RepetitionLoadActivityBehaviour {
        @Override
        public void sendRepetitionToCallerAndExit(File repetitionFile) {
            repetitionLoadListener.onRepetitionFileSelected(repetitionFile);
            popupWindow.dismiss();
        }

        @Override
        public void sendCancelToCallerAndExit() {
            repetitionLoadListener.onRepetitionFileSelectionCanceled();
            popupWindow.dismiss();
        }

        @Override
        public void onModelChanged(RepetitionLoadModel model) {
            btnOk.setEnabled(model.hasRepetitionFile());
            btnDeleteSelected.setEnabled(model.hasRepetitionFile());
        }

        @Override
        public void selectedRepetitionDeleted() {
            int itemPosition = lvRepetitionList.getCheckedItemPosition();
            if (itemPosition == ListView.INVALID_POSITION)
                return;
            repetitionListAdapter.remove(repetitionListAdapter.getItem(itemPosition));
        }
    }
}
