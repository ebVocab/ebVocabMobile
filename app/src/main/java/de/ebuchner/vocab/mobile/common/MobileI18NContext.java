package de.ebuchner.vocab.mobile.common;

import de.ebuchner.toolbox.i18n.I18NContext;
import de.ebuchner.vocab.nui.common.I18NLocator;

import java.util.List;

public class MobileI18NContext {

    private I18NContext context = I18NLocator.locate();

    private String toMobile(String value) {
        return value.replaceAll("_", "");
    }

    public String getString(String name) {
        String value = context.getString(name);
        return toMobile(value);
    }

    public String getString(String name, List args) {
        String value = context.getString(name, args);
        return toMobile(value);
    }
}
