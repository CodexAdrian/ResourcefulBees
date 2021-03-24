package com.resourcefulbees.resourcefulbees.tileentity;

import com.google.common.collect.Lists;
import com.resourcefulbees.resourcefulbees.config.Config;
import com.resourcefulbees.resourcefulbees.container.AutomationSensitiveItemStackHandler;
import com.resourcefulbees.resourcefulbees.container.EnderBeeconContainer;
import com.resourcefulbees.resourcefulbees.registry.ModEffects;
import com.resourcefulbees.resourcefulbees.entity.passive.CustomBeeEntity;
import com.resourcefulbees.resourcefulbees.item.CustomHoneyBottleItem;
import com.resourcefulbees.resourcefulbees.lib.ModConstants;
import com.resourcefulbees.resourcefulbees.lib.NBTConstants;
import com.resourcefulbees.resourcefulbees.network.NetPacketHandler;
import com.resourcefulbees.resourcefulbees.network.packets.SyncGUIMessage;
import com.resourcefulbees.resourcefulbees.network.packets.UpdateClientBeeconMessage;
import com.resourcefulbees.resourcefulbees.registry.ModFluids;
import com.resourcefulbees.resourcefulbees.registry.ModItems;
import com.resourcefulbees.resourcefulbees.utils.BeeInfoUtils;
import io.netty.buffer.Unpooled;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.passive.BeeEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.IContainerListener;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.BucketItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.tags.ITag;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.gen.Heightmap;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;

import com.resourcefulbees.resourcefulbees.container.AutomationSensitiveItemStackHandler.IAcceptor;
import com.resourcefulbees.resourcefulbees.container.AutomationSensitiveItemStackHandler.IRemover;
import com.resourcefulbees.resourcefulbees.tileentity.HoneyTankTileEntity.TankTier;

public class EnderBeeconTileEntity extends HoneyTankTileEntity implements ITickableTileEntity, INamedContainerProvider {
    //TODO see about trimming the duplicate code if possible - epic

    public static final ITag<Fluid> HONEY_FLUID_TAG = BeeInfoUtils.getFluidTag("forge:honey"); //DUPLICATE
    public static final ITag<Item> HONEY_BOTTLE_TAG = BeeInfoUtils.getItemTag("forge:honey_bottle"); //DUPLICATE
    private List<BeamSegment> beamSegments = Lists.newArrayList();
    private List<BeamSegment> beams = Lists.newArrayList();
    private int worldHeight = -1;
    private final float[] afloat = {255f, 255f, 255f};
    private boolean updateBeecon = true;
    private boolean beeconActive = false;
    private boolean playSound = true;
    private boolean showBeam = true;
    public static final int HONEY_BOTTLE_INPUT = 0;
    public static final int BOTTLE_OUTPUT = 1;
    public static final int HONEY_FILL_AMOUNT = ModConstants.HONEY_PER_BOTTLE;
    private static final int FLUID_PULL_RATE = Config.BEECON_PULL_AMOUNT.get();
    private final AutomationSensitiveItemStackHandler tileStackHandler = new EnderBeeconTileEntity.TileStackHandler(2, getAcceptor(), getRemover());
    private final LazyOptional<IItemHandler> lazyOptional = LazyOptional.of(this::getTileStackHandler);
    private boolean dirty;

    private List<BeeconEffect> effects;

    public EnderBeeconTileEntity(TileEntityType<?> tileEntityType) {
        super(tileEntityType, TankTier.NETHER);
        setFluidTank(new InternalFluidTank(getTier().maxFillAmount, honeyFluidPredicate()));
        effects = readEffectsFromNBT(new CompoundNBT());
    }

    @Nonnull
    @Override
    public ITextComponent getDisplayName() {
        return new TranslationTextComponent("gui.resourcefulbees.ender_beecon");
    }

    @Nullable
    @Override
    public Container createMenu(int id, @NotNull PlayerInventory playerInventory, @NotNull PlayerEntity playerEntity) {
        assert level != null;
        return new EnderBeeconContainer(id, level, worldPosition, playerInventory);
    }

    @Override
    public CompoundNBT writeNBT(CompoundNBT tag) {
        tag.putInt("tier", TankTier.NETHER.getTier());
        tag.putBoolean(NBTConstants.NBT_SHOW_BEAM, isShowBeam());
        tag.putBoolean(NBTConstants.NBT_PLAY_SOUND, playSound);
        if (effects != null && !effects.isEmpty()) {
            tag.put("active_effects", writeEffectsToNBT(new CompoundNBT()));
        }
        if (!getFluidTank().isEmpty()) {
            tag.put(NBTConstants.NBT_FLUID, getFluidTank().writeToNBT(new CompoundNBT()));
        }
        return tag;
    }

