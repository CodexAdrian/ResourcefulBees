/*
 * This file ("SlotItemHandlerUnconditioned.java") is part of the Actually Additions mod for Minecraft.
 * It is created and owned by Ellpeck and distributed
 * under the Actually Additions License to be found at
 * http://ellpeck.de/actaddlicense
 * View the source code at https://github.com/Ellpeck/ActuallyAdditions
 *
 * © 2015-2017 Ellpeck
 */

package com.teamresourceful.resourcefulbees.container;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;

/**
 * @author Ellpeck
 * Edit By ThatGravyBoat to match offical Mappings
 */
public class SlotItemHandlerUnconditioned extends SlotItemHandler {

  private final AutomationSensitiveItemStackHandler inv;

  public SlotItemHandlerUnconditioned(AutomationSensitiveItemStackHandler h, int index, int xPosition, int yPosition) {
    super(h, index, xPosition, yPosition);
    this.inv = h;
  }

  @Override
  public boolean mayPlace(ItemStack stack) {
    if (!stack.isEmpty() || this.inv.canAccept(this.getSlotIndex(), stack, false)) {
      ItemStack currentStack = this.inv.getStackInSlot(this.getSlotIndex());
      this.inv.setStackInSlot(this.getSlotIndex(), ItemStack.EMPTY);
      ItemStack remainder = this.inv.insertItem(this.getSlotIndex(), stack, true, false);
      this.inv.setStackInSlot(this.getSlotIndex(), currentStack);
      return remainder.isEmpty() || remainder.getCount() < stack.getCount();
    }
    return false;
  }

  /**
   * Helper fnct to get the stack in the slot.
   */
  @Override
  @NotNull
  public ItemStack getItem() {
    return this.inv.getStackInSlot(this.getSlotIndex());
  }

  @Override
  public void set(@NotNull ItemStack stack) {
    this.inv.setStackInSlot(this.getSlotIndex(), stack);
    this.setChanged();
  }

  @Override
  public int getMaxStackSize(ItemStack stack) {
    ItemStack maxAdd = stack.copy();
    maxAdd.setCount(stack.getMaxStackSize());
    ItemStack currentStack = this.inv.getStackInSlot(this.getSlotIndex());
    this.inv.setStackInSlot(this.getSlotIndex(), ItemStack.EMPTY);
    ItemStack remainder = this.inv.insertItem(this.getSlotIndex(), maxAdd, true, false);
    this.inv.setStackInSlot(this.getSlotIndex(), currentStack);
    return stack.getMaxStackSize() - remainder.getCount();
  }

  @Override
  public boolean mayPickup(Player playerIn) {
    return !this.inv.extractItem(this.getSlotIndex(), 1, true, false).isEmpty();
  }

  @NotNull
  @Override
  public ItemStack remove(int amount) {
    return this.inv.extractItem(this.getSlotIndex(), amount, false, false);
  }

  //Adding an additional method for easy access
  public AutomationSensitiveItemStackHandler getInv() {
    return inv;
  }
}
