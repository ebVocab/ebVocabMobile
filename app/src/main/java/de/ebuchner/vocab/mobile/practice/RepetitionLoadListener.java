package de.ebuchner.vocab.mobile.practice;

import java.io.File;

public interface RepetitionLoadListener {

    void onRepetitionFileSelected(File repetitionFile);

    void onRepetitionFileSelectionCanceled();

}
