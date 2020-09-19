package de.ebuchner.vocab.mobile.common;

import android.content.Context;
import android.widget.TextView;
import de.ebuchner.vocab.mobile.R;
import de.ebuchner.vocab.mobile.platform.MobileUIPlatform;
import de.ebuchner.vocab.model.practice.FieldRendererContext;

import java.io.File;

public class DefaultFieldRenderer implements FieldRenderer {

    public void renderLabel(FieldRendererContext rendererContext, TextView label) {
        Context context = MobileUIPlatform.instance().getMobileNuiDirector().getCurrentActivity();
        if (context == null)
            return;

        if (rendererContext.isFieldHidden() && rendererContext.isValueNotEmpty())
            label.setTextColor(context.getResources().getColor(R.color.indiaGreen));
        else
            label.setTextColor(context.getResources().getColor(R.color.indiaBlue));
    }

    public void renderTextComponent(FieldRendererContext rendererContext, TextView textComponent) {
        textComponent.setText(rendererContext.getValue());
    }

    public void renderLessonComponent(File fileRef, TextView textComponent) {
        if (fileRef == null) {
            textComponent.setText("");
            return;
        }

        String name = fileRef.getName();
        int pos = name.lastIndexOf('.');
        if (pos > 0)
            name = name.substring(0, pos);

        textComponent.setText(name);
    }

}

