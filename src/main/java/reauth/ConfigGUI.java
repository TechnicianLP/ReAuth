package reauth;

import com.google.common.collect.ImmutableList;
import cpw.mods.fml.client.config.GuiConfig;
import cpw.mods.fml.client.config.IConfigElement;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.common.config.Configuration;

class ConfigGUI extends GuiConfig {

    private static IConfigElement ce = new ConfigElement(Main.config.getCategory(Configuration.CATEGORY_GENERAL));

    ConfigGUI(GuiScreen parent) {
        super(parent, ImmutableList.of(ce), "reauth", "reauth", false, false, "Config for ReAuth", "");
    }

}