    @Override
    public void readNBT(CompoundNBT tag) {
        getFluidTank().readFromNBT(tag.getCompound(NBTConstants.NBT_FLUID));
        effects = readEffectsFromNBT(tag.getCompound("active_effects"));
        if (tag.contains(NBTConstants.NBT_SHOW_BEAM)) setShowBeam(tag.getBoolean(NBTConstants.NBT_SHOW_BEAM));
        if (tag.contains(NBTConstants.NBT_PLAY_SOUND)) playSound = tag.getBoolean(NBTConstants.NBT_PLAY_SOUND);
        setTier(TankTier.NETHER);
        if (getFluidTank().getTankCapacity(0) != getTier().maxFillAmount) getFluidTank().setCapacity(getTier().maxFillAmount);
        if (getFluidTank().getFluidAmount() > getFluidTank().getTankCapacity(0))
            getFluidTank().getFluid().setAmount(getFluidTank().getTankCapacity(0));
    }

    public AutomationSensitiveItemStackHandler.IAcceptor getAcceptor() {
        return (slot, stack, automation) -> !automation || slot == 0;
    }

    public AutomationSensitiveItemStackHandler.IRemover getRemover() {
        return (slot, automation) -> !automation || slot == 1;
    }

    private boolean canStartFluidProcess() {
        ItemStack stack = getTileStackHandler().getStackInSlot(HONEY_BOTTLE_INPUT);
        ItemStack output = getTileStackHandler().getStackInSlot(BOTTLE_OUTPUT);

        boolean stackValid = false;
        boolean isBucket = false;
        boolean outputValid;
        boolean hasRoom;
        if (!stack.isEmpty()) {
            if (stack.getItem() instanceof BucketItem) {
                BucketItem bucket = (BucketItem) stack.getItem();
                stackValid = bucket.getFluid().is(HONEY_FLUID_TAG);
                isBucket = true;
            } else {
                stackValid = stack.getItem().is(HONEY_BOTTLE_TAG);
            }
        }
        if (!output.isEmpty()) {
            if (isBucket) {
                outputValid = output.getItem() == Items.BUCKET;
            } else {
                outputValid = output.getItem() == Items.GLASS_BOTTLE;
            }
            hasRoom = output.getCount() < output.getMaxStackSize();
        } else {
            outputValid = true;
            hasRoom = true;
        }
        return stackValid && outputValid && hasRoom;
    }

    private void processFluid() {
        if (canProcessFluid()) {
            ItemStack stack = getTileStackHandler().getStackInSlot(HONEY_BOTTLE_INPUT);
            ItemStack output = getTileStackHandler().getStackInSlot(BOTTLE_OUTPUT);
            if (stack.getItem() instanceof BucketItem) {
                BucketItem bucket = (BucketItem) stack.getItem();
                getFluidTank().fill(new FluidStack(bucket.getFluid(), 1000), IFluidHandler.FluidAction.EXECUTE);
                stack.shrink(1);
                if (output.isEmpty()) {
                    getTileStackHandler().setStackInSlot(BOTTLE_OUTPUT, new ItemStack(Items.BUCKET));
                } else {
                    output.grow(1);
                }
            } else {
                if (stack.getItem() instanceof CustomHoneyBottleItem) {
                    CustomHoneyBottleItem item = (CustomHoneyBottleItem) stack.getItem();
                    FluidStack fluid = new FluidStack(item.getHoneyData().getHoneyStillFluidRegistryObject().get().getSource(), HONEY_FILL_AMOUNT);
                    getFluidTank().fill(fluid, IFluidHandler.FluidAction.EXECUTE);
                } else if (stack.getItem() == ModItems.CATNIP_HONEY_BOTTLE.get()) {
                    FluidStack fluid = new FluidStack(ModFluids.CATNIP_HONEY_STILL.get(), HONEY_FILL_AMOUNT);
                    getFluidTank().fill(fluid, IFluidHandler.FluidAction.EXECUTE);
                }
                stack.shrink(1);
                if (output.isEmpty()) {
                    getTileStackHandler().setStackInSlot(BOTTLE_OUTPUT, new ItemStack(Items.GLASS_BOTTLE));
                } else {
                    output.grow(1);
                }
            }
            this.dirty = true;
        }
    }

