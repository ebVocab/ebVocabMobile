package de.ebuchner.vocab.model.practice;

import android.content.Intent;
import de.ebuchner.vocab.config.fields.FieldFactory;
import de.ebuchner.vocab.mobile.analysis.AnalysisPopup;
import de.ebuchner.vocab.mobile.common.VocabBaseActivity;
import de.ebuchner.vocab.mobile.nui.MobileNuiDirector;
import de.ebuchner.vocab.mobile.platform.MobileUIPlatform;
import de.ebuchner.vocab.mobile.search.SearchPopup;
import de.ebuchner.vocab.model.lessons.entry.VocabEntry;
import de.ebuchner.vocab.nui.common.I18NLocator;

import java.io.File;

public class MobilePracticeController extends PracticeController {
    public MobilePracticeController(PracticeWindowBehaviour practiceWindow) {
        super(practiceWindow);
    }

    private VocabEntry currentEntry() {
        if (!practiceModel.hasCurrentRef())
            return null;

        SelectedStrategy selectedStrategy = practiceModel.getSelectedStrategy();
        AbstractPracticeStrategy strategy = AbstractPracticeStrategy.getOrCreateStrategyModel(selectedStrategy);
        return strategy.currentEntry();
    }

    public void onCopyText(String field) {
        VocabEntry entry = currentEntry();
        if (entry == null)
            return;

        String text = entry.getFieldValue(field);
        if (text == null || text.trim().length() == 0)
            return;

        MobileNuiDirector nuiDirector = MobileUIPlatform.instance().getMobileNuiDirector();
        nuiDirector.copyToClipboard(text);
        nuiDirector.showToast(I18NLocator.locate().getString("nui.clipboard.copied"));
    }

    public void onRepetitionLoad(VocabBaseActivity currentActivity, Class<? extends VocabBaseActivity> targetActivity) {
        Intent intent = new Intent(currentActivity, targetActivity);
        currentActivity.startActivityForResult(intent, 0);
    }

    public void onChangeRepetition(File repetitionFile) {
        super.onChangeRepetitionImpl(repetitionFile);
    }

    public void onAnalysis() {
        new AnalysisPopup().showPopup(
                MobileUIPlatform.instance().getMobileNuiDirector().getCurrentActivity(),
                currentForeignValue()
        );
    }

    private String currentForeignValue() {
        String initialText = null;
        VocabEntry entry = currentEntry();
        if (entry != null)
            initialText = entry.getFieldValue(FieldFactory.FOREIGN);
        return initialText;
    }

    public void onSearch() {
        new SearchPopup().showPopup(
                MobileUIPlatform.instance().getMobileNuiDirector().getCurrentActivity(),
                currentForeignValue()
        );
    }
}
