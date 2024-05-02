//package b2.quiver.item;
//
//import net.minecraft.client.item.TooltipData;
//import net.minecraft.component.ComponentMap;
//import net.minecraft.component.type.BundleContentsComponent;
//import net.minecraft.entity.ItemEntity;
//import net.minecraft.entity.player.PlayerEntity;
//import net.minecraft.inventory.StackReference;
//import net.minecraft.item.BundleItem;
//import net.minecraft.item.Item;
//import net.minecraft.item.ItemStack;
//import net.minecraft.nbt.NbtCompound;
//import net.minecraft.nbt.NbtElement;
//import net.minecraft.nbt.NbtList;
//import net.minecraft.registry.tag.ItemTags;
//import net.minecraft.screen.slot.Slot;
//import net.minecraft.server.network.ServerPlayerEntity;
//import net.minecraft.sound.SoundCategory;
//import net.minecraft.sound.SoundEvents;
//import net.minecraft.util.*;
//import net.minecraft.util.collection.DefaultedList;
//import net.minecraft.util.math.MathHelper;
//import net.minecraft.world.World;
//
//import java.util.Optional;
//import java.util.function.Predicate;
//import java.util.stream.Stream;
//
//public class QuiverItem extends Item {
//
//    public static final int MAX_STORAGE = 256;
//
//    private static final String ARROWS_KEY = "Arrows";
//    private static final String TOOLTIP_ID = "item.minecraft.bundle.fullness";
//    private static final int ITEM_BAR_COLOR = MathHelper.packRgb(0.4f, 0.4f, 1f);
//
//    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
//        ItemStack quiver = user.getStackInHand(hand);
//
//        if (removeAllArrows(quiver, user)) {
//            // this.playDropContentsSound(user);
//            return TypedActionResult.success(quiver, world.isClient());
//        }
//
//        return TypedActionResult.fail(quiver);
//    }
//
//    @Override
//    public int getItemBarColor(ItemStack stack) {
//        return ITEM_BAR_COLOR;
//    }
//
//    @Override
//    public boolean isItemBarVisible(ItemStack stack) {
//        return getArrowCount(stack) > 0;
//    }
//
//    @Override
//    public int getItemBarStep(ItemStack stack) {
//        return Math.min(1 + 12 * getArrowCount(stack) / MAX_STORAGE, 13);
//    }
//
//    @Override
//    public Optional<TooltipData> getTooltipData(ItemStack quiver) {
//        DefaultedList<ItemStack> stackList = DefaultedList.of();
//        getArrows(quiver).forEach(stackList::add);
//
//        return Optional.of(new BundleContentsComponent(stackList));
//    }
//    
//    public static int insertArrows(ItemStack quiver, ItemStack arrows) {
//        if (arrows.isEmpty() || !arrows.isIn(ItemTags.ARROWS)) {
//            return 0;
//        }
//
//        NbtCompound nbt = quiver.getOrCreateNbt();
//        if (!nbt.contains(ARROWS_KEY)) {
//            nbt.put(ARROWS_KEY, new NbtList());
//        }
//
//        int occupancy = getArrowCount(quiver);
//        int insertCount = Math.min(arrows.getCount(), MAX_STORAGE - occupancy);
//        if (insertCount <= 0) {
//            return 0;
//        }
//
//        NbtList arrowList = nbt.getList(ARROWS_KEY, NbtElement.COMPOUND_TYPE);
//        for (int toInsert = insertCount; toInsert > 0; ) {
//            Optional<NbtCompound> existingStack = findStackToMerge(arrows, arrowList);
//
//            if (existingStack.isPresent()) {
//                NbtCompound existingNbt = existingStack.get();
//                ItemStack before = ItemStack.fromNbt(existingNbt);
//
//                int toInsertNow = Math.min(toInsert, before.getMaxCount() - before.getCount());
//                before.increment(toInsertNow);
//                before.writeNbt(existingNbt);
//
//                arrowList.remove(existingNbt);
//                arrowList.add(0, existingNbt);
//                toInsert -= toInsertNow;
//            } else {
//                ItemStack stack = arrows.copyWithCount(toInsert);
//                NbtCompound stackNbt = new NbtCompound();
//                stack.writeNbt(stackNbt);
//                arrowList.add(0, stackNbt);
//                toInsert -= stack.getCount();
//            }
//        }
//
//        return insertCount;
//    }
//
//    private static Optional<NbtCompound> findStackToMerge(ItemStack arrows, NbtList items) {
//        return items.stream()
//                .filter(NbtCompound.class::isInstance)
//                .map(NbtCompound.class::cast)
//                .filter(item -> {
//                    ItemStack existing = ItemStack.fromNbt(item);
//                    return ItemStack.canCombine(existing, arrows) && existing.getCount() < existing.getMaxCount();
//                })
//                .findFirst();
//    }
//
//    public static Optional<ItemStack> removeFirstStack(ItemStack quiver, Predicate<ItemStack> predicate) {
//        NbtCompound nbt = quiver.getOrCreateNbt();
//        if (!nbt.contains(ARROWS_KEY)) {
//            return Optional.empty();
//        }
//
//        NbtList nbtList = nbt.getList(ARROWS_KEY, NbtElement.COMPOUND_TYPE);
//        ItemStack foundStack = null;
//
//        for (int i = 0; i < nbtList.size(); i++) {
//            NbtCompound firstNbtStack = nbtList.getCompound(i);
//            ItemStack nextStack = ItemStack.fromNbt(firstNbtStack);
//
//            if (predicate.test(nextStack)) {
//                foundStack = nextStack;
//                nbtList.remove(i);
//                break;
//            }
//        }
//
//        if (nbtList.isEmpty()) {
//            quiver.removeSubNbt(ARROWS_KEY);
//        }
//
//        return foundStack == null ? Optional.empty() : Optional.of(foundStack);
//    }
//
//    public static Optional<ItemStack> getFirstStack(ItemStack quiver, Predicate<ItemStack> predicate) {
//        NbtCompound nbt = quiver.getNbt();
//
//        if (nbt == null) {
//            return Optional.empty();
//        }
//
//        return nbt.getList(ARROWS_KEY, NbtElement.COMPOUND_TYPE).stream()
//                .map(NbtCompound.class::cast)
//                .map(nbtStack -> QuiverItemStack.fromNbt(nbtStack, quiver))
//                .filter(predicate)
//                .findFirst();
//    }
//
//    public static void updateCount(ItemStack quiver, ItemStack arrowStack, int count) {
//        removeFirstStack(quiver, stack -> ItemStack.canCombine(arrowStack, stack))
//                .ifPresent(stack -> {
//                    stack.setCount(count);
//                    if (!stack.isEmpty() && stack.getCount() > 0 && count > 0) {
//                        insertArrows(quiver, stack);
//                    }
//                });
//    }
//
//    private static boolean removeAllArrows(ItemStack quiver, PlayerEntity player) {
//        NbtCompound nbt = quiver.getOrCreateNbt();
//
//        if (!nbt.contains(ARROWS_KEY)) {
//            return false;
//        }
//
//        if (player instanceof ServerPlayerEntity) {
//            NbtList arrowList = nbt.getList(ARROWS_KEY, NbtElement.COMPOUND_TYPE);
//
//            for (int i = 0; i < arrowList.size(); ++i) {
//                NbtCompound arrowStackNbt = arrowList.getCompound(i);
//                ItemStack arrowStack = ItemStack.fromNbt(arrowStackNbt);
//                player.dropItem(arrowStack, true);
//            }
//        }
//        quiver.removeSubNbt(ARROWS_KEY);
//        return true;
//    }
//
//    public static int getArrowCount(ItemStack quiver) {
//        return getArrows(quiver)
//                .mapToInt(ItemStack::getCount)
//                .sum();
//    }
//
//    public static Stream<ItemStack> getArrows(ItemStack quiver) {
//        ComponentMap quiverComponents = quiver.getComponents();
//
//        if (!quiverComponents.isEmpty()) {
//            return quiverComponents.get(ItemStack., NbtElement.COMPOUND_TYPE).stream()
//                    .map(NbtCompound.class::cast)
//                    .map(ItemStack::fromNbt);
//        }
//
//        return Stream.empty();
//    }
//}
//
//
////    public static void registerEvents() {
////        PlayerEntityEvents.ITEM_PICKUP.register((PlayerEntity player, ItemEntity item) -> {
////            if (player.getWorld().isClient) {
////                return ActionResult.PASS;
////            }
////
////            ItemStack quiver;
////
////            List<Pair<SlotReference, ItemStack>> quiverTrinket = TrinketsApi.getTrinketComponent(player).get().getEquipped(LegendGear.QUIVER);
////
////            if (!quiverTrinket.isEmpty()) {
////                quiver = quiverTrinket.stream().findFirst().map(Pair::getRight).orElse(ItemStack.EMPTY);
////
////                if (quiver.isOf(LegendGear.QUIVER) && getArrowCount(quiver) < MAX_STORAGE) {
////                    ItemStack arrowStack = item.getStack();
////                    int inserted = insertArrows(quiver, arrowStack);
////
////                    if (inserted == arrowStack.getCount()) {
////                        item.discard();
////                        player.getWorld().playSound(null, player.getBlockPos(), SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.NEUTRAL);
////                        return ActionResult.CONSUME;
////                    } else {
////                        arrowStack.decrement(inserted);
////                        item.setStack(arrowStack);
////                        return ActionResult.CONSUME_PARTIAL;
////                    }
////                }
////            } else {
////                for (int i = 0; i < player.getInventory().size(); ++i) {
////                    quiver = player.getInventory().getStack(i);
////
////                    if (quiver.isOf(LegendGear.QUIVER) && getArrowCount(quiver) < MAX_STORAGE) {
////                        ItemStack arrowStack = item.getStack();
////                        int inserted = insertArrows(quiver, arrowStack);
////
////                        if (inserted == arrowStack.getCount()) {
////                            item.discard();
////                            player.getWorld().playSound(null, player.getBlockPos(), SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.NEUTRAL);
////                            return ActionResult.CONSUME;
////                        } else {
////                            arrowStack.decrement(inserted);
////                            item.setStack(arrowStack);
////                            return ActionResult.CONSUME_PARTIAL;
////                        }
////                    }
////                }
////            }
////
////            return ActionResult.PASS;
////        });
////    }
//
//    public static void registerEvents() {
//        PlayerEntityEvents.ITEM_PICKUP.register((PlayerEntity player, ItemEntity item) -> {
//            if (player.getWorld().isClient || !item.getStack().isIn(ItemTags.ARROWS)) {
//                return ActionResult.PASS;
//            }
//
//            ItemStack arrowStack = item.getStack();
//            TrinketComponent trinketComponent = TrinketsApi.getTrinketComponent(player).get();
//
//            for (Pair<SlotReference, ItemStack> equipped : trinketComponent.getEquipped(LegendGear.QUIVER)) {
//                ActionResult result = tryInsertArrows(player, item, equipped.getRight(), arrowStack);
//                if (result != ActionResult.PASS) {
//                    return result;
//                }
//            }
//
//            for (int i = 0; i < player.getInventory().size(); ++i) {
//                ItemStack quiver = player.getInventory().getStack(i);
//                ActionResult result = tryInsertArrows(player, item, quiver, arrowStack);
//                if (result != ActionResult.PASS) {
//                    return result;
//                }
//            }
//
//            return ActionResult.PASS;
//        });
//    }
//
//    private static ActionResult tryInsertArrows(PlayerEntity player, ItemEntity item, ItemStack quiver, ItemStack arrowStack) {
//        if (quiver.isOf(LegendGear.QUIVER) && getArrowCount(quiver) < MAX_STORAGE) {
//            int inserted = insertArrows(quiver, arrowStack);
//            if (inserted == arrowStack.getCount()) {
//                item.discard();
//                player.getWorld().playSound(null, player.getBlockPos(), SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.NEUTRAL);
//                return ActionResult.CONSUME;
//            } else {
//                arrowStack.decrement(inserted);
//                item.setStack(arrowStack);
//                return ActionResult.CONSUME_PARTIAL;
//            }
//        }
//        return ActionResult.PASS;
//    }
//}
