package gradeflow.ui;

// All colour and style constants in one place so we can change the look easily
public final class Styles {

    private Styles() {}

    public static final String PRIMARY      = "#3B82F6";
    public static final String PRIMARY_DARK = "#1D4ED8";
    public static final String BG           = "#F1F5F9";
    public static final String SURFACE      = "#FFFFFF";
    public static final String SIDEBAR_BG   = "#1E293B";
    public static final String SIDEBAR_HOVER= "#334155";
    public static final String TEXT         = "#1E293B";
    public static final String TEXT_MUTED   = "#64748B";
    public static final String BORDER       = "#E2E8F0";

    public static final String BTN_PRIMARY =
        "-fx-background-color:" + PRIMARY + ";-fx-text-fill:white;-fx-font-size:13px;" +
        "-fx-font-weight:bold;-fx-padding:9 20 9 20;-fx-background-radius:7;-fx-cursor:hand;";

    public static final String BTN_PRIMARY_HOVER =
        "-fx-background-color:" + PRIMARY_DARK + ";-fx-text-fill:white;-fx-font-size:13px;" +
        "-fx-font-weight:bold;-fx-padding:9 20 9 20;-fx-background-radius:7;-fx-cursor:hand;";

    public static final String BTN_SECONDARY =
        "-fx-background-color:" + SURFACE + ";-fx-text-fill:" + TEXT + ";-fx-font-size:12px;" +
        "-fx-padding:7 14 7 14;-fx-background-radius:7;-fx-border-color:" + BORDER + ";" +
        "-fx-border-radius:7;-fx-cursor:hand;";

    public static final String BTN_SIDEBAR =
        "-fx-background-color:transparent;-fx-text-fill:#CBD5E1;-fx-font-size:13px;" +
        "-fx-alignment:CENTER_LEFT;-fx-padding:10 16 10 16;-fx-cursor:hand;-fx-min-width:200;";

    public static final String BTN_SIDEBAR_HOVER =
        "-fx-background-color:" + SIDEBAR_HOVER + ";-fx-text-fill:white;-fx-font-size:13px;" +
        "-fx-alignment:CENTER_LEFT;-fx-padding:10 16 10 16;-fx-cursor:hand;-fx-min-width:200;" +
        "-fx-background-radius:6;";

    public static final String TEXT_FIELD =
        "-fx-background-color:" + SURFACE + ";-fx-border-color:" + BORDER + ";" +
        "-fx-border-radius:6;-fx-background-radius:6;-fx-padding:7 10 7 10;" +
        "-fx-font-size:13px;-fx-text-fill:" + TEXT + ";";

    public static final String TEXT_AREA =
        "-fx-background-color:" + SURFACE + ";-fx-border-color:" + BORDER + ";" +
        "-fx-border-radius:6;-fx-background-radius:6;-fx-font-size:12px;" +
        "-fx-text-fill:" + TEXT + ";";

    public static final String RADIO_BUTTON = "-fx-text-fill:" + TEXT + ";-fx-font-size:13px;";

    public static final String COMBO_BOX    = "-fx-font-size:13px;-fx-text-fill:" + TEXT + ";";

    public static final String LABEL_MUTED  = "-fx-text-fill:" + TEXT_MUTED + ";-fx-font-size:12px;";
    public static final String LABEL_BOLD   = "-fx-text-fill:" + TEXT + ";-fx-font-size:14px;-fx-font-weight:bold;";

    public static final String BADGE_PASS =
        "-fx-background-color:#DCFCE7;-fx-text-fill:#166534;" +
        "-fx-background-radius:12;-fx-padding:3 10 3 10;-fx-font-size:11px;-fx-font-weight:bold;";
    public static final String BADGE_FAIL =
        "-fx-background-color:#FEE2E2;-fx-text-fill:#991B1B;" +
        "-fx-background-radius:12;-fx-padding:3 10 3 10;-fx-font-size:11px;-fx-font-weight:bold;";
    public static final String BADGE_WARN =
        "-fx-background-color:#FEF3C7;-fx-text-fill:#92400E;" +
        "-fx-background-radius:12;-fx-padding:3 10 3 10;-fx-font-size:11px;-fx-font-weight:bold;";
    public static final String BADGE_GREY =
        "-fx-background-color:#F1F5F9;-fx-text-fill:#475569;" +
        "-fx-background-radius:12;-fx-padding:3 10 3 10;-fx-font-size:11px;-fx-font-weight:bold;";

    public static String badgeFor(String status) {
        return switch (status) {
            case "PASS"                    -> BADGE_PASS;
            case "FAIL"                    -> BADGE_FAIL;
            case "COMPILE_ERROR","RUNTIME_ERROR","ZIP_ERROR","TIMEOUT","MISSING_FILE"
                                           -> BADGE_WARN;
            default                        -> BADGE_GREY;
        };
    }
}
