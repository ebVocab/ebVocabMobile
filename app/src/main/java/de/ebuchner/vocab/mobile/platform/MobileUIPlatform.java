package de.ebuchner.vocab.mobile.platform;

import android.os.Build;
import de.ebuchner.vocab.mobile.model.project.MobileProjectBean;
import de.ebuchner.vocab.mobile.nui.MobileNuiDirector;
import de.ebuchner.vocab.model.io.ImageEncoderBehaviour;
import de.ebuchner.vocab.model.nui.platform.UIPlatform;
import de.ebuchner.vocab.model.nui.platform.UIPlatformFactory;
import de.ebuchner.vocab.model.nui.platform.UIPlatformType;
import de.ebuchner.vocab.model.project.ProjectBean;
import de.ebuchner.vocab.nui.NuiDirector;

import java.text.MessageFormat;

public class MobileUIPlatform implements UIPlatform {
    private MobileNuiDirector nuiDirector = new MobileNuiDirector();

    public static MobileUIPlatform instance() {
        return (MobileUIPlatform) UIPlatformFactory.getUIPlatform();
    }

    public UIPlatformType getType() {
        return UIPlatformType.MOBILE;
    }

    public void initializeUISystem() {
    }

    public ImageEncoderBehaviour newImageEncoder() {
        throw new UnsupportedOperationException();
    }

    public String windowClassName(String windowID) {
        return MessageFormat.format(windowID, "mobile");
    }

    public String uiRuntimeName() {
        return String.format("%s - Android %s (%d)", Build.PRODUCT, Build.VERSION.RELEASE, Build.VERSION.SDK_INT);
    }

    public NuiDirector getNuiDirector() {
        return nuiDirector;
    }

    public ProjectBean getProjectBean() {
        // don't cache bean. Cloud beans may change underlying files
        MobileProjectBean projectBean = new MobileProjectBean();
        projectBean.loadProjectBean();
        return projectBean;
    }

    public MobileNuiDirector getMobileNuiDirector() {
        return nuiDirector;
    }
}
