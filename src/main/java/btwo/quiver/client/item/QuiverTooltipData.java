package btwo.quiver.client.item;

import btwo.quiver.item.QuiverContentsComponent;
import net.minecraft.client.item.TooltipData;

public record QuiverTooltipData(QuiverContentsComponent contents) implements TooltipData {
    public QuiverTooltipData(QuiverContentsComponent contents) {
        this.contents = contents;
    }

    public QuiverContentsComponent contents() {
        return this.contents;
    }
}
