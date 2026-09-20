package Com.Mint.Standoff2;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.InputType;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Random;

public final class MainActivity extends Activity {
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final DecelerateInterpolator smooth = new DecelerateInterpolator();
    private final Random flowRandom = new Random();

    private FrameLayout root;
    private ImageView backgroundLogo;
    private ModernPanel loginPanel;
    private LinearLayout loginContent;
    private LinearLayout finalErrorContainer;
    private ProgressBar panelLoader;
    private TextView connectionStatus;
    private Button okButton;
    private Button loginButton;
    private EditText keyInput;
    private TextView getKeyText;
    private boolean authorizationInProgress;
    private boolean telegramOpening;
    private boolean loadingActive;
    private int authAttemptId;

    private static final int COLOR_ROOT = Color.rgb(241, 245, 249);
    private static final int COLOR_PANEL = Color.rgb(255, 255, 255);
    private static final int COLOR_PANEL_ERROR = Color.rgb(254, 242, 242);
    private static final int COLOR_BORDER = Color.rgb(203, 213, 225);
    private static final int COLOR_BORDER_ERROR = Color.rgb(252, 165, 165);
    private static final int COLOR_ACCENT = Color.rgb(37, 99, 235);
    private static final int COLOR_ACCENT_ERROR = Color.rgb(220, 38, 38);
    private static final int COLOR_TEXT = Color.rgb(15, 23, 42);
    private static final int COLOR_TEXT_HINT = Color.rgb(100, 116, 139);
    private static final int COLOR_INPUT_FILL = Color.rgb(241, 245, 249);
    private static final int COLOR_INPUT_STROKE = Color.rgb(148, 163, 184);
    private static final int COLOR_BUTTON_FILL = Color.rgb(37, 99, 235);
    private static final int COLOR_BUTTON_STROKE = Color.rgb(29, 78, 216);
    private static final int COLOR_BUTTON_ERROR_FILL = Color.rgb(220, 38, 38);
    private static final int COLOR_BUTTON_ERROR_STROKE = Color.rgb(185, 28, 28);
    private static final int COLOR_RIPPLE = Color.argb(42, 255, 255, 255);
    private static final int COLOR_RAIN = Color.rgb(96, 165, 250);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        Window window = getWindow();
        window.setStatusBarColor(COLOR_PANEL);
        window.setNavigationBarColor(COLOR_PANEL);
        window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
        );
        applyImmersiveMode();

        root = new FrameLayout(this);
        root.setBackgroundColor(COLOR_ROOT);
        setContentView(root);

        backgroundLogo = new ImageView(this);
        backgroundLogo.setImageResource(R.drawable.axlebolt_background);
        backgroundLogo.setScaleType(ImageView.ScaleType.CENTER_CROP);
        backgroundLogo.setScaleX(0.5f);
        backgroundLogo.setScaleY(0.5f);
        backgroundLogo.setAlpha(0f);
        backgroundLogo.setVisibility(View.GONE);
        root.addView(backgroundLogo, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
        ));

        handler.postDelayed(() -> {
            if (!isFinishing()) {
                Toast.makeText(this, "You are not logged in", Toast.LENGTH_SHORT).show();
            }
        }, 500L);

        handler.postDelayed(() -> {
            if (!isFinishing()) {
                showLoginPanel();
            }
        }, 1000L);

        handler.postDelayed(this::fadeInBackgroundLogo, 3000L);
        handler.postDelayed(this::fadeOutBackgroundLogo, 6000L);
    }

    private void fadeInBackgroundLogo() {
        if (isFinishing()) {
            return;
        }
        backgroundLogo.setVisibility(View.VISIBLE);
        backgroundLogo.animate()
                .alpha(1f)
                .setDuration(700L)
                .setInterpolator(smooth)
                .start();
    }

    private void fadeOutBackgroundLogo() {
        if (isFinishing() || backgroundLogo.getVisibility() != View.VISIBLE) {
            return;
        }
        backgroundLogo.animate()
                .alpha(0f)
                .setDuration(700L)
                .setInterpolator(smooth)
                .withEndAction(() -> backgroundLogo.setVisibility(View.GONE))
                .start();
    }

    private void showLoginPanel() {
        if (loginPanel != null) {
            return;
        }

        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        int screenHeight = getResources().getDisplayMetrics().heightPixels;
        int panelWidth = Math.min(dp(420), screenWidth - dp(48));
        int panelHeight = Math.min(dp(310), screenHeight - dp(32));

        loginPanel = new ModernPanel(this, true, true);
        loginPanel.setPadding(dp(32), dp(24), dp(32), dp(24));

        loginContent = new LinearLayout(this);
        loginContent.setOrientation(LinearLayout.VERTICAL);
        loginContent.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL);
        loginPanel.addView(loginContent, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
        ));

        TextView title = new TextView(this);
        title.setText("Enter your key");
        title.setTextColor(COLOR_TEXT);
        title.setTextSize(19f);
        title.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
        title.setLetterSpacing(0.04f);
        title.setGravity(Gravity.CENTER);
        title.setBackgroundColor(COLOR_PANEL);
        title.setPadding(0, dp(4), 0, dp(4));
        loginContent.addView(title, linearParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                0
        ));

        keyInput = new EditText(this);
        keyInput.setSingleLine(true);
        keyInput.setTextColor(COLOR_TEXT);
        keyInput.setTextSize(16f);
        keyInput.setTypeface(Typeface.MONOSPACE, Typeface.NORMAL);
        keyInput.setHint("t.me/Morphinecome");
        keyInput.setHintTextColor(COLOR_TEXT_HINT);
        keyInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        keyInput.setPadding(dp(14), 0, dp(14), 0);
        keyInput.setBackground(roundedBox(
                COLOR_INPUT_FILL,
                COLOR_INPUT_STROKE,
                6f,
                1f
        ));
        loginContent.addView(keyInput, linearParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(48),
                dp(18)
        ));

        loginButton = new Button(this);
        loginButton.setText("Login");
        loginButton.setTextColor(Color.WHITE);
        loginButton.setTextSize(16f);
        loginButton.setAllCaps(false);
        loginButton.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
        loginButton.setLetterSpacing(0.03f);
        loginButton.setStateListAnimator(null);
        GradientDrawable buttonShape = roundedBox(
                COLOR_BUTTON_FILL,
                COLOR_BUTTON_STROKE,
                6f,
                1f
        );
        loginButton.setBackground(new RippleDrawable(
                ColorStateList.valueOf(COLOR_RIPPLE),
                buttonShape,
                null
        ));
        loginButton.setOnClickListener(v -> beginAuthorization());
        loginContent.addView(loginButton, linearParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(48),
                dp(14)
        ));

        getKeyText = new TextView(this);
        getKeyText.setTextSize(15f);
        getKeyText.setTypeface(Typeface.MONOSPACE, Typeface.NORMAL);
        getKeyText.setGravity(Gravity.CENTER);
        getKeyText.setBackgroundColor(COLOR_PANEL);
        showGetKeyLink();
        loginContent.addView(getKeyText, linearParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(44),
                dp(8)
        ));

        panelLoader = new ProgressBar(this, null, android.R.attr.progressBarStyleLarge);
        panelLoader.setIndeterminateTintList(ColorStateList.valueOf(COLOR_ACCENT));
        panelLoader.setVisibility(View.GONE);
        loginPanel.addView(panelLoader, new FrameLayout.LayoutParams(
                dp(58),
                dp(58),
                Gravity.CENTER
        ));

        finalErrorContainer = new LinearLayout(this);
        finalErrorContainer.setOrientation(LinearLayout.VERTICAL);
        finalErrorContainer.setGravity(Gravity.CENTER);
        finalErrorContainer.setPadding(dp(24), dp(22), dp(24), dp(22));
        finalErrorContainer.setBackground(roundedBox(
                COLOR_PANEL_ERROR,
                COLOR_BORDER_ERROR,
                6f,
                1f
        ));
        finalErrorContainer.setVisibility(View.GONE);

        connectionStatus = new TextView(this);
        connectionStatus.setTextColor(COLOR_ACCENT_ERROR);
        connectionStatus.setTextSize(18f);
        connectionStatus.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
        connectionStatus.setLetterSpacing(0.01f);
        connectionStatus.setGravity(Gravity.CENTER);
        connectionStatus.setPadding(dp(20), 0, dp(20), 0);
        finalErrorContainer.addView(connectionStatus, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));

        okButton = new Button(this);
        okButton.setText("Ok");
        okButton.setTextColor(Color.WHITE);
        okButton.setTextSize(16f);
        okButton.setAllCaps(false);
        okButton.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
        okButton.setLetterSpacing(0.03f);
        okButton.setStateListAnimator(null);
        GradientDrawable okShape = roundedBox(
                COLOR_BUTTON_ERROR_FILL,
                COLOR_BUTTON_ERROR_STROKE,
                6f,
                1f
        );
        okButton.setBackground(new RippleDrawable(
                ColorStateList.valueOf(COLOR_RIPPLE),
                okShape,
                null
        ));
        okButton.setOnClickListener(v -> closeApplication());
        LinearLayout.LayoutParams okParams = new LinearLayout.LayoutParams(dp(112), dp(46));
        okParams.topMargin = dp(18);
        finalErrorContainer.addView(okButton, okParams);

        FrameLayout.LayoutParams finalErrorParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT,
                Gravity.CENTER
        );
        finalErrorParams.setMargins(dp(16), dp(16), dp(16), dp(16));
        loginPanel.addView(finalErrorContainer, finalErrorParams);

        FrameLayout.LayoutParams panelParams = new FrameLayout.LayoutParams(
                panelWidth,
                panelHeight,
                Gravity.CENTER
        );
        root.addView(loginPanel, panelParams);

        loginPanel.setAlpha(0f);
        loginPanel.setScaleX(0.96f);
        loginPanel.setScaleY(0.96f);
        loginPanel.setTranslationY(dp(12));
        loginPanel.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .translationY(0f)
                .setDuration(460L)
                .setInterpolator(smooth)
                .withLayer()
                .start();
    }

    private void beginAuthorization() {
        if (authorizationInProgress) {
            return;
        }

        authorizationInProgress = true;
        loginPanel.setErrorMode(false);
        setLoginButtonErrorStyle(false);
        final int attemptId = ++authAttemptId;
        String key = keyInput.getText().toString();
        boolean valid = key.startsWith("PlutCC-") || key.startsWith("ZENIN-");

        loginButton.setText("Awaiting authorization");
        loginButton.setEnabled(false);
        keyInput.setEnabled(false);
        getKeyText.setClickable(false);

        handler.postDelayed(() -> {
            if (valid) {
                showLoadingInsidePanel();
            } else {
                showInvalidKeyState(attemptId);
            }
        }, 650L);
    }

    private void showInvalidKeyState(int attemptId) {
        authorizationInProgress = false;
        loginPanel.setErrorMode(true);
        setLoginButtonErrorStyle(true);
        loginButton.setText("Failed to auth");
        loginButton.setEnabled(true);
        keyInput.setEnabled(true);

        getKeyText.setText("Expired or invalid key");
        getKeyText.setTextColor(COLOR_ACCENT_ERROR);
        getKeyText.setOnClickListener(null);
        getKeyText.setClickable(false);

        ObjectAnimator shake = ObjectAnimator.ofFloat(
                loginButton,
                View.TRANSLATION_X,
                0f,
                -dp(11),
                dp(11),
                -dp(8),
                dp(8),
                -dp(4),
                dp(4),
                0f
        );
        shake.setDuration(520L);
        shake.start();

        handler.postDelayed(() -> {
            if (attemptId == authAttemptId && !authorizationInProgress) {
                resetLoginForm();
            }
        }, 4000L);
    }

    private void resetLoginForm() {
        loginPanel.setErrorMode(false);
        setLoginButtonErrorStyle(false);
        keyInput.setText("");
        keyInput.setEnabled(true);
        loginButton.setTranslationX(0f);
        loginButton.setText("Login");
        loginButton.setEnabled(true);
        showGetKeyLink();
    }

    private void showLoadingInsidePanel() {
        loadingActive = true;

        loginContent.animate()
                .alpha(0f)
                .setDuration(240L)
                .setInterpolator(smooth)
                .withEndAction(() -> loginContent.setVisibility(View.INVISIBLE))
                .start();

        panelLoader.setAlpha(0f);
        panelLoader.setScaleX(0.86f);
        panelLoader.setScaleY(0.86f);
        panelLoader.setVisibility(View.VISIBLE);
        panelLoader.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(300L)
                .setInterpolator(smooth)
                .start();

        handler.postDelayed(() -> {
            if (loadingActive) {
                showDeviceWarning();
            }
        }, 7000L);
    }

    private void showConnectionFailure() {
        if (!loadingActive) {
            return;
        }
        loadingActive = false;
        loginPanel.setErrorMode(true);
        connectionStatus.setText(flowRandom.nextBoolean()
                ? "Bad HWID )"
                : "Device not supported,Fatal ERROR");

        panelLoader.animate()
                .alpha(0f)
                .setDuration(220L)
                .setInterpolator(smooth)
                .withEndAction(() -> {
                    panelLoader.setVisibility(View.GONE);
                    finalErrorContainer.setAlpha(0f);
                    finalErrorContainer.setScaleX(0.96f);
                    finalErrorContainer.setScaleY(0.96f);
                    finalErrorContainer.setVisibility(View.VISIBLE);
                    finalErrorContainer.animate()
                            .alpha(1f)
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(320L)
                            .setInterpolator(smooth)
                            .start();
                })
                .start();
    }

    private void showDeviceWarning() {
        if (!loadingActive) {
            return;
        }
        panelLoader.setVisibility(View.INVISIBLE);

        ModernPanel warningPanel = new ModernPanel(this, false);
        warningPanel.setPadding(dp(28), dp(24), dp(28), dp(24));

        LinearLayout warningContent = new LinearLayout(this);
        warningContent.setOrientation(LinearLayout.VERTICAL);
        warningContent.setGravity(Gravity.CENTER);
        warningPanel.addView(warningContent, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
        ));

        TextView warningText = new TextView(this);
        warningText.setText("You server connection failed,you want to try another server? ");
        warningText.setTextColor(COLOR_TEXT);
        warningText.setTextSize(16f);
        warningText.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
        warningText.setLetterSpacing(0.01f);
        warningText.setGravity(Gravity.CENTER);
        warningContent.addView(warningText, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
        ));

        LinearLayout warningButtons = new LinearLayout(this);
        warningButtons.setOrientation(LinearLayout.HORIZONTAL);
        warningButtons.setGravity(Gravity.CENTER);
        Button noButton = createPanelButton("No");
        Button yesButton = createPanelButton("Yes");
        LinearLayout.LayoutParams noParams = new LinearLayout.LayoutParams(0, dp(48), 1f);
        LinearLayout.LayoutParams yesParams = new LinearLayout.LayoutParams(0, dp(48), 1f);
        yesParams.leftMargin = dp(12);
        warningButtons.addView(noButton, noParams);
        warningButtons.addView(yesButton, yesParams);
        warningContent.addView(warningButtons, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(48)
        ));

        noButton.setOnClickListener(v -> dismissOverlayPanel(
                warningPanel,
                this::resumeLoadingAfterWarning
        ));
        yesButton.setOnClickListener(v -> dismissOverlayPanel(warningPanel, () -> {
            resumeLoadingAfterWarning();
            showFixFailurePanel();
        }));

        int panelWidth = Math.min(dp(500), root.getWidth() - dp(48));
        int panelHeight = Math.min(dp(230), root.getHeight() - dp(32));
        root.addView(warningPanel, new FrameLayout.LayoutParams(
                panelWidth,
                panelHeight,
                Gravity.CENTER
        ));
        warningPanel.setAlpha(0f);
        warningPanel.setScaleX(0.96f);
        warningPanel.setScaleY(0.96f);
        warningPanel.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(260L)
                .setInterpolator(smooth)
                .start();
    }

    private void resumeLoadingAfterWarning() {
        if (!loadingActive) {
            return;
        }
        panelLoader.setIndeterminate(true);
        panelLoader.setAlpha(1f);
        panelLoader.setVisibility(View.VISIBLE);

        handler.postDelayed(() -> {
            if (loadingActive) {
                showConnectionFailure();
            }
        }, 7000L);
    }

    private void showFixFailurePanel() {
        ModernPanel failedFixPanel = new ModernPanel(this, false);
        failedFixPanel.setErrorMode(true);
        failedFixPanel.setPadding(dp(24), dp(18), dp(24), dp(18));
        TextView failedFixText = new TextView(this);
        failedFixText.setText("Failed to enable fix");
        failedFixText.setTextColor(COLOR_ACCENT_ERROR);
        failedFixText.setTextSize(18f);
        failedFixText.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
        failedFixText.setGravity(Gravity.CENTER);
        failedFixPanel.addView(failedFixText, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
        ));

        int panelWidth = Math.min(dp(360), root.getWidth() - dp(48));
        int panelHeight = Math.min(dp(150), root.getHeight() - dp(32));
        root.addView(failedFixPanel, new FrameLayout.LayoutParams(
                panelWidth,
                panelHeight,
                Gravity.CENTER
        ));
        failedFixPanel.setAlpha(0f);
        failedFixPanel.setScaleX(0.96f);
        failedFixPanel.setScaleY(0.96f);
        failedFixPanel.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(220L)
                .setInterpolator(smooth)
                .start();

        handler.postDelayed(() -> dismissOverlayPanel(failedFixPanel, null), 1300L);
    }

    private Button createPanelButton(String text) {
        Button button = new Button(this);
        button.setText(text);
        button.setTextColor(Color.WHITE);
        button.setTextSize(16f);
        button.setAllCaps(false);
        button.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
        button.setLetterSpacing(0.03f);
        button.setStateListAnimator(null);
        GradientDrawable shape = roundedBox(
                COLOR_BUTTON_FILL,
                COLOR_BUTTON_STROKE,
                6f,
                1f
        );
        button.setBackground(new RippleDrawable(
                ColorStateList.valueOf(COLOR_RIPPLE),
                shape,
                null
        ));
        return button;
    }

    private void setLoginButtonErrorStyle(boolean error) {
        GradientDrawable shape = roundedBox(
                error ? COLOR_BUTTON_ERROR_FILL : COLOR_BUTTON_FILL,
                error ? COLOR_BUTTON_ERROR_STROKE : COLOR_BUTTON_STROKE,
                6f,
                1f
        );
        loginButton.setBackground(new RippleDrawable(
                ColorStateList.valueOf(COLOR_RIPPLE),
                shape,
                null
        ));
    }

    private void dismissOverlayPanel(ModernPanel panel, Runnable afterDismiss) {
        if (panel.getParent() == null) {
            return;
        }
        panel.animate()
                .alpha(0f)
                .scaleX(0.96f)
                .scaleY(0.96f)
                .setDuration(200L)
                .setInterpolator(smooth)
                .withEndAction(() -> {
                    root.removeView(panel);
                    if (afterDismiss != null) {
                        afterDismiss.run();
                    }
                })
                .start();
    }

    private void closeApplication() {
        finishAndRemoveTask();
    }

    private void showGetKeyLink() {
        getKeyText.setText("Don’t have key? Get key here");
        getKeyText.setTextColor(COLOR_ACCENT);
        getKeyText.setClickable(true);
        getKeyText.setOnClickListener(v -> openKeyBot());
    }

    private void openKeyBot() {
        if (telegramOpening) {
            return;
        }
        telegramOpening = true;
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/"));
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException ignored) {
        }
        handler.postDelayed(() -> telegramOpening = false, 1800L);
    }

    private void applyImmersiveMode() {
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        );
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            applyImmersiveMode();
        }
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }

    private LinearLayout.LayoutParams linearParams(int width, int height, int topMargin) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(width, height);
        params.topMargin = topMargin;
        return params;
    }

    private GradientDrawable roundedBox(int fillColor, int strokeColor, float radiusDp, float strokeDp) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(fillColor);
        drawable.setCornerRadius(dp(radiusDp));
        drawable.setStroke(dp(strokeDp), strokeColor);
        return drawable;
    }

    private int dp(float value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private final class ModernPanel extends FrameLayout {
        private static final int PANEL_DROP_COUNT = 38;

        private final Paint backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint accentPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint rainPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final RectF panelRect = new RectF();
        private final Random rainRandom = new Random();
        private final float[] rainX = new float[PANEL_DROP_COUNT];
        private final float[] rainY = new float[PANEL_DROP_COUNT];
        private final float[] rainSpeed = new float[PANEL_DROP_COUNT];
        private final float[] rainLength = new float[PANEL_DROP_COUNT];
        private final int[] rainAlpha = new int[PANEL_DROP_COUNT];

        private final boolean canDrag;
        private final boolean rainEnabled;
        private boolean dragging;
        private float downRawX;
        private float downRawY;
        private float startTranslationX;
        private float startTranslationY;
        private long lastRainFrameNanos;

        ModernPanel(Context context) {
            this(context, true, false);
        }

        ModernPanel(Context context, boolean canDrag) {
            this(context, canDrag, false);
        }

        ModernPanel(Context context, boolean canDrag, boolean rainEnabled) {
            super(context);
            this.canDrag = canDrag;
            this.rainEnabled = rainEnabled;
            setWillNotDraw(false);
            setClickable(true);
            backgroundPaint.setColor(COLOR_PANEL);
            borderPaint.setStyle(Paint.Style.STROKE);
            borderPaint.setStrokeWidth(dp(1f));
            borderPaint.setColor(COLOR_BORDER);
            accentPaint.setStyle(Paint.Style.FILL);
            accentPaint.setColor(COLOR_ACCENT);
            rainPaint.setColor(COLOR_RAIN);
            rainPaint.setStrokeWidth(dp(1f));
            rainPaint.setStrokeCap(Paint.Cap.ROUND);
        }

        @Override
        protected void onSizeChanged(int width, int height, int oldWidth, int oldHeight) {
            super.onSizeChanged(width, height, oldWidth, oldHeight);
            panelRect.set(dp(1), dp(1), width - dp(1), height - dp(1));
            if (rainEnabled) {
                for (int i = 0; i < PANEL_DROP_COUNT; i++) {
                    resetPanelDrop(i, width, height, true);
                }
                lastRainFrameNanos = 0L;
            }
        }

        private void resetPanelDrop(int index, int width, int height, boolean anywhere) {
            rainX[index] = rainRandom.nextFloat() * Math.max(1, width + dp(30));
            rainY[index] = anywhere
                    ? rainRandom.nextFloat() * Math.max(1, height)
                    : -rainRandom.nextFloat() * Math.max(1, height) * 0.35f;
            rainSpeed[index] = dp(78 + rainRandom.nextFloat() * 92);
            rainLength[index] = dp(7 + rainRandom.nextFloat() * 14);
            rainAlpha[index] = 24 + rainRandom.nextInt(38);
        }

        void setErrorMode(boolean enabled) {
            backgroundPaint.setColor(enabled
                    ? COLOR_PANEL_ERROR
                    : COLOR_PANEL);
            borderPaint.setColor(enabled
                    ? COLOR_BORDER_ERROR
                    : COLOR_BORDER);
            accentPaint.setColor(enabled
                    ? COLOR_ACCENT_ERROR
                    : COLOR_ACCENT);
            invalidate();
        }

        @Override
        public boolean onInterceptTouchEvent(MotionEvent event) {
            if (!canDrag) {
                return false;
            }
            if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
                if (touchesInteractiveView(event.getRawX(), event.getRawY())) {
                    dragging = false;
                    return false;
                }
                dragging = true;
                downRawX = event.getRawX();
                downRawY = event.getRawY();
                startTranslationX = getTranslationX();
                startTranslationY = getTranslationY();
                return true;
            }
            return dragging;
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            if (!canDrag) {
                return super.onTouchEvent(event);
            }
            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    return true;
                case MotionEvent.ACTION_MOVE:
                    if (!dragging) {
                        return false;
                    }
                    float wantedX = startTranslationX + event.getRawX() - downRawX;
                    float wantedY = startTranslationY + event.getRawY() - downRawY;
                    float minX = -getLeft();
                    float maxX = root.getWidth() - getLeft() - getWidth();
                    float minY = -getTop();
                    float maxY = root.getHeight() - getTop() - getHeight();
                    setTranslationX(Math.max(minX, Math.min(maxX, wantedX)));
                    setTranslationY(Math.max(minY, Math.min(maxY, wantedY)));
                    return true;
                case MotionEvent.ACTION_UP:
                    dragging = false;
                    performClick();
                    return true;
                case MotionEvent.ACTION_CANCEL:
                    dragging = false;
                    return true;
                default:
                    return true;
            }
        }

        @Override
        public boolean performClick() {
            super.performClick();
            return true;
        }

        private boolean touchesInteractiveView(float rawX, float rawY) {
            return pointInside(keyInput, rawX, rawY)
                    || pointInside(loginButton, rawX, rawY)
                    || pointInside(getKeyText, rawX, rawY)
                    || pointInside(okButton, rawX, rawY);
        }

        private boolean pointInside(View view, float rawX, float rawY) {
            if (view == null || !view.isShown()) {
                return false;
            }
            int[] location = new int[2];
            view.getLocationOnScreen(location);
            return rawX >= location[0]
                    && rawX <= location[0] + view.getWidth()
                    && rawY >= location[1]
                    && rawY <= location[1] + view.getHeight();
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            canvas.drawRect(panelRect, backgroundPaint);
            if (rainEnabled) {
                drawPanelRain(canvas);
            }
            canvas.drawRect(panelRect, borderPaint);
            canvas.drawRect(
                    panelRect.left,
                    panelRect.top,
                    panelRect.right,
                    panelRect.top + dp(3),
                    accentPaint
            );
        }

        private void drawPanelRain(Canvas canvas) {
            long now = System.nanoTime();
            float deltaSeconds = lastRainFrameNanos == 0L
                    ? 0f
                    : Math.min(0.034f, (now - lastRainFrameNanos) / 1_000_000_000f);
            lastRainFrameNanos = now;

            for (int i = 0; i < PANEL_DROP_COUNT; i++) {
                rainY[i] += rainSpeed[i] * deltaSeconds;
                rainX[i] -= rainSpeed[i] * deltaSeconds * 0.09f;
                if (rainY[i] - rainLength[i] > getHeight() || rainX[i] < -dp(24)) {
                    resetPanelDrop(i, getWidth(), getHeight(), false);
                }
                rainPaint.setAlpha(rainAlpha[i]);
                canvas.drawLine(
                        rainX[i] + dp(3),
                        rainY[i] - rainLength[i],
                        rainX[i],
                        rainY[i],
                        rainPaint
                );
            }
            postInvalidateOnAnimation();
        }
    }
}
