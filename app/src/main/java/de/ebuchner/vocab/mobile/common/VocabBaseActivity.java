package de.ebuchner.vocab.mobile.common;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;

import de.ebuchner.vocab.config.Config;
import de.ebuchner.vocab.config.VocabEnvironment;
import de.ebuchner.vocab.mobile.platform.MobileUIPlatform;
import de.ebuchner.vocab.model.VocabModel;
import de.ebuchner.vocab.model.nui.WindowType;
import de.ebuchner.vocab.model.nui.platform.UIPlatformFactory;

public abstract class VocabBaseActivity extends AppCompatActivity {

    private static final String TAG = VocabBaseActivity.class.getName();
    private static final int PERMISSION_REQUEST_CODE = 42;
    protected MobileI18NContext i18n = new MobileI18NContext();
    private Typeface defaultTypeface;

    private Bundle lastSavedInstanceState;
    private boolean isWaitingForPermission = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        MobileUIPlatform.instance().getMobileNuiDirector().setCurrentActivity(this);

        super.onCreate(savedInstanceState);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            lastSavedInstanceState = savedInstanceState;
            isWaitingForPermission = true;
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            }, PERMISSION_REQUEST_CODE);
        } else initActivity(savedInstanceState);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_CODE && ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
            initActivity(lastSavedInstanceState);
        else {
            showToast("Permissions not granted!");
            finish();
        }
    }

    private void initActivity(Bundle savedInstanceState) {
        if (requiresConfig() && !Config.projectInitialized()) {
            redirectToMainActivity();
            return;
        }

        setTitle(generateActivityTitle());

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        onCreateImpl(savedInstanceState);
        onResumeImpl();
    }

    protected boolean requiresConfig() {
        return false;
    }

    private void redirectToMainActivity() {
        UIPlatformFactory.getUIPlatform().getNuiDirector().showWindow(WindowType.PROJECT_WINDOW);
    }

    protected String generateActivityTitle() {
        return VocabEnvironment.APP_TITLE_SHORT + " - " + i18n.getString(titleResourceKey());
    }

    protected String generateActivityTitleExtra(String extra) {
        return VocabEnvironment.APP_TITLE_SHORT + " - " + i18n.getString(titleResourceKey()) + " - " + extra;
    }

    protected abstract String titleResourceKey();

    // should be overridden in child classes
    protected abstract void onCreateImpl(Bundle savedInstanceState);

    @Override
    protected final void onResume() {
        if (isWaitingForPermission) {
            super.onResume();
            return;
        }

        MobileUIPlatform.instance().getMobileNuiDirector().setCurrentActivity(this);

        super.onResume();

        if (requiresConfig() && !Config.projectInitialized()) {
            redirectToMainActivity();
            return;
        }

        onResumeImpl();
    }

    // may be overridden if resume event is relevant to child classes
    protected void onResumeImpl() {

    }

    @Override
    protected void onPause() {
        super.onPause();
        boolean success = true;
        if (Config.projectInitialized()) {
            try {
                VocabModel.getInstance().savePreferences();
                Config.instance().saveState();
            } catch (Exception e) {
                Log.e(TAG, "error saving app state", e);
                success = false;
            }
        }
        Log.i(TAG, "VOCAB SAVED STATE. Success: " + success);
    }

    protected Typeface getDefaultTypeface() {
        if (defaultTypeface == null) {
            File defaultFontFile = Config.instance().getDefaultFontFile();
            if (defaultFontFile != null && defaultFontFile.exists())
                defaultTypeface = Typeface.createFromFile(defaultFontFile);
        }
        return defaultTypeface;
    }

    protected String textFromClipboard() {
        return MobileUIPlatform.instance().getMobileNuiDirector().textFromClipboard();
    }

    protected void showToast(String message) {
        MobileUIPlatform.instance().getMobileNuiDirector().showToast(message);
    }
}
