package reauth;

import com.google.common.collect.ImmutableList;

import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.config.GuiConfig;
import net.minecraftforge.fml.client.config.IConfigElement;

public class ConfigGUI extends GuiConfig {

	private static IConfigElement ce = new ConfigElement(Main.config.getCategory(Configuration.CATEGORY_GENERAL));

	public ConfigGUI(GuiScreen parent) {
		super(parent, ImmutableList.of(ce), "ReAuth", false, false, "Config for ReAuth");
	}

}
