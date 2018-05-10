package technicianlp.reauth;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.config.GuiConfig;
import net.minecraftforge.fml.client.config.IConfigElement;

final class ConfigGUI extends GuiConfig {

    private static IConfigElement ce = new ConfigElement(Main.config.getCategory(Configuration.CATEGORY_GENERAL));

    ConfigGUI(GuiScreen parent) {
        super(parent, ImmutableList.of(ce), "reauth", "reauth", false, false, "Config for ReAuth", "");
    }

}
