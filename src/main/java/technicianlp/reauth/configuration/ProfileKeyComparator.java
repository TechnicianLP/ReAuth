package technicianlp.reauth.configuration;

import java.util.Comparator;

/**
 * Used to provide consistent ordering for config entries.
 * Note: this comparator imposes orderings that are inconsistent with equals.
 */
final class ProfileKeyComparator implements Comparator<String> {

    @Override
    public int compare(String o1, String o2) {
        return Integer.compare(this.getIndex(o1), this.getIndex(o2));
    }

    private int getIndex(String key) {
        if (key != null) {
            switch (key) {
                case Profile.PROFILE_TYPE:
                    return 0;
                case Profile.NAME:
                    return 1;
                case Profile.UUID:
                    return 2;
                case Profile.USERNAME:
                case Profile.XBL_TOKEN:
                    return 3;
                case Profile.PASSWORD:
                case Profile.REFRESH_TOKEN:
                    return 4;
                case Profile.KEY:
                    return 5;
                case Profile.SALT:
                    return 6;
            }
        }
        return 100;
    }
}
