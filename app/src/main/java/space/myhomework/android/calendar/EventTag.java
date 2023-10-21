package space.myhomework.android.calendar;

public enum EventTag {
    RESERVED,
    DESCRIPTION,
    HOMEWORK,
    TERM_ID,
    CLASS_ID,
    OWNER_ID,
    OWNER_NAME,
    DAY_NUMBER,
    BLOCK,
    BUILDING_NAME,
    ROOM_NUMBER,
    LOCATION,
    READ_ONLY,
    SHORT_NAME,
    ACTIONS,
    CANCELLED,
    CANCELABLE,
    SECTION,
    ORIGINAL_START,
    ORIGINAL_END,
    HIDE_BUILDING_NAME,
    HOMEWORK_CLASS,
    INSTANCE_START,
    INSTANCE_END,
    IS_CONTINUATION,
    CONTINUES;

    private static EventTag[] values = EventTag.values();

    public static EventTag fromInteger(int x) {
        return values[x];
    }
}
