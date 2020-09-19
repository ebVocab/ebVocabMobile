package de.ebuchner.vocab.mobile.common;

import android.widget.TextView;
import de.ebuchner.vocab.model.practice.FieldRendererContext;

import java.io.File;

public interface FieldRenderer {

    void renderLabel(FieldRendererContext context, TextView label);

    void renderTextComponent(FieldRendererContext context, TextView textComponent);

    void renderLessonComponent(File fileRef, TextView textComponent);

}