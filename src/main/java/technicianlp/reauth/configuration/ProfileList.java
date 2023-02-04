package technicianlp.reauth.configuration;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Supplier;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.toml.TomlFormat;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.config.ModConfig;

public final class ProfileList {

    private final Config configuration;
    private final ForgeConfigSpec.ConfigValue<List<CommentedConfig>> profilesProperty;

    private Supplier<CommentedConfig> configSupplier = TomlFormat::newConfig;

    ProfileList(Config configuration, ForgeConfigSpec.Builder builder) {
        this.configuration = configuration;
        this.profilesProperty = builder
                .comment("Saved Profiles. Check Documentation for Info & Syntax")
                .define("profiles", this::createDefaultProfileList, this::validateProfileList);
    }

    final void updateConfig(ModConfig config) {
        this.configSupplier = config.getConfigData()::createSubConfig;

        List<CommentedConfig> list = new ArrayList<>(this.profilesProperty.get());
        this.correctProfiles(list);
        this.saveProfiles(list);
    }

    public final void storeProfile(Profile profile) {
        List<CommentedConfig> list = new ArrayList<>(this.profilesProperty.get());
        if (list.isEmpty()) {
            list.add(profile.getConfig());
        } else {
            list.set(0, profile.getConfig());
        }
        this.saveProfiles(list);
    }

    public final Profile getProfile() {
        List<CommentedConfig> list = this.profilesProperty.get();
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
    private void saveProfiles(List<CommentedConfig> list) {
        if (list.isEmpty()) {
            list.add(this.createPlaceholderConfig());
        }
        this.profilesProperty.set(list);
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
     * checks whether the given Object is a {@link List} and all it's elements are an instance of {@link CommentedConfig}
     * This effectively checks whether the provided {@link Object} is an Array of Tables as described by the TOML specification
     */
    private boolean validateProfileList(Object el) {
        return el instanceof List && ((List<?>) el).stream().allMatch(CommentedConfig.class::isInstance);
    }
}
