package reauth;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.google.common.collect.ImmutableList;

import cpw.mods.fml.client.IModGuiFactory;
import cpw.mods.fml.client.config.GuiConfig;
import cpw.mods.fml.client.config.IConfigElement;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.common.config.ConfigElement;

public class ConfigGUI extends GuiConfig {

	private static IConfigElement ce = new ConfigElement(Main.config.getCategory(Main.config.CATEGORY_GENERAL));

	public ConfigGUI(GuiScreen parent) {
		super(parent, ImmutableList.of(ce), "ReAuth", false, false, "Config for ReAuth");
	}

}
