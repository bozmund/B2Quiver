package btwo.quiver;

import btwo.quiver.item.QuiverContentsComponent;
import btwo.quiver.item.QuiverItem;
import net.fabricmc.api.ModInitializer;

import net.minecraft.component.DataComponentType;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.UnaryOperator;

public class B2Quiver implements ModInitializer {
    public static final String MODID = "b2quiver";
    public static final Logger LOGGER = LoggerFactory.getLogger("b2quiver");
    UnaryOperator<DataComponentType.Builder<QuiverContentsComponent>> builderOperator = (builder) -> builder.codec(QuiverContentsComponent.CODEC).packetCodec(QuiverContentsComponent.PACKET_CODEC);


    @Override
    public void onInitialize() {

        LOGGER.info("Hello Fabric world!");
        Registry.register(Registries.DATA_COMPONENT_TYPE,new Identifier(MODID, "quiver_contents"), ((DataComponentType.Builder)builderOperator.apply(DataComponentType.builder())).build());
        Registry.register(Registries.ITEM, new Identifier(MODID, "quiver"), new QuiverItem(new Item.Settings()));
    }
}
