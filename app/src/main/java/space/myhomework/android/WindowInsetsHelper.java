package space.myhomework.android;

import android.app.Activity;
import android.view.View;

import androidx.core.graphics.Insets;
import androidx.core.view.OnApplyWindowInsetsListener;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

/**
 * Applies the system bar insets that AppCompat reports, instead of relying on it to offset our
 * content for us.
 *
 * Since appcompat 1.7, ActionBarOverlayLayout stops giving the content view a top margin whenever
 * the decor does not fit system windows -- which the framework forces for every activity at
 * targetSdk 35 on Android 15 and up. It folds the action bar's height into the top system window
 * inset it dispatches down, so the top value read here is already status bar + action bar. When the
 * decor does fit system windows, AppCompat has already done the offsetting and the insets arrive
 * here as zero, so this becomes a no-op.
 *
 * The display cutout is included alongside the system bars, since at targetSdk 35 the window
 * extends into it and a landscape hole-punch camera shows up as a left or right inset.
 */
public final class WindowInsetsHelper {
    private WindowInsetsHelper() {}

    private static final int TYPES =
            WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.displayCutout();

    /** Pads {@code view} by the insets on whichever edges are requested. */
    public static void applyTo(View view, final boolean left, final boolean top,
                               final boolean right, final boolean bottom) {
        ViewCompat.setOnApplyWindowInsetsListener(view, new OnApplyWindowInsetsListener() {
            @Override
            public WindowInsetsCompat onApplyWindowInsets(View v, WindowInsetsCompat insets) {
                Insets bars = insets.getInsets(TYPES);
                v.setPadding(
                        left ? bars.left : 0,
                        top ? bars.top : 0,
                        right ? bars.right : 0,
                        bottom ? bars.bottom : 0);
                // Deliberately not consumed, so children that inset themselves still see them.
                return insets;
            }
        });
    }

    /**
     * Pads an activity's content root on every edge.
     *
     * @param applyBottom whether to pad for the bottom system bar. Pass false when a child view
     *                    already handles it.
     */
    public static void applyToContent(Activity activity, boolean applyBottom) {
        applyTo(activity.findViewById(android.R.id.content), true, true, true, applyBottom);
    }
}
