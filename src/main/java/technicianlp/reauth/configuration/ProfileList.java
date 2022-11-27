package technicianlp.reauth.configuration;

import com.google.common.collect.Iterables;
import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public final class ProfileList {

    private static final String PROFILES_CATEGORY = "profiles" + Configuration.CATEGORY_SPLITTER;

    private final Config config;

    private ConfigCategory profilesCategory = null;

    ProfileList(Config config) {
        this.config = config;
    }

    final void loadConfig(ConfigCategory profilesCategory) {
        this.profilesCategory = profilesCategory;

        if (!profilesCategory.isEmpty()) {
            profilesCategory.clear();
        }

        profilesCategory.getChildren().forEach(this::correctProfile);
        this.config.save();
    }

    public final void storeProfile(Profile profile) {
        if (profile.isLoaded())
            return;

        Configuration configuration = this.config.getConfig();

        ConfigCategory oldCategory = Iterables.getFirst(this.profilesCategory.getChildren(), null);
        if (oldCategory != null) {
            configuration.removeCategory(oldCategory);
        }

        ConfigCategory category = configuration.getCategory(this.findNewProfileName());
        Map<String, String> profileData = profile.getConfig();
        profileData.forEach((key, value) -> configuration.get(category.getQualifiedName(), key, "").set(value));
        category.setPropertyOrder(ProfileConstants.getOrderedProfileKeys());

        this.saveProfiles();
    }

    public final Profile getProfile() {
        ConfigCategory category = Iterables.getFirst(this.profilesCategory.getChildren(), null);
        if (category == null)
            return null;

        Map<String, String> values = new HashMap<>();
        category.forEach((k, v) -> values.put(k, v.getString()));

        String profileType = values.getOrDefault(ProfileConstants.PROFILE_TYPE, ProfileConstants.PROFILE_TYPE_NONE);
        if (!ProfileConstants.PROFILE_TYPE_NONE.equals(profileType)) {
            return new Profile(values, true);
        } else {
            return null;
        }
    }

    public final ConfigCategory getProfilesCategory() {
        return this.profilesCategory;
    }

    public final Profile createProfile(Map<String, String> data) {
        return new Profile(data, false);
    }

    private void correctProfile(ConfigCategory profile) {
        List<Property> newProps = new ArrayList<>(0);
        for (Iterator<Property> iterator = profile.values().iterator(); iterator.hasNext(); ) {
            Property entry = iterator.next();
            if (entry.getType() != Property.Type.STRING || entry.isList()) {
                iterator.remove();
                String value = entry.isList() ? Arrays.toString(entry.getStringList()) : entry.getString();
                Property property = new Property(entry.getName(), value, Property.Type.STRING);
                property.setDefaultValue("");
                newProps.add(property);
            } else {
                entry.setDefaultValue("");
            }
        }
        newProps.forEach(entry -> profile.put(entry.getName(), entry));

        profile.setPropertyOrder(ProfileConstants.getOrderedProfileKeys());
    }

    /**
     * Save the list of Profiles to config
     * Add a dummy profile if the list is empty
     */
    private void saveProfiles() {
        this.config.save();
    }

    private String findNewProfileName() {
        Configuration configuration = this.config.getConfig();
        int i = 1;
        String nameBase = PROFILES_CATEGORY + "profile";
        while (configuration.hasCategory(nameBase + i)) {
            i = i + 1;
        }
        return nameBase + i;
    }
}
