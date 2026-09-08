package space.myhomework.android;

import android.app.Activity;
import android.view.View;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

/**
 * Applies the system bar insets that AppCompat reports, instead of relying on it to offset our
 * content for us.
 *
 * Since appcompat 1.7, ActionBarOverlayLayout stops giving the content view a top margin whenever
 * the decor does not fit system windows -- which the framework forces for every activity at
 * targetSdk 35 on Android 15 and up. It folds the action bar's height into the top system window
 * inset it dispatches down instead, so the value read here is already status bar + action bar.
 * When the decor does fit system windows, AppCompat has already done the offsetting and the insets
 * arrive here as zero, so this is a no-op.
 */
public final class WindowInsetsHelper {
    private WindowInsetsHelper() {}

    /**
     * @param applyBottom whether to pad for the bottom system bar. Pass false when a child view
     *                    already handles it (BottomNavigationView pads itself).
     */
    public static void applyToContent(Activity activity, final boolean applyBottom) {
        View content = activity.findViewById(android.R.id.content);
        ViewCompat.setOnApplyWindowInsetsListener(content, new androidx.core.view.OnApplyWindowInsetsListener() {
            @Override
            public WindowInsetsCompat onApplyWindowInsets(View v, WindowInsetsCompat insets) {
                Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(0, bars.top, 0, applyBottom ? bars.bottom : 0);
                // Deliberately not consumed, so children that inset themselves still see them.
                return insets;
            }
        });
    }
}