    private boolean canProcessFluid() {
        boolean spaceLeft;
        ItemStack stack = getTileStackHandler().getStackInSlot(HONEY_BOTTLE_INPUT);
        Fluid fluid = ModFluids.HONEY_STILL.get();
        if (stack.getItem() instanceof BucketItem) {
            BucketItem item = (BucketItem) stack.getItem();
            spaceLeft = (getFluidTank().getFluidAmount() + 1000) <= getFluidTank().getCapacity();
            fluid = item.getFluid();
        } else {
            spaceLeft = (getFluidTank().getFluidAmount() + HONEY_FILL_AMOUNT) <= getFluidTank().getCapacity();
            if (stack.getItem() instanceof CustomHoneyBottleItem) {
                CustomHoneyBottleItem item = (CustomHoneyBottleItem) stack.getItem();
                fluid = item.getHoneyData().getHoneyStillFluidRegistryObject().get().getSource();
            } else if (stack.getItem() == ModItems.CATNIP_HONEY_BOTTLE.get()) {
                fluid = ModFluids.CATNIP_HONEY_STILL.get();
            }
        }
        return spaceLeft && (getFluidTank().getFluid().getFluid() == fluid || getFluidTank().isEmpty());
    }


    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @javax.annotation.Nullable Direction side) {
        if (cap.equals(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)) return lazyOptional.cast();
        return super.getCapability(cap, side);
    }

    @Override
    protected void invalidateCaps() {
        super.invalidateCaps();
        this.lazyOptional.invalidate();
    }

    @Override
    public void tick() {
        int i = this.worldPosition.getX();
        int j = this.worldPosition.getY();
        int k = this.worldPosition.getZ();
        BlockPos blockpos;
        if (this.worldHeight < j) {
            blockpos = this.worldPosition;
            this.beams = Lists.newArrayList();
            this.worldHeight = blockpos.getY() - 1;
        } else {
            blockpos = new BlockPos(i, this.worldHeight + 1, k);
        }
        if (doEffects()) {
            getFluidTank().drain(getDrain(), IFluidHandler.FluidAction.EXECUTE);
        }

        BeamSegment segment = this.beams.isEmpty() ? null : this.beams.get(this.beams.size() - 1);
        assert this.level != null; //will fix later - epic
        int l = this.level.getHeight(Heightmap.Type.WORLD_SURFACE, i, k);

        for (int i1 = 0; i1 < 10 && blockpos.getY() <= l; ++i1) {
            BlockState blockstate = this.level.getBlockState(blockpos);
            Block block = blockstate.getBlock();
            if (afloat != null) {
                if (this.beams.size() <= 1) {
                    segment = new EnderBeeconTileEntity.BeamSegment(afloat);
                    this.beams.add(segment);
                } else if (segment != null) {
                    if (Arrays.equals(afloat, segment.colors)) {
                        segment.incrementHeight();
                    } else {
                        segment = new EnderBeeconTileEntity.BeamSegment(new float[]{afloat[0], afloat[1], afloat[2]});
                        this.beams.add(segment);
                    }
                }
            } else {
                if (segment == null || blockstate.getLightBlock(this.level, blockpos) >= 15 && block != Blocks.BEDROCK) {
                    this.beams.clear();
                    this.worldHeight = l;
                    break;
                }
                segment.incrementHeight();
            }
            blockpos = blockpos.above();
            ++this.worldHeight;
        }


        if (this.level.getGameTime() % 80L == 0L && !this.beamSegments.isEmpty() && !getFluidTank().isEmpty()) {
            AxisAlignedBB box = getEffectBox();
            List<BeeEntity> bees = level.getEntitiesOfClass(BeeEntity.class, box);
            bees.stream()
                    .filter(CustomBeeEntity.class::isInstance)
                    .map(CustomBeeEntity.class::cast)
                    .forEach(CustomBeeEntity::setHasDisruptorInRange);
            this.addEffectsToBees(bees);
            if (playSound) this.playSound(SoundEvents.BEACON_AMBIENT);
        }

        if (canStartFluidProcess()) {
            processFluid();
        }

        doPullProcess();

        if (dirty) {
            this.dirty = false;
            this.setChanged();
        }

        int j1 = getFluidTank().getFluidAmount();
        if (this.worldHeight >= l) {
            this.worldHeight = -1;
            boolean flag = j1 > 0;
            this.beamSegments = this.beams;
            if (!this.level.isClientSide) {
                if (flag && updateBeecon && !beeconActive) {
                    this.playSound(SoundEvents.BEACON_ACTIVATE);
                    beeconActive = true;
                } else if (!flag && updateBeecon && beeconActive) {
                    this.playSound(SoundEvents.BEACON_DEACTIVATE);
                    beeconActive = false;
                }
                updateBeecon = false;
            }
        }
    }

    private void pullFluid(Fluid i, IFluidHandler handler) {
        int remainingSpace = getFluidTank().getSpace();
        FluidStack amountDrained;
        if (FLUID_PULL_RATE > remainingSpace) {
            amountDrained = handler.drain(new FluidStack(i, remainingSpace), IFluidHandler.FluidAction.EXECUTE);
        } else {
            amountDrained = handler.drain(new FluidStack(i, FLUID_PULL_RATE), IFluidHandler.FluidAction.EXECUTE);
        }
        getFluidTank().fill(amountDrained, IFluidHandler.FluidAction.EXECUTE);
    }

    private void doPullProcess() {
        assert level != null; //will fix later - epic
        TileEntity tileEntity = level.getBlockEntity(worldPosition.below());
        if (tileEntity == null) return;
        LazyOptional<IFluidHandler> fluidCap = tileEntity.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, Direction.UP);
        fluidCap.map(iFluidHandler -> {
            int tanks = iFluidHandler.getTanks();
            for (int i = 0; i < tanks; i++) {
                if (!iFluidHandler.getFluidInTank(i).isEmpty() &&
                        iFluidHandler.getFluidInTank(i).getFluid().is(HONEY_FLUID_TAG) &&
                        (getFluidTank().isEmpty() || iFluidHandler.getFluidInTank(i).getFluid() == getFluidTank().getFluid().getFluid())) {
                    pullFluid(iFluidHandler.getFluidInTank(i).getFluid(), iFluidHandler);
                    return true;
                }
            }
            return false;
        });
    }


    public void toggleSound() {
        playSound = !playSound;
        dirty = true;
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        return new AxisAlignedBB(worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), worldPosition.getX(), worldPosition.getY() + 255D, worldPosition.getZ());
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public double getViewDistance() {
        return 256.0D;
    }

    public void sendGUINetworkPacket(IContainerListener player) {
        if (player instanceof ServerPlayerEntity && (!(player instanceof FakePlayer))) {
            PacketBuffer buffer = new PacketBuffer(Unpooled.buffer());
            buffer.writeFluidStack(getFluidTank().getFluid());
            NetPacketHandler.sendToPlayer(new SyncGUIMessage(this.worldPosition, buffer), (ServerPlayerEntity) player);
        }
    }

    public static void syncApiaryToPlayersUsing(World world, BlockPos pos, CompoundNBT data) {
        NetPacketHandler.sendToAllLoaded(new UpdateClientBeeconMessage(pos, data), world, pos);
    }


    @OnlyIn(Dist.CLIENT)
    public List<EnderBeeconTileEntity.BeamSegment> getBeamSegments() {
        return beamSegments;
    }

    public List<BeeconEffect> getEffects() {
        return effects;
    }

    public AxisAlignedBB getEffectBox() {
        assert this.level != null;
        return (new AxisAlignedBB(this.worldPosition)).inflate(getRange()).expandTowards(0.0D, this.level.getMaxBuildHeight(), 0.0D);
    }

    public void toggleBeam() {
        setShowBeam(!isShowBeam());
        dirty = true;
    }

    public boolean isShowBeam() {
        return showBeam;
    }

    public void setShowBeam(boolean showBeam) {
        this.showBeam = showBeam;
    }

    public @NotNull AutomationSensitiveItemStackHandler getTileStackHandler() {
        return tileStackHandler;
    }

    public static class BeamSegment {
        private final float[] colors;
        private int height;

        public BeamSegment(float[] colors) {
            this.colors = colors;
            this.height = 1;
        }

        protected void incrementHeight() {
            ++this.height;
        }

        @OnlyIn(Dist.CLIENT)
        public int getHeight() {
            return this.height;
        }
    }

    protected class TileStackHandler extends AutomationSensitiveItemStackHandler {
        protected TileStackHandler(int slots, IAcceptor acceptor, IRemover remover) {
            super(slots, acceptor, remover);
        }

        @Override
        protected void onContentsChanged(int slot) {
            super.onContentsChanged(slot);
            setChanged();
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return EnderBeeconTileEntity.isItemValid(stack);
        }
    }

    public class InternalFluidTank extends FluidTank {
        public InternalFluidTank(int capacity, Predicate<FluidStack> validator) {
            super(capacity, validator);
        }

        @Override
        protected void onContentsChanged() {
            super.onContentsChanged();
            if (level != null) {
                BlockState state = level.getBlockState(worldPosition);
                level.sendBlockUpdated(worldPosition, state, state, 2);
            }
            updateBeecon = true;
        }
    }

    private void addEffectsToBees(List<BeeEntity> bees) {
        assert this.level != null;
        if (!this.level.isClientSide && doEffects()) {
            for (BeeEntity mob : bees) {
                for (BeeconEffect effect : effects) {
                    if (!effect.isActive()) continue;
                    mob.addEffect(new EffectInstance(effect.getEffect(), 120, 0, true, true));
                }
            }
        }
    }

    public int getRange() {
        int range = Config.BEECON_RANGE_PER_EFFECT.get();
        for (BeeconEffect effect : effects) {
            if (effect.isActive()) range += Config.BEECON_RANGE_PER_EFFECT.get();
        }
        return range;
    }

    public boolean doEffects() {
        if (getFluidTank().isEmpty()) return false;
        for (BeeconEffect effect : effects) {
            if (effect.isActive()) return true;
        }
        return false;
    }

    public void updateBeeconEffect(ResourceLocation effectLocation, boolean active) {
        Effect effect = ForgeRegistries.POTIONS.getValue(effectLocation);
        for (BeeconEffect e : effects) {
            if (e.getEffect() == effect) {
                e.setActive(active);
            }
        }
        syncApiaryToPlayersUsing(this.level, this.getBlockPos(), this.writeNBT(new CompoundNBT()));
    }

    public int getDrain() {
        double base = Config.BEECON_BASE_DRAIN.get();
        for (BeeconEffect e : effects) {
            if (Config.BEECON_DO_MULTIPLIER.get()) {
                if (e.isActive()) base *= e.getValue();
            } else {
                if (e.isActive()) base += e.getValue();
            }
        }
        return (int) Math.ceil(base);
    }

    public boolean getEffectActive(Effect effect) {
        BeeconEffect e = getEffect(effect);
        return e != null && e.isActive();
    }

    public BeeconEffect getEffect(Effect effect) {
        for (BeeconEffect e : effects) {
            if (e.getEffect() == effect) return e;
        }
        return null;
    }

    public CompoundNBT writeEffectsToNBT(CompoundNBT nbt) {
        nbt.putBoolean("calming", getEffectActive(ModEffects.CALMING.get()));
        nbt.putBoolean("water_breathing", getEffectActive(Effects.WATER_BREATHING));
        nbt.putBoolean("fire_resistance", getEffectActive(Effects.FIRE_RESISTANCE));
        nbt.putBoolean("regeneration", getEffectActive(Effects.REGENERATION));
        return nbt;
    }

    public List<BeeconEffect> readEffectsFromNBT(CompoundNBT nbt) {
        List<BeeconEffect> beeconEffects = new LinkedList<>();
        beeconEffects.add(new BeeconEffect(ModEffects.CALMING.get(), Config.BEECON_CALMING_VALUE.get(), nbt.getBoolean("calming")));
        beeconEffects.add(new BeeconEffect(Effects.WATER_BREATHING, Config.BEECON_WATER_BREATHING_VALUE.get(), nbt.getBoolean("water_breathing")));
        beeconEffects.add(new BeeconEffect(Effects.FIRE_RESISTANCE, Config.BEECON_FIRE_RESISTANCE_VALUE.get(), nbt.getBoolean("fire_resistance")));
        beeconEffects.add(new BeeconEffect(Effects.REGENERATION, Config.BEECON_REGENERATION_VALUE.get(), nbt.getBoolean("regeneration")));
        return beeconEffects;
    }

    public static boolean isItemValid(ItemStack stack) {
        if (stack.getItem() instanceof BucketItem) {
            BucketItem bucket = (BucketItem) stack.getItem();
            return bucket.getFluid().is(HONEY_FLUID_TAG);
        } else {
            return stack.getItem().is(HONEY_BOTTLE_TAG);
        }
    }

    public static class BeeconEffect {
        private Effect effect;
        private double value;
        private boolean active;

        public BeeconEffect(Effect effect, double multiplier, boolean active) {
            this.setEffect(effect);
            this.setValue(multiplier);
            this.setActive(active);
        }

        public Effect getEffect() {
            return effect;
        }

        public void setEffect(Effect effect) {
            this.effect = effect;
        }

        public double getValue() {
            return value;
        }

        public void setValue(double value) {
            this.value = value;
        }

        public boolean isActive() {
            return active;
        }

        public void setActive(boolean active) {
            this.active = active;
        }
    }
}
