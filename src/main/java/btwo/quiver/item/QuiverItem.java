package btwo.quiver.item;

import btwo.quiver.client.item.QuiverTooltipData;
import btwo.quiver.component.DataComponentTypes;
import net.minecraft.client.item.TooltipData;
import net.minecraft.client.item.TooltipType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsage;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;
import net.minecraft.util.ClickType;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.apache.commons.lang3.math.Fraction;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public class QuiverItem extends Item{
    private static final String ARROWS_KEY = "Arrows";
    private static final int ITEM_BAR_COLOR = MathHelper.packRgb(0.4F, 0.4F, 1.0F);
    private static final int field_51352 = 64;

    public QuiverItem(Item.Settings settings) {
        super(settings);
    }

    public static float getAmountFilled(ItemStack stack) {
        QuiverContentsComponent bundleContentsComponent = stack.getOrDefault(DataComponentTypes.QUIVER_CONTENTS, QuiverContentsComponent.DEFAULT);
        return bundleContentsComponent.getOccupancy().floatValue();
    }

    public static Optional<ItemStack> getFirstStack(ItemStack quiver, Predicate<ItemStack> predicate) {
        QuiverContentsComponent quiverContentsComponent = quiver.get(DataComponentTypes.QUIVER_CONTENTS);

        if (quiverContentsComponent == null) {
            return Optional.empty();
        }

        return quiverContentsComponent.stream().findFirst().filter(predicate);
    }

    @Override
    public boolean onStackClicked(ItemStack stack, Slot slot, ClickType clickType, PlayerEntity player) {
        if (clickType != ClickType.RIGHT) {
            return false;
        } else {
            QuiverContentsComponent bundleContentsComponent = stack.get(DataComponentTypes.QUIVER_CONTENTS);
            if (bundleContentsComponent == null) {
                return false;
            } else {
                ItemStack itemStack = slot.getStack();
                QuiverContentsComponent.Builder builder = new QuiverContentsComponent.Builder(bundleContentsComponent);
                if (itemStack.isEmpty()) {
                    this.playRemoveOneSound(player);
                    ItemStack itemStack2 = builder.removeFirst();
                    if (itemStack2 != null) {
                        ItemStack itemStack3 = slot.insertStack(itemStack2);
                        builder.add(itemStack3);
                    }
                } else if (itemStack.getItem().canBeNested() && itemStack.isIn(ItemTags.ARROWS)) {
                    int i = builder.add(slot, player);
                    if (i > 0) {
                        this.playInsertSound(player);
                    }
                }

                stack.set(DataComponentTypes.QUIVER_CONTENTS, builder.build());
                return true;
            }
        }
    }

    @Override
    public boolean onClicked(ItemStack stack, ItemStack otherStack, Slot slot, ClickType clickType, PlayerEntity player, StackReference cursorStackReference) {
        if (clickType == ClickType.RIGHT && slot.canTakePartial(player)) {
            QuiverContentsComponent bundleContentsComponent = stack.get(DataComponentTypes.QUIVER_CONTENTS);
            if (bundleContentsComponent == null) {
                return false;
            } else {
                QuiverContentsComponent.Builder builder = new QuiverContentsComponent.Builder(bundleContentsComponent);
                if (otherStack.isEmpty()) {
                    ItemStack itemStack = builder.removeFirst();
                    if (itemStack != null) {
                        this.playRemoveOneSound(player);
                        cursorStackReference.set(itemStack);
                    }
                } else {
                    int i = builder.add(otherStack);
                    if (i > 0) {
                        this.playInsertSound(player);
                    }
                }

                stack.set(DataComponentTypes.QUIVER_CONTENTS, builder.build());
                return true;
            }
        } else {
            return false;
        }
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack itemStack = user.getStackInHand(hand);
        if (dropAllBundledItems(itemStack, user)) {
            this.playDropContentsSound(user);
            user.incrementStat(Stats.USED.getOrCreateStat(this));
            return TypedActionResult.success(itemStack, world.isClient());
        } else {
            return TypedActionResult.fail(itemStack);
        }
    }

    @Override
    public boolean isItemBarVisible(ItemStack stack) {
        QuiverContentsComponent bundleContentsComponent = stack.getOrDefault(DataComponentTypes.QUIVER_CONTENTS, QuiverContentsComponent.DEFAULT);
        return bundleContentsComponent.getOccupancy().compareTo(Fraction.ZERO) > 0;
    }

    @Override
    public int getItemBarStep(ItemStack stack) {
        QuiverContentsComponent bundleContentsComponent = stack.getOrDefault(DataComponentTypes.QUIVER_CONTENTS, QuiverContentsComponent.DEFAULT);
        return Math.min(1 + MathHelper.multiplyFraction(bundleContentsComponent.getOccupancy(), 12), 13);
    }

    @Override
    public int getItemBarColor(ItemStack stack) {
        return ITEM_BAR_COLOR;
    }

    private static boolean dropAllBundledItems(ItemStack stack, PlayerEntity player) {
        QuiverContentsComponent bundleContentsComponent = stack.get(DataComponentTypes.QUIVER_CONTENTS);
        if (bundleContentsComponent != null && !bundleContentsComponent.isEmpty()) {
            stack.set(DataComponentTypes.QUIVER_CONTENTS, QuiverContentsComponent.DEFAULT);
            if (player instanceof ServerPlayerEntity) {
                bundleContentsComponent.iterateCopy().forEach(stackx -> player.dropItem(stackx, true));
            }

            return true;
        } else {
            return false;
        }
    }

    @Override
    public Optional<TooltipData> getTooltipData(ItemStack stack) {
        return !stack.contains(net.minecraft.component.DataComponentTypes.HIDE_TOOLTIP) && !stack.contains(net.minecraft.component.DataComponentTypes.HIDE_ADDITIONAL_TOOLTIP)
                ? Optional.ofNullable(stack.get(DataComponentTypes.QUIVER_CONTENTS)).map(QuiverTooltipData::new)
                : Optional.empty();
    }

    @Override
    public void appendTooltip(ItemStack stack, Item.TooltipContext context, List<Text> tooltip, TooltipType type) {
        QuiverContentsComponent bundleContentsComponent = stack.get(DataComponentTypes.QUIVER_CONTENTS);
        if (bundleContentsComponent != null) {
            int i = MathHelper.multiplyFraction(bundleContentsComponent.getOccupancy(), 64);
            tooltip.add(Text.translatable("item.minecraft.bundle.fullness", i, 64).formatted(Formatting.GRAY));
        }
    }

    @Override
    public void onItemEntityDestroyed(ItemEntity entity) {
        QuiverContentsComponent bundleContentsComponent = entity.getStack().get(DataComponentTypes.QUIVER_CONTENTS);
        if (bundleContentsComponent != null) {
            entity.getStack().set(DataComponentTypes.QUIVER_CONTENTS, QuiverContentsComponent.DEFAULT);
            ItemUsage.spawnItemContents(entity, bundleContentsComponent.iterateCopy());
        }
    }

    private void playRemoveOneSound(Entity entity) {
        entity.playSound(SoundEvents.ITEM_BUNDLE_REMOVE_ONE, 0.8F, 0.8F + entity.getWorld().getRandom().nextFloat() * 0.4F);
    }

    private void playInsertSound(Entity entity) {
        entity.playSound(SoundEvents.ITEM_BUNDLE_INSERT, 0.8F, 0.8F + entity.getWorld().getRandom().nextFloat() * 0.4F);
    }

    private void playDropContentsSound(Entity entity) {
        entity.playSound(SoundEvents.ITEM_BUNDLE_DROP_CONTENTS, 0.8F, 0.8F + entity.getWorld().getRandom().nextFloat() * 0.4F);
    }
}
