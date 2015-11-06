package reauth;

import java.lang.reflect.Field;
import java.util.UUID;

import javax.swing.JLabel;
import javax.swing.JOptionPane;

import org.lwjgl.opengl.Display;

import com.mojang.authlib.Agent;
import com.mojang.authlib.exceptions.AuthenticationException;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import com.mojang.authlib.yggdrasil.YggdrasilUserAuthentication;
import com.mojang.util.UUIDTypeAdapter;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiMultiplayer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.Session;
import net.minecraft.world.World;
import net.minecraftforge.client.event.GuiScreenEvent.ActionPerformedEvent;
import net.minecraftforge.client.event.GuiScreenEvent.InitGuiEvent;
import net.minecraftforge.common.MinecraftForge;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.common.network.IGuiHandler;

public class GuiHandler {

	@SubscribeEvent
	public void ongui(InitGuiEvent.Post e) {
		if (e.gui instanceof GuiMultiplayer)
			e.buttonList.add(new GuiButton(17325, 5, 5, 100, 20, "Re-Login"));
	}

	@SubscribeEvent
	public void ongui(ActionPerformedEvent.Post e) {
		if (e.gui instanceof GuiMultiplayer && e.button.id == 17325) {
			Minecraft.getMinecraft().displayGuiScreen(new GuiLogin(Minecraft.getMinecraft().currentScreen));
		}
	}
}
