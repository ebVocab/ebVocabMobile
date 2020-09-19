package de.ebuchner.vocab.mobile.practice;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.InputType;
import android.view.*;
import android.widget.*;
import de.ebuchner.toolbox.i18n.I18NContext;
import de.ebuchner.vocab.config.Config;
import de.ebuchner.vocab.config.fields.Field;
import de.ebuchner.vocab.config.fields.FieldFactory;
import de.ebuchner.vocab.mobile.R;
import de.ebuchner.vocab.mobile.common.DefaultFieldRenderer;
import de.ebuchner.vocab.mobile.common.FieldRenderer;
import de.ebuchner.vocab.mobile.common.VocabBaseActivity;
import de.ebuchner.vocab.mobile.platform.MobileUIPlatform;
import de.ebuchner.vocab.model.lessons.RepetitionMode;
import de.ebuchner.vocab.model.nui.WindowType;
import de.ebuchner.vocab.model.practice.*;
import de.ebuchner.vocab.nui.NuiDirector;
import de.ebuchner.vocab.nui.common.I18NLocator;

import java.io.File;
import java.util.*;

public class PracticeActivity extends VocabBaseActivity {
    private MobilePracticeController practiceController;
    private StrategyUI strategyUI = new StrategyUI();

    private Map<Field, FieldRenderer> fieldRenderer = new HashMap<Field, FieldRenderer>();
    private Map<Field, TextView> valueLabels = new HashMap<Field, TextView>();
    private Map<Field, TextView> valueFields = new HashMap<Field, TextView>();

    private TextView tfLesson;
    private MenuItem miReverse;
    private MenuItem miStatistics;
    private MenuItem miLessonShowOption;
    private boolean optMenuEnabled;
    private ImageButton btnNavigationBackward;
    private ImageButton btnNavigationForward;
    private ImageButton btnReshuffleRandom, btnReshuffleStatistics, btnReshuffleIntense, btnReshuffleBrowse;
    private ImageButton btnNavigationReset;
    private ProgressBar progressBar;
    private TextView lbStatus;
    private ImageButton btnRepetitionAdd;
    private ImageButton btnRepetitionRemove;
    private ImageButton btnRepetitionClear;
    private ImageButton btnRepetitionLoad;
    private Spinner spStrategy;
    private MenuItem miImage;
    private MenuItem miClipboard;
    private TextView lbRepetitionMode;
    private MyBehaviour myBehaviour;
    private ImageButton btnTextAnalyze;
    private MenuItem miUrduSpecial;

    @Override
    protected boolean requiresConfig() {
        return true;
    }

    @Override
    protected String titleResourceKey() {
        return "nui.practice.title";
    }

    @Override
    protected void onCreateImpl(Bundle savedInstanceState) {
        setContentView(R.layout.practice);

        myBehaviour = new MyBehaviour();
        practiceController = new MobilePracticeController(myBehaviour);
    }

