package technicianlp.reauth.gui;

import com.google.common.collect.ImmutableList;
import cpw.mods.fml.client.config.GuiConfig;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.common.config.ConfigElement;
import technicianlp.reauth.ReAuth;

public final class ConfigGUI extends GuiConfig {

    public ConfigGUI(GuiScreen parent) {
        super(parent, ImmutableList.of(new ConfigElement<>(ReAuth.config.getProfileList().getProfilesCategory())), "reauth", "reauth", false, false, "Config for ReAuth", "");
    }
}
