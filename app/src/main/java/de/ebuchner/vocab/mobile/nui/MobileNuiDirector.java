package de.ebuchner.vocab.mobile.nui;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Build;
import android.widget.Toast;
import de.ebuchner.vocab.mobile.common.VocabBaseActivity;
import de.ebuchner.vocab.mobile.common.VocabBaseMobileWindow;
import de.ebuchner.vocab.model.lessons.entry.VocabEntry;
import de.ebuchner.vocab.model.nui.NuiWindow;
import de.ebuchner.vocab.model.nui.NuiWindowParameter;
import de.ebuchner.vocab.model.nui.WindowType;
import de.ebuchner.vocab.model.nui.platform.UIPlatformFactory;
import de.ebuchner.vocab.nui.NuiDirector;

import java.util.List;

public class MobileNuiDirector extends NuiDirector {

    private static final String TAG = MobileNuiDirector.class.getName();
    private VocabBaseActivity currentActivity;

    @Override
    protected NuiWindow createWindow(WindowType windowType, NuiWindowParameter windowParameter) {
        if (currentActivity == null)
            throw new IllegalStateException("No context found");

        String windowClassName = UIPlatformFactory.getUIPlatform().windowClassName(windowType.getWindowClassName());
        try {
            VocabBaseMobileWindow window = (VocabBaseMobileWindow) Class.forName(windowClassName).newInstance();
            //currentActivity.finish();
            window.mobileWindowCreate(currentActivity);
            return window;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    protected void exitSystemDependant() {
        if (currentActivity != null)
            currentActivity.finish();
    }

    @Override
    public boolean closeAll() {
        // not supported / not necessary on this platform
        return true;
    }

    @Override
    public boolean entriesToClipboard(List<VocabEntry> entriesToCopy) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<VocabEntry> entriesFromClipboard() {
        throw new UnsupportedOperationException();
    }

    public VocabBaseActivity getCurrentActivity() {
        if (currentActivity == null)
            throw new IllegalStateException("No current activity available");
        return currentActivity;
    }

    public void setCurrentActivity(VocabBaseActivity currentActivity) {
        this.currentActivity = currentActivity;
    }

    public ClipboardManager getClipboard() {
        if (currentActivity == null)
            return null;
        return (ClipboardManager) currentActivity.getSystemService(Context.CLIPBOARD_SERVICE);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void copyToClipboard(String text) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB)
            return;

        getClipboard().setPrimaryClip(ClipData.newPlainText(null, text));
    }

    public void showToast(String text) {
        if (currentActivity == null)
            return;

        Toast toast = Toast.makeText(currentActivity, text, Toast.LENGTH_SHORT);
        toast.show();
    }

    public String textFromClipboard() {
        if (currentActivity == null)
            return "";

        android.content.ClipboardManager clipboard = (android.content.ClipboardManager)
                currentActivity.getSystemService(Activity.CLIPBOARD_SERVICE);
        if (!clipboard.hasPrimaryClip())
            return null;

        CharSequence content = clipboard.getPrimaryClip().toString();

        return content != null ? content.toString() : null;
    }
}