    protected void onResumeImpl() {
        fieldRenderer.clear();
        valueLabels.clear();
        valueFields.clear();

        Typeface typeface = getDefaultTypeface();

        Config config = Config.instance();
        for (Field field : Config.instance().fieldList()) {
            if (config.getFieldRenderer(field.name()) != null)
                try {
                    fieldRenderer.put(
                            field,
                            (FieldRenderer) Class.forName(config.getFieldRenderer(field.name())).newInstance()
                    );
                } catch (Exception e) {
                    fieldRenderer.put(
                            field,
                            new DefaultFieldRenderer()
                    );
                }
            else
                fieldRenderer.put(
                        field,
                        new DefaultFieldRenderer()
                );
        }

        TextView tfLessonShowOption = (TextView) findViewById(R.id.practice_tfLessonShowOption);
        tfLessonShowOption.setText(i18n.getString("nui.practice.lesson"));

        tfLesson = (TextView) findViewById(R.id.practice_tfLesson);

        NuiDirector nuiDirector = MobileUIPlatform.instance().getNuiDirector();

        TextView lbForeign = (TextView) findViewById(R.id.practice_lbForeign);
        lbForeign.setText(nuiDirector.uiDescription(FieldFactory.getGenericField(FieldFactory.FOREIGN)));
        valueLabels.put(FieldFactory.getGenericField(FieldFactory.FOREIGN), lbForeign);

        TextView tfForeign = (TextView) findViewById(R.id.practice_tfForeign);
        if (typeface != null)
            tfForeign.setTypeface(typeface);
        valueFields.put(FieldFactory.getGenericField(FieldFactory.FOREIGN), tfForeign);

        if (isUrdu() && miUrduSpecial != null && miUrduSpecial.isChecked()) {
            Typeface typeFace = Typeface.createFromAsset(getAssets(), "NotoNastaliqUrdu-Regular.ttf");
            tfForeign.setTypeface(typeFace);
        }

        TextView lbType = (TextView) findViewById(R.id.practice_lbType);
        lbType.setText(nuiDirector.uiDescription(FieldFactory.getGenericField(FieldFactory.TYPE)));
        valueLabels.put(FieldFactory.getGenericField(FieldFactory.TYPE), lbType);

        TextView tfType = (TextView) findViewById(R.id.practice_tfType);
        if (typeface != null)
            tfType.setTypeface(typeface);
        valueFields.put(FieldFactory.getGenericField(FieldFactory.TYPE), tfType);

        TextView lbUser = (TextView) findViewById(R.id.practice_lbUser);
        lbUser.setText(nuiDirector.uiDescription(FieldFactory.getGenericField(FieldFactory.USER)));
        valueLabels.put(FieldFactory.getGenericField(FieldFactory.USER), lbUser);

        TextView tfUser = (TextView) findViewById(R.id.practice_tfUser);
        if (typeface != null)
            tfUser.setTypeface(typeface);
        valueFields.put(FieldFactory.getGenericField(FieldFactory.USER), tfUser);

        TextView lbComment = (TextView) findViewById(R.id.practice_lbComment);
        lbComment.setText(nuiDirector.uiDescription(FieldFactory.getGenericField(FieldFactory.COMMENT)));
        valueLabels.put(FieldFactory.getGenericField(FieldFactory.COMMENT), lbComment);

        TextView tfComment = (TextView) findViewById(R.id.practice_tfComment);
        if (typeface != null)
            tfComment.setTypeface(typeface);
        valueFields.put(FieldFactory.getGenericField(FieldFactory.COMMENT), tfComment);

        spStrategy = (Spinner) findViewById(R.id.practice_spStrategy);
        ArrayAdapter<SelectedStrategyItem> adapter = new ArrayAdapter<>(
                this, R.layout.practice_strategy_item, SelectedStrategyItem.createItems()
        );
        spStrategy.setAdapter(adapter);
        spStrategy.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                SelectedStrategyItem item = (SelectedStrategyItem) parent.getItemAtPosition(position);
                practiceController.onStrategyChanged(item.getStrategy());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        btnNavigationReset = (ImageButton) findViewById(R.id.practice_btnNavigationReset);
        btnNavigationReset.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                practiceController.onNavigationReset();
            }
        });

        btnNavigationBackward = (ImageButton) findViewById(R.id.practice_btnNavigationBackward);
        btnNavigationBackward.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                practiceController.onNavigationBackward();
            }
        });

        btnNavigationForward = (ImageButton) findViewById(R.id.practice_btnNavigationForward);
        btnNavigationForward.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                practiceController.onNavigationForward();
            }
        });

        View.OnClickListener reshuffleListener = new View.OnClickListener() {
            public void onClick(View view) {
                practiceController.onNavigationReshuffle();
            }
        };

        btnReshuffleRandom = (ImageButton) findViewById(R.id.practice_btnReshuffleRandom);
        btnReshuffleRandom.setOnClickListener(reshuffleListener);
        btnReshuffleRandom.setContentDescription("nui.practice.navigation.button.reshuffle.random");

        btnReshuffleStatistics = (ImageButton) findViewById(R.id.practice_btnReshuffleStatistics);
        btnReshuffleStatistics.setOnClickListener(reshuffleListener);
        btnReshuffleStatistics.setContentDescription("nui.practice.navigation.button.reshuffle.statistics");

        btnReshuffleIntense = (ImageButton) findViewById(R.id.practice_btnReshuffleIntense);
        btnReshuffleIntense.setOnClickListener(reshuffleListener);
        btnReshuffleIntense.setContentDescription("nui.practice.navigation.button.reshuffle.intense");

        btnReshuffleBrowse = (ImageButton) findViewById(R.id.practice_btnReshuffleBrowse);
        btnReshuffleBrowse.setOnClickListener(reshuffleListener);
        btnReshuffleBrowse.setContentDescription("nui.practice.navigation.button.reshuffle.browse");

        strategyUI.updateUI();

        lbRepetitionMode = (TextView) findViewById(R.id.practice_lbRepetitionMode);
        lbRepetitionMode.setText(i18n.getString("nui.practice.repetition"));
        lbRepetitionMode.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                myBehaviour.onRepetitionToggled();
            }
        });

        btnRepetitionAdd = (ImageButton) findViewById(R.id.practice_btnRepetitionAdd);
        btnRepetitionAdd.setContentDescription(i18n.getString("nui.practice.repetition.add"));
        btnRepetitionAdd.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                practiceController.onRepetitionAdd();
            }
        });

        btnRepetitionRemove = (ImageButton) findViewById(R.id.practice_btnRepetitionRemove);
        btnRepetitionRemove.setContentDescription(i18n.getString("nui.practice.repetition.remove"));
        btnRepetitionRemove.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                practiceController.onRepetitionRemove();
            }
        });

        btnRepetitionClear = (ImageButton) findViewById(R.id.practice_btnRepetitionClear);
        btnRepetitionClear.setContentDescription(i18n.getString("nui.practice.repetition.clear"));
        btnRepetitionClear.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {

                AlertDialog.Builder builder = new AlertDialog.Builder(PracticeActivity.this);
                builder.setTitle(i18n.getString(
                        "nui.practice.auto.save.title"
                ));

                final TextView message = new TextView(PracticeActivity.this);
                message.setText(i18n.getString("nui.practice.auto.save.message"));
                builder.setView(message);

                builder.setPositiveButton(i18n.getString("nui.yes"), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        practiceController.onRepetitionClear(true);
                    }
                });
                builder.setNegativeButton(i18n.getString("nui.no"), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        practiceController.onRepetitionClear(false);
                    }
                });

                builder.setNeutralButton(i18n.getString("nui.cancel"), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });

                builder.show();
            }
        });

        btnRepetitionLoad = (ImageButton) findViewById(R.id.practice_btnRepetitionLoad);
        btnRepetitionLoad.setContentDescription(i18n.getString("nui.practice.repetition.load"));
        btnRepetitionLoad.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                new RepetitionLoadPopup().showPopup(
                        PracticeActivity.this,
                        new RepetitionLoadListener() {
                            @Override
                            public void onRepetitionFileSelected(File repetitionFile) {
                                // todo ui freezes when repetition is loaded in intense mode...?
                                practiceController.onChangeRepetition(repetitionFile);
                            }

                            @Override
                            public void onRepetitionFileSelectionCanceled() {

                            }
                        }
                );
            }
        });

        progressBar = (ProgressBar) findViewById(R.id.practice_progressPanel);

        lbStatus = (TextView) findViewById(R.id.practice_lbStatus);

        btnTextAnalyze = (ImageButton) findViewById(R.id.practice_btnTextAnalyze);
        btnTextAnalyze.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                practiceController.onAnalysis();
            }
        });

        ImageButton btnSearch = (ImageButton) findViewById(R.id.practice_btnSearch);
        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                practiceController.onSearch();
            }
        });

        practiceController.onWindowWasCreated();
    }

    private boolean isUrdu() {
        return Config.instance().getLocale().equals("ur");
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if ((event.getActionMasked() & MotionEvent.ACTION_UP) > 0) {
            if (btnNavigationForward.isEnabled()) {
                btnNavigationForward.performClick();
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.practice_option_menu, menu);

        miReverse = menu.findItem(R.id.pr_opt_reverse);
        miReverse.setTitle(i18n.getString("nui.practice.reverse"));

        miImage = menu.findItem(R.id.pr_opt_image);
        miImage.setTitle(i18n.getString("nui.practice.image"));

        miStatistics = menu.findItem(R.id.pr_opt_showStatistics);
        miStatistics.setTitle(i18n.getString("nui.menu.options.show.statistics"));

        miLessonShowOption = menu.findItem(R.id.pr_opt_showLesson);
        miLessonShowOption.setTitle(i18n.getString("nui.practice.show.lesson"));

        miUrduSpecial = menu.findItem(R.id.pr_opt_urduSpecial);
        miUrduSpecial.setTitle(i18n.getString("nui.practice.urdu"));
        miUrduSpecial.setVisible(isUrdu());
        miUrduSpecial.setEnabled(true);

        MenuItem miLessons = menu.findItem(R.id.pr_open_lessons);
        miLessons.setTitle(i18n.getString("nui.menu.tools.lesson"));

        MenuItem miEditor = menu.findItem(R.id.pr_open_editor);
        miEditor.setTitle(i18n.getString("nui.menu.tools.editor"));

        MenuItem miOptions = menu.findItem(R.id.pr_opt);
        miOptions.setTitle(i18n.getString("nui.menu.tools.options"));

        MenuItem miProjects = menu.findItem(R.id.pr_open_projects);
        miProjects.setTitle(i18n.getString("nui.menu.tools.project"));

        MenuItem miAnalysis = menu.findItem(R.id.pr_open_analysis);
        miAnalysis.setTitle(i18n.getString("nui.menu.tools.analysis"));

        MenuItem miAbout = menu.findItem(R.id.pr_open_about);
        miAbout.setTitle(i18n.getString("nui.menu.tools.about"));

        miClipboard = menu.findItem(R.id.pr_opt_clipboard);
        miClipboard.setTitle(i18n.getString("nui.practice.clipboard"));

        NuiDirector nuiDirector = MobileUIPlatform.instance().getNuiDirector();

        MenuItem miClipboardForeign = menu.findItem(R.id.pr_opt_clipboard_foreign);
        miClipboardForeign.setTitle(nuiDirector.uiDescription(FieldFactory.getGenericField(FieldFactory.FOREIGN)));

        MenuItem miClipboardType = menu.findItem(R.id.pr_opt_clipboard_type);
        miClipboardType.setTitle(nuiDirector.uiDescription(FieldFactory.getGenericField(FieldFactory.TYPE)));

        MenuItem miClipboardUser = menu.findItem(R.id.pr_opt_clipboard_user);
        miClipboardUser.setTitle(nuiDirector.uiDescription(FieldFactory.getGenericField(FieldFactory.USER)));

        MenuItem miClipboardComment = menu.findItem(R.id.pr_opt_clipboard_comment);
        miClipboardComment.setTitle(nuiDirector.uiDescription(FieldFactory.getGenericField(FieldFactory.COMMENT)));

        optMenuEnabled = true;

        practiceController.onWindowWasCreated();

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.pr_opt_reverse:
                practiceController.onReverseOptionChanged(!item.isChecked());
                return true;
            case R.id.pr_opt_image:
                practiceController.onImageOptionChanged(!item.isChecked());
                return true;
            case R.id.pr_opt_showLesson:
                practiceController.onLessonShowOptionChanged(!item.isChecked());
                return true;
            case R.id.pr_opt_showStatistics:
                practiceController.onShowStatisticsOptionChanged(!item.isChecked());
                return true;
            case R.id.pr_open_lessons:
                practiceController.onOpenWindowType(WindowType.LESSONS_WINDOW);
                return true;
            case R.id.pr_open_editor:
                practiceController.onOpenWindowType(WindowType.EDITOR_WINDOW);
                return true;
            case R.id.pr_open_projects:
                practiceController.onOpenWindowType(WindowType.PROJECT_WINDOW);
                return true;
            case R.id.pr_open_analysis:
                practiceController.onAnalysis();
                return true;
            case R.id.pr_open_about:
                practiceController.onOpenWindowType(WindowType.ABOUT_WINDOW);
                return true;
            case R.id.pr_opt_clipboard_foreign:
                practiceController.onCopyText(FieldFactory.FOREIGN);
                return true;
            case R.id.pr_opt_clipboard_type:
                practiceController.onCopyText(FieldFactory.TYPE);
                return true;
            case R.id.pr_opt_clipboard_user:
                practiceController.onCopyText(FieldFactory.USER);
                return true;
            case R.id.pr_opt_clipboard_comment:
                practiceController.onCopyText(FieldFactory.COMMENT);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private static class SelectedStrategyItem {
        private final String name;
        private final SelectedStrategy strategy;
        I18NContext i18n = I18NLocator.locate();

        private SelectedStrategyItem(SelectedStrategy strategy) {
            this.strategy = strategy;
            String strategyResKey = null;
            if (strategy == SelectedStrategy.RANDOM)
                strategyResKey = "nui.practice.strategy.random";
            else if (strategy == SelectedStrategy.AGE)
                strategyResKey = "nui.practice.strategy.age";
            else if (strategy == SelectedStrategy.FREQUENCY)
                strategyResKey = "nui.practice.strategy.frequency";
            else if (strategy == SelectedStrategy.BROWSE)
                strategyResKey = "nui.practice.strategy.browse";
            else if (strategy == SelectedStrategy.INTENSE)
                strategyResKey = "nui.practice.strategy.intense";

            this.name =
                    strategyResKey == null ? "" :
                            /*i18n.getString(
                                    "nui.practice.strategy.label",
                                    Collections.singletonList(*/i18n.getString(strategyResKey)/*)
                            )*/
            ;
        }

        static List<SelectedStrategyItem> createItems() {
            List<SelectedStrategyItem> items = new ArrayList<>();
            for (SelectedStrategy strategy : SelectedStrategy.values()) {
                items.add(new SelectedStrategyItem(strategy));
            }
            return items;
        }

        public static int positionOf(SelectedStrategy aStrategy) {
            int pos = 0;
            for (SelectedStrategy strategy : SelectedStrategy.values()) {
                if (strategy.equals(aStrategy))
                    return pos;
                pos++;
            }
            return -1;
        }

        @Override
        public String toString() {
            return name;
        }

        public SelectedStrategy getStrategy() {
            return strategy;
        }
    }

    class MyBehaviour implements PracticeWindowBehaviour {

        private FieldRenderer lessonFieldRenderer = new DefaultFieldRenderer();
        private boolean repModeEnabled;
        private boolean repModeActive;
        private int repModeCount;

        public void setFieldEnabled(Field field, boolean enabled) {
            TextView valueField = valueFields.get(field);
            if (valueField != null) {
                if (!enabled)
                    valueField.setText("");
                valueField.setEnabled(enabled);
            }

            TextView valueLabel = valueLabels.get(field);
            if (valueLabel != null)
                valueLabel.setEnabled(enabled);

            if (FieldFactory.FOREIGN.equals(field.name()))
                btnTextAnalyze.setEnabled(enabled);
        }

        public void renderField(FieldRendererContext context) {
            FieldRenderer renderer = fieldRenderer.get(context.getField());
            if (renderer == null)
                throw new NullPointerException("No renderer found for " + context.getField().name());
            TextView label = valueLabels.get(context.getField());
            TextView textComponent = valueFields.get(context.getField());
            renderer.renderLabel(context, label);
            renderer.renderTextComponent(context, textComponent);
        }

        private boolean isImageHidden() {
            return !miImage.isChecked();
        }

        public void setStrategyOptionsEnabled(boolean enabled) {
            strategyUI.setEnabled(enabled);
        }

        public void setSelectedStrategy(SelectedStrategy strategy) {
            strategyUI.setStrategy(strategy);
        }

        public void setReverseOptionEnabled(boolean enabled) {
            if (!optMenuEnabled)
                return;
            miReverse.setEnabled(enabled);
        }

        public void setReverseOption(PracticeReverse reverseOption) {
            if (!optMenuEnabled)
                return;
            miReverse.setChecked(reverseOption == PracticeReverse.REVERSE);
        }

        @Override
        public void setImageOptionEnabled(boolean enabled) {
            if (!optMenuEnabled)
                return;
            miImage.setEnabled(enabled);
        }

        @Override
        public void setImageOption(PracticeImage imageOption) {
            if (!optMenuEnabled)
                return;
            miImage.setChecked(imageOption == PracticeImage.IMAGE);
        }

        public void setShowStatisticsOptionEnabled(boolean enabled) {
            if (!optMenuEnabled)
                return;
            miStatistics.setEnabled(enabled);
        }

        public void setShowStatisticsOption(boolean showStatistics) {
            if (!optMenuEnabled)
                return;
            miStatistics.setChecked(showStatistics);
        }

        public void setLessonFieldsEnabled(boolean enabled) {
            if (!optMenuEnabled)
                return;
            tfLesson.setEnabled(enabled);
            miLessonShowOption.setEnabled(enabled);
        }

        public void setLessonFieldFileRef(File fileRef) {
            lessonFieldRenderer.renderLessonComponent(fileRef, tfLesson);
        }

        public void setLessonFieldShow(boolean showLessonFile) {
            if (!optMenuEnabled)
                return;
            miLessonShowOption.setChecked(showLessonFile);
        }

        public void setRepetitionModeEnabled(boolean enabled) {
            this.repModeEnabled = enabled;
            updateRepMode();
        }

        public void setRepetitionMode(RepetitionMode repetitionMode) {
            this.repModeActive = (repetitionMode == RepetitionMode.ON);
            updateRepMode();
        }

        public void setRepetitionAddEnabled(boolean enabled) {
            btnRepetitionAdd.setEnabled(enabled);
        }

        public void setRepetitionAddVisible(boolean visible) {
            if (!visible)
                btnRepetitionAdd.setEnabled(false);
        }

        public void setRepetitionRemoveEnabled(boolean enabled) {
            btnRepetitionRemove.setEnabled(enabled);
        }

        public void setRepetitionRemoveVisible(boolean visible) {
            if (!visible)
                btnRepetitionRemove.setEnabled(false);
        }

        public void setRepetitionClearEnabled(boolean enabled) {
            btnRepetitionClear.setEnabled(enabled);
        }

        public void setRepetitionClearVisible(boolean visible) {
            if (!visible)
                btnRepetitionClear.setEnabled(false);
        }

        public void setRepetitionCount(int count) {
            this.repModeCount = count;
            updateRepMode();
        }

        private void updateRepMode() {
            if (!repModeEnabled) {
                lbRepetitionMode.setText(i18n.getString("nui.practice.repetition.label.disabled"));
                return;
            }

            String repLabelKey;
            if (repModeCount == 0)
                repLabelKey =
                        repModeActive ?
                                "nui.practice.repetition.label.active.0" :
                                "nui.practice.repetition.label.inactive.0";
            else
                repLabelKey =
                        repModeActive ?
                                "nui.practice.repetition.label.active.N" :
                                "nui.practice.repetition.label.inactive.N";

            lbRepetitionMode.setText(
                    i18n.getString(
                            repLabelKey,
                            Collections.singletonList(String.valueOf(repModeCount))
                    )
            );

            boolean focusOnRep = repModeEnabled && repModeActive;
            lbRepetitionMode.setBackgroundResource(
                    focusOnRep ? R.color.indiaOrange : R.color.white
            );
            lbRepetitionMode.setTextColor(getResources().getColor(
                    focusOnRep ? R.color.white : R.color.indiaBlue
                    )
            );
        }

        public void setPracticeProgress(PracticeProgress progress) {
            if (progress.isEmpty()) {
                progressBar.setProgress(0);
                lbStatus.setText(i18n.getString("nui.practice.default"));
            } else {
                String usageString = "";
                if (progress.isShowUsage()) {
                    usageString = i18n.getString(
                            "nui.practice.status.statistics",
                            Arrays.asList(
                                    String.valueOf(progress.getUsageCount()),
                                    progress.getLastUsageText()
                            )
                    );
                }


                progressBar.setProgress((int) Math.round(progress.getRatioCurrentOfTotal() * 100.0));
                if (progress.isRepetitionMode()) {
                    lbStatus.setText(
                            i18n.getString(
                                    "nui.practice.status.repetition.param",
                                    Arrays.asList(
                                            String.valueOf(progress.getTotalNumberOfEntries()),
                                            String.valueOf(progress.getCurrentPosition() + 1),
                                            progress.getRatioCurrentOfTotalText(),
                                            usageString
                                    )
                            )
                    );
                } else {
                    lbStatus.setText(
                            i18n.getString(
                                    "nui.practice.status.param",
                                    Arrays.asList(
                                            String.valueOf(progress.getNumberOfLessons()),
                                            String.valueOf(progress.getTotalNumberOfEntries()),
                                            String.valueOf(progress.getCurrentPosition() + 1),
                                            String.valueOf(progress.getRatioCurrentOfTotalText()),
                                            usageString
                                    )
                            )
                    );
                }
            }
        }

        public void firePracticeWindowOnFocusChanged() {
        }

        public void firePracticeWindowOnUnFocus() {
        }

        public void setNavigationReshuffleEnabled(boolean enabled) {
            btnReshuffleRandom.setEnabled(enabled);
            btnReshuffleStatistics.setEnabled(enabled);
            btnReshuffleIntense.setEnabled(enabled);
            btnReshuffleBrowse.setEnabled(enabled);
            strategyUI.updateUI();
        }

        public void setNavigationResetEnabled(boolean enabled) {
            btnNavigationReset.setEnabled(enabled);
            strategyUI.updateUI();
        }

        public void askGotoPosition(int maxGotoPosition) {
            new GotoInput().askGotoPosition(maxGotoPosition);
        }

        public void setNavigationForwardEnabled(boolean enabled) {
            btnNavigationForward.setEnabled(enabled);
            strategyUI.updateUI();
        }

        public void setNavigationBackwardEnabled(boolean enabled) {
            btnNavigationBackward.setEnabled(enabled);
            strategyUI.updateUI();
        }

        public void sendRepetitionSavedSuccess(File file) {
            MobileUIPlatform.instance().getMobileNuiDirector().showToast(i18n.getString("nui.practice.repetition.saved"));
        }

        public void onRepetitionToggled() {
            if (repModeEnabled)
                practiceController.onRepetitionToggled();
        }
    }

    private class GotoInput {

        public void askGotoPosition(int maxGotoPosition) {
            AlertDialog.Builder builder = new AlertDialog.Builder(PracticeActivity.this);
            builder.setTitle(i18n.getString(
                    "nui.practice.goto.input",
                    Collections.singletonList(maxGotoPosition)
            ));

            final EditText input = new EditText(PracticeActivity.this);
            input.setInputType(InputType.TYPE_CLASS_NUMBER);
            builder.setView(input);

            builder.setPositiveButton(i18n.getString("nui.ok"), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    practiceController.onAskGotoPositionResult(input.getText().toString());
                }
            });
            builder.setNegativeButton(i18n.getString("nui.cancel"), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });

            builder.show();
        }
    }

    private class StrategyUI {
        private boolean enabled;
        private SelectedStrategy strategy;

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
            updateUI();
        }

        public void setStrategy(SelectedStrategy strategy) {
            this.strategy = strategy;
            updateUI();
        }

        public void updateUI() {
            spStrategy.setEnabled(enabled);
            int selectedStrategyPos = SelectedStrategyItem.positionOf(strategy);
            if (selectedStrategyPos >= 0)
                spStrategy.setSelection(selectedStrategyPos);

            btnReshuffleRandom.setVisibility(strategy == SelectedStrategy.RANDOM ? View.VISIBLE : View.GONE);
            btnReshuffleStatistics.setVisibility(strategy == SelectedStrategy.AGE
                    || strategy == SelectedStrategy.FREQUENCY ? View.VISIBLE : View.GONE);
            btnReshuffleIntense.setVisibility(strategy == SelectedStrategy.INTENSE ? View.VISIBLE : View.GONE);
            btnReshuffleBrowse.setVisibility(strategy == SelectedStrategy.BROWSE ? View.VISIBLE : View.GONE);

            if (optMenuEnabled)
                miClipboard.setEnabled(enabled);
        }
    }

}
