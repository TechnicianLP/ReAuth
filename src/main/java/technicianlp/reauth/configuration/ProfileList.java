package technicianlp.reauth.configuration;

import com.electronwill.nightconfig.core.CommentedConfig;

import java.util.*;
import java.util.function.Supplier;

public final class ProfileList {
    public static final String PROFILES_PATH = "profiles";

    private final Config configuration;
    private final List<CommentedConfig> profilesProperty;

    private final Supplier<CommentedConfig> configSupplier;

    ProfileList(Config configuration, CommentedConfig config) {
        this.configuration = configuration;
        this.configSupplier = config::createSubConfig;
        this.profilesProperty = config.getOrElse(PROFILES_PATH,
            () -> config.set(PROFILES_PATH, new ArrayList<>()));
        config.setComment(PROFILES_PATH, "Saved Profiles. Check Documentation for Info & Syntax");
        correctProfiles(this.profilesProperty);
        this.saveProfiles();
    }

    public void storeProfile(Profile profile) {
        List<CommentedConfig> list = this.profilesProperty;
        if (list.isEmpty()) {
            list.add(profile.getConfig());
        } else { // TODO check this logic
            list.set(0, profile.getConfig());
        }
        this.saveProfiles();
    }

    public Profile getProfile() {
        List<CommentedConfig> list = this.profilesProperty;
        if (list.isEmpty()) {
            return null;
        }
        CommentedConfig config = list.get(0);
        String profileType = config.getOrElse(ProfileConstants.PROFILE_TYPE, ProfileConstants.PROFILE_TYPE_NONE);
        if (ProfileConstants.PROFILE_TYPE_NONE.equals(profileType)) {
            return null;
        } else {
            return new Profile(config);
        }
    }

    Profile createProfile(Map<String, String> data) {
        Map<String, String> orderedData = new TreeMap<>(ProfileConstants::compareProfileKeys);
        orderedData.putAll(data);

        CommentedConfig config = this.configSupplier.get();
        orderedData.forEach(config::set);

        return new Profile(config);
    }

    private static void correctProfiles(Collection<CommentedConfig> profileList) {
        for (CommentedConfig profile : profileList) {
            Iterator<? extends CommentedConfig.Entry> iterator;
            for (iterator = profile.entrySet().iterator(); iterator.hasNext(); ) {
                CommentedConfig.Entry entry = iterator.next();
                Object value = entry.getValue();
                if (value == null) {
                    iterator.remove();
                } else if (!(value instanceof String)) {
                    entry.setValue(value.toString());
                }
            }
        }
        profileList.removeIf(CommentedConfig::isEmpty);
    }

    /**
     * Save the list of Profiles to config Add a dummy profile if the list is empty
     */
    private void saveProfiles() {
        List<CommentedConfig> list = this.profilesProperty;
        if (list.isEmpty()) {
            list.add(this.createPlaceholderConfig());
        }
        this.configuration.save();
    }

    private CommentedConfig createPlaceholderConfig() {
        CommentedConfig config = this.configSupplier.get();
        config.set(ProfileConstants.PROFILE_TYPE, ProfileConstants.PROFILE_TYPE_NONE);
        return config;
    }

}
