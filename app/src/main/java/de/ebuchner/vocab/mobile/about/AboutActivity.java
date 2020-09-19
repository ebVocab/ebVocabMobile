package de.ebuchner.vocab.mobile.about;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.UnderlineSpan;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import de.ebuchner.vocab.config.Config;
import de.ebuchner.vocab.config.VocabEnvironment;
import de.ebuchner.vocab.mobile.R;
import de.ebuchner.vocab.mobile.common.Density;
import de.ebuchner.vocab.mobile.common.ScreenSize;
import de.ebuchner.vocab.mobile.common.VocabBaseActivity;
import de.ebuchner.vocab.model.nui.WindowType;
import de.ebuchner.vocab.model.nui.platform.UIPlatformFactory;

import java.util.Arrays;

public class AboutActivity extends VocabBaseActivity {

    AboutWindowController controller = new AboutWindowController();

    @Override
    protected String titleResourceKey() {
        return "nui.about.title";
    }

    @Override
    protected void onCreateImpl(Bundle savedInstanceState) {
        setContentView(R.layout.about);

        TextView lbWebsite = (TextView) findViewById(R.id.about_lbWebsite);
        lbWebsite.setText(i18n.getString("nui.about.website"));

        TextView tfWebsite = (TextView) findViewById(R.id.about_tfWebsite);

        String sWebsite = i18n.getString("nui.about.website.description");
        Spannable websiteSpan = new SpannableString(sWebsite);

        websiteSpan.setSpan(new UnderlineSpan(), 0, sWebsite.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        tfWebsite.setText(websiteSpan);
        tfWebsite.setClickable(true);
        tfWebsite.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                controller.onWebsiteClick();
            }
        });

        TextView lbProgramVersion = (TextView) findViewById(R.id.about_lbProgramVersion);
        lbProgramVersion.setText(i18n.getString("nui.about.version"));

        TextView tfProgramVersion = (TextView) findViewById(R.id.about_tfProgramVersion);
        try {
            tfProgramVersion.setText(
                    i18n.getString(
                            "nui.about.version.value",
                            Arrays.asList(
                                    VocabEnvironment.APP_VERSION,
                                    String.valueOf(getPackageManager().getPackageInfo(
                                            getPackageName(), 0
                                    ).versionCode)
                            )
                    )

            );
        } catch (PackageManager.NameNotFoundException e) {
            tfProgramVersion.setText(VocabEnvironment.APP_VERSION);
        }

        TextView lbOSVersion = (TextView) findViewById(R.id.about_lbOSVersion);
        lbOSVersion.setText(i18n.getString("nui.about.os"));

        TextView tfOSVersion = (TextView) findViewById(R.id.about_tfOSVersion);
        tfOSVersion.setText(UIPlatformFactory.getUIPlatform().uiRuntimeName());

        TextView lbScreenSize = (TextView) findViewById(R.id.about_lbScreenSize);
        lbScreenSize.setText(i18n.getString("nui.about.screen.size"));

        TextView tfScreenSize = (TextView) findViewById(R.id.about_tfScreenSize);
        tfScreenSize.setText(ScreenSize.screenSizeName(this).name());

        TextView lbDensity = (TextView) findViewById(R.id.about_lbDensity);
        lbDensity.setText(i18n.getString("nui.about.density"));

        TextView tfDensity = (TextView) findViewById(R.id.about_tfDensity);
        tfDensity.setText(Density.densityType(this).name());

        Button btnBack = (Button) findViewById(R.id.about_btnBack);
        btnBack.setText(i18n.getString("nui.about.back"));
        btnBack.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                if (Config.projectInitialized())
                    UIPlatformFactory.getUIPlatform().getNuiDirector().showWindow(WindowType.PRACTICE_WINDOW);
                else
                    UIPlatformFactory.getUIPlatform().getNuiDirector().showWindow(WindowType.PROJECT_WINDOW);
            }
        });

    }

    class AboutWindowController {
        void onWebsiteClick() {
            Intent browserIntent = new Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse(VocabEnvironment.APP_WEBSITE)
            );
            startActivity(browserIntent);
        }
    }

}
