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
        this.correctProfiles(this.profilesProperty);
        this.saveProfiles();
    }

    public final void storeProfile(Profile profile) {
        List<CommentedConfig> list = this.profilesProperty;
        if (list.isEmpty()) {
            list.add(profile.getConfig());
        } else { // TODO check this logic
            list.set(0, profile.getConfig());
        }
        this.saveProfiles();
    }

    public final Profile getProfile() {
        List<CommentedConfig> list = this.profilesProperty;
        if (list.isEmpty()) {
            return null;
        }
        CommentedConfig config = list.get(0);
        String profileType = config.getOrElse(ProfileConstants.PROFILE_TYPE, ProfileConstants.PROFILE_TYPE_NONE);
        if (!ProfileConstants.PROFILE_TYPE_NONE.equals(profileType)) {
            return new Profile(config);
        } else {
            return null;
        }
    }

    final Profile createProfile(Map<String, String> data) {
        Map<String, String> orderedData = new TreeMap<>(ProfileConstants::compareProfileKeys);
        orderedData.putAll(data);

        CommentedConfig config = this.configSupplier.get();
        orderedData.forEach(config::set);

        return new Profile(config);
    }

    private void correctProfiles(List<CommentedConfig> profileList) {
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
     * Save the list of Profiles to config
     * Add a dummy profile if the list is empty
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


    private List<CommentedConfig> createDefaultProfileList() {
        List<CommentedConfig> list = new ArrayList<>();
        list.add(this.createPlaceholderConfig());
        return list;
    }

    /**
     * checks whether the given Object is a {@link List} and all it's elements are an instance of
     * {@link CommentedConfig}
     * This effectively checks whether the provided {@link Object} is an Array of Tables as described by the TOML
     * specification
     */
    private boolean validateProfileList(Object el) {
        return el instanceof List && ((List<?>) el).stream().allMatch(CommentedConfig.class::isInstance);
    }
}
