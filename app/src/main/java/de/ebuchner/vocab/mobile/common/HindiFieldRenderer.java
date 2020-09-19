package de.ebuchner.vocab.mobile.common;

import android.graphics.Color;
import android.widget.TextView;
import de.ebuchner.toolbox.i18n.I18NContext;
import de.ebuchner.vocab.config.fields.FieldFactory;
import de.ebuchner.vocab.config.templates.hi.MasculineFemininePlural;
import de.ebuchner.vocab.model.practice.FieldRendererContext;
import de.ebuchner.vocab.nui.common.I18NLocator;

import java.util.Collections;

public class HindiFieldRenderer extends DefaultFieldRenderer {
    private static final int LIGHT_BLUE = Color.parseColor("#ADD8E6");
    private static final int LIGHT_RED = Color.parseColor("#FFC0CB");
    private static final int WHITE = Color.WHITE;
    I18NContext i18n = I18NLocator.locate();
    MasculineFemininePlural plural = new MasculineFemininePlural();

    @Override
    public void renderTextComponent(FieldRendererContext rendererContext, TextView textComponent) {
        textComponent.setBackgroundColor(WHITE);

        if (rendererContext.isFieldHidden()) {
            super.renderTextComponent(rendererContext, textComponent);
            return;
        }

        if (FieldFactory.TYPE.equals(rendererContext.getField().name())) {

            String foreignValue = rendererContext.getCurrentEntry().getFieldValue(FieldFactory.FOREIGN);

            String value = rendererContext.getValue();
            if (plural.isMasculine(value)) {
                textComponent.setText(i18n.getString(
                        "nui.practice.hindi.gender.m",
                        Collections.singletonList(plural.doMasculine(foreignValue))));
                textComponent.setBackgroundColor(LIGHT_BLUE);
                return;
            } else if (plural.isFeminine(value)) {
                textComponent.setText(i18n.getString(
                        "nui.practice.hindi.gender.f",
                        Collections.singletonList(plural.doFeminine(foreignValue))));
                textComponent.setBackgroundColor(LIGHT_RED);
                return;
            }
        }

        super.renderTextComponent(rendererContext, textComponent);
    }
}
