package space.myhomework.android;

import android.graphics.Color;

public class PrefixManager {
    public static PrefixInfo getPrefixInfo(String input) {
        String prefix = input.split(" ")[0].toLowerCase();

        // todo: sync with main website
        if (prefix.equals("hw") || prefix.equals("read") || prefix.equals("reading")) {
            return new PrefixInfo(Color.parseColor("#4c6c9b"), Color.WHITE);
        } else if (prefix.equals("project")) {
            return new PrefixInfo(Color.parseColor("#9ACD32"), Color.WHITE);
        } else if (prefix.equals("report") || prefix.equals("essay") || prefix.equals("paper")) {
            return new PrefixInfo(Color.parseColor("#FFD700"), Color.WHITE);
        } else if (prefix.equals("quiz") || prefix.equals("popquiz")) {
            return new PrefixInfo(Color.parseColor("#ffa500"), Color.WHITE);
        } else if (prefix.equals("test") || prefix.equals("final") || prefix.equals("exam") || prefix.equals("midterm")) {
            return new PrefixInfo(Color.parseColor("#DC143C"), Color.WHITE);
        } else if (prefix.equals("ica")) {
            return new PrefixInfo(Color.parseColor("#2ac0f1"), Color.WHITE);
        } else if (prefix.equals("lab") || prefix.equals("study") || prefix.equals("memorize")) {
            return new PrefixInfo(Color.parseColor("#2af15e"), Color.WHITE);
        } else if (prefix.equals("docid")) {
            return new PrefixInfo(Color.parseColor("#003DAD"), Color.WHITE);
        } else if (prefix.equals("trojun") || prefix.equals("hex")) {
            return new PrefixInfo(Color.BLACK, Color.parseColor("#00FF00"));
        } else if (prefix.equals("optionalhw") || prefix.equals("challenge")) {
            return new PrefixInfo(Color.parseColor("#5000BC"), Color.WHITE);
        } else if (prefix.equals("presentation") || prefix.equals("prez")) {
            return new PrefixInfo(Color.parseColor("#000099"), Color.WHITE);
        } else {
            return new PrefixInfo(Color.parseColor("#FFD3BD"), Color.BLACK);
        }
    }
}
