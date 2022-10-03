package com.teamresourceful.resourcefulbees.client.components.centrifuge.controlpanels;

import com.mojang.blaze3d.vertex.PoseStack;
import com.teamresourceful.resourcefulbees.common.lib.enums.ControlPanelTabs;
import com.teamresourceful.resourcefulbees.client.screens.centrifuge.CentrifugeTextures;
import com.teamresourceful.resourcefulbees.client.screens.centrifuge.CentrifugeTerminalScreen;
import com.teamresourceful.resourcefulbees.client.utils.ClientUtils;
import com.teamresourceful.resourcefulbees.client.utils.TextUtils;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public class TerminalControlPanel extends AbstractControlPanel {

    public TerminalControlPanel(int x, int y, CentrifugeTerminalScreen terminalScreen) {
        super(x, y, terminalScreen);
    }

    @Override
    protected void init() {
        createControlPanelDisplayTab(y+18, ControlPanelTabs.HOME);
        createControlPanelDisplayTab(y+32, ControlPanelTabs.INPUTS/*, new TerminalInputNavPanel(x, y+101, screen)*/);
        createControlPanelDisplayTab(y+46, ControlPanelTabs.ITEM_OUTPUTS);
        createControlPanelDisplayTab(y+60, ControlPanelTabs.FLUID_OUTPUTS);
        createControlPanelDisplayTab(y+74, ControlPanelTabs.DUMPS);
    }

    @Override
    public void render(@NotNull PoseStack stack, int mouseX, int mouseY, float partialTicks) {
        ClientUtils.bindTexture(CentrifugeTextures.COMPONENTS);
        blit(stack, x, y, 75, 165, 75, 91);
        TextUtils.tf12DrawCenteredStringNoShadow(stack, Component.literal("Terminal"), x+37f, y+6.5f, TextUtils.FONT_COLOR_1);
        super.render(stack, mouseX, mouseY, partialTicks);
    }
}
