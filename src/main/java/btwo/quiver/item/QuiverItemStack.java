//package b2.quiver.item;
//
//import net.minecraft.item.ItemConvertible;
//import net.minecraft.item.ItemStack;
//import net.minecraft.nbt.NbtCompound;
//import net.minecraft.nbt.NbtElement;
//import net.minecraft.registry.Registries;
//import net.minecraft.util.Identifier;
//
//public class QuiverItemStack extends ItemStack {
//
//	private final ItemStack quiver;
//	
//	public QuiverItemStack(ItemConvertible item, ItemStack quiver) {
//		super(item, 1);
//		this.quiver = quiver;
//	}
//	
//	private QuiverItemStack(NbtCompound nbt, ItemStack quiver) {
//		this(Registries.ITEM.get(new Identifier(nbt.getString("id"))), quiver);
//		super.setCount(nbt.getByte("Count"));
//		
//		if (nbt.contains("tag", NbtElement.COMPOUND_TYPE)) {
//			setNbt(nbt.getCompound("tag").copy());
//			getItem().postProcessNbt(getNbt());
//		}
//		
//		if (getItem().isDamageable()) {
//			setDamage(getDamage());
//		}
//	}
//
//	public static ItemStack fromNbt(NbtCompound nbt, ItemStack quiver) {
//		try {
//			return new QuiverItemStack(nbt, quiver);
//		} catch (RuntimeException runtimeException) {
//			return ItemStack.EMPTY;
//		}
//	}
//	
//	public void setCount(int count) {
//		super.setCount((count == 0) ? 1 : count);
//		QuiverItem.updateCount(quiver, this, count);
//	}
//	
//}
