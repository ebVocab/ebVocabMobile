package de.ebuchner.vocab.mobile.analysis;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.TextView;
import de.ebuchner.toolbox.i18n.I18NContext;
import de.ebuchner.vocab.mobile.R;
import de.ebuchner.vocab.mobile.platform.MobileUIPlatform;
import de.ebuchner.vocab.model.analysis.AnalysisController;
import de.ebuchner.vocab.model.analysis.AnalysisWindowBehaviour;
import de.ebuchner.vocab.model.translate.TranslateURL;
import de.ebuchner.vocab.nui.common.I18NLocator;

import java.io.UnsupportedEncodingException;

public class AnalysisPopup {
    private Activity parent;
    private I18NContext i18n = I18NLocator.locate();

    private AnalysisController controller = new AnalysisController(new MyBehaviour());
    private AnalysisWindowController windowController = new AnalysisWindowController();
    private TextView tfInput;
    private TextView tfOutput;
    private TextView tfUnicode;

    public void showPopup(Activity parent, String initialText) {
        this.parent = parent;

        View textAnalyzeView = parent.getLayoutInflater().inflate(
                R.layout.analysis,
                null
        );

        PopupWindow textAnalyzePopup = new PopupWindow(textAnalyzeView,
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT,
                true);

        textAnalyzePopup.setTouchable(true);
        textAnalyzePopup.setFocusable(true);

        TextView lbInput = (TextView) textAnalyzeView.findViewById(R.id.analysis_lbInput);
        lbInput.setText(i18n.getString("nui.analysis.input"));

        TextView lbOutput = (TextView) textAnalyzeView.findViewById(R.id.analysis_lbOutput);
        lbOutput.setText(i18n.getString("nui.analysis.output"));

        TextView lbUnicode = (TextView) textAnalyzeView.findViewById(R.id.analysis_lbUnicode);
        lbUnicode.setText(i18n.getString("nui.analysis.unicode"));

        tfInput = (TextView) textAnalyzeView.findViewById(R.id.analysis_tfInput);

        Button btnConvert = (Button) textAnalyzeView.findViewById(R.id.analysis_btnConvert);
        btnConvert.setText(i18n.getString("nui.analysis.convert"));
        btnConvert.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                controller.onConvert(tfInput.getText().toString());
            }
        });

        Button btnPaste = (Button) textAnalyzeView.findViewById(R.id.analysis_btnPaste);
        btnPaste.setText(i18n.getString("nui.analysis.paste"));
        btnPaste.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                windowController.onPaste();
            }
        });

        Button btnTranslate = (Button) textAnalyzeView.findViewById(R.id.analysis_btnTranslate);
        btnTranslate.setText(i18n.getString("nui.analysis.translate"));
        btnTranslate.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                windowController.onTranslate();
            }
        });

        tfOutput = (TextView) textAnalyzeView.findViewById(R.id.analysis_tfOutput);
        tfUnicode = (TextView) textAnalyzeView.findViewById(R.id.analysis_tfUnicode);

        Button btnClose = (Button) textAnalyzeView.findViewById(R.id.analysis_btnClose);
        btnClose.setText(i18n.getString("nui.close"));
        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                textAnalyzePopup.dismiss();
            }
        });

        if (initialText != null) {
            tfInput.setText(initialText);
            controller.onConvert(initialText);
        }


        textAnalyzePopup.showAtLocation(textAnalyzeView, Gravity.CENTER, 0, 0);
    }

    private class MyBehaviour implements AnalysisWindowBehaviour {
        public void showResult(String result) {
            tfOutput.setText(result);
        }

        public void showUnicode(String unicode) {
            tfUnicode.setText(unicode);
        }
    }

    private class AnalysisWindowController {
        public void onPaste() {
            String pasted = MobileUIPlatform.instance().getMobileNuiDirector().textFromClipboard();
            if (pasted == null) {
                MobileUIPlatform.instance().getMobileNuiDirector().showToast(
                        i18n.getString("nui.analysis.clipboard.empty")
                );
                return;
            }
            tfInput.setText(pasted);
            controller.onConvert(pasted);
        }

        public void onTranslate() {
            try {
                CharSequence text = tfInput.getText();
                if (text == null || text.toString().trim().length() == 0) {
                    MobileUIPlatform.instance().getMobileNuiDirector().showToast(
                            i18n.getString("nui.analysis.input.empty")
                    );
                    return;
                }
                String url = TranslateURL.url(text.toString().trim());
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(url));
                parent.startActivity(intent);
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
