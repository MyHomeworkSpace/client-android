package space.myhomework.android.api;

public enum APIRecurFrequency {
    DAILY,
    WEEKLY,
    MONTHLY,
    YEARLY;

    private static APIRecurFrequency[] values = APIRecurFrequency.values();

    public static APIRecurFrequency fromInteger(int x) {
        return values[x];
    }
}
