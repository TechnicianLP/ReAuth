package technicianlp.reauth.configuration;

import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.List;

public final class ProfileConstants {

    // profile type
    public static final String PROFILE_TYPE = "type";
    public static final String PROFILE_TYPE_NONE = "none";
    public static final String PROFILE_TYPE_MOJANG = "mojang";
    public static final String PROFILE_TYPE_MICROSOFT = "microsoft";

    // common fields
    public static final String NAME = "name";
    public static final String UUID = "uuid";

    // mojang profiles
    public static final String USERNAME = "username";
    public static final String PASSWORD = "password";

    // microsoft profiles
    public static final String REFRESH_TOKEN = "refresh-token";
    public static final String XBL_TOKEN = "xbl-token";

    // encryption
    public static final String KEY = "key";
    public static final String KEY_AUTO = "auto";
    public static final String KEY_NONE = "none";
    public static final String SALT = "salt";

    /**
     * Used to provide consistent ordering for config entries.
     */
    private static final List<String> propertyOrder = ImmutableList.of(
            ProfileConstants.PROFILE_TYPE,
            ProfileConstants.NAME,
            ProfileConstants.UUID,
            ProfileConstants.USERNAME,
            ProfileConstants.XBL_TOKEN,
            ProfileConstants.PASSWORD,
            ProfileConstants.REFRESH_TOKEN,
            ProfileConstants.KEY,
            ProfileConstants.SALT);

    static List<String> getOrderedProfileKeys() {
        return new ArrayList<>(propertyOrder);
    }

    static int compareProfileKeys(String key1, String key2) {
        if (key1.equals(key2)) {
            return 0;
        } else {
            if (propertyOrder.contains(key1)) {
                if (propertyOrder.contains(key2)) {
                    return Integer.compare(propertyOrder.indexOf(key1), propertyOrder.indexOf(key2));
                } else {
                    return -1;
                }
            } else {
                if (propertyOrder.contains(key2)) {
                    return 1;
                } else {
                    return key1.compareTo(key2);
                }
            }
        }
    }
}
