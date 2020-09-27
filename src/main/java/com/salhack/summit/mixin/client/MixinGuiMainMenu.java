package com.salhack.summit.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import net.minecraft.client.gui.GuiMainMenu;

@Mixin(GuiMainMenu.class)
public class MixinGuiMainMenu extends MixinGuiScreen
{
    // retracted
}
