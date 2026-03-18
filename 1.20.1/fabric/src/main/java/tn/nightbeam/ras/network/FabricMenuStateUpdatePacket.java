package tn.nightbeam.ras.network;

import net.minecraft.network.FriendlyByteBuf;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.resources.ResourceLocation;

public class FabricMenuStateUpdatePacket {
    public static final ResourceLocation ID = new ResourceLocation("rpg_attribute_system", "menu_update");

    public static FriendlyByteBuf create(int elementType, String name, Object elementState) {
        FriendlyByteBuf buf = PacketByteBufs.create();
        buf.writeInt(elementType);
        buf.writeUtf(name);
        if (elementType == 0) {
            buf.writeUtf((String) elementState);
        } else if (elementType == 1) {
            buf.writeBoolean((boolean) elementState);
        }
        return buf;
    }

    public static class Data {
        public final int elementType;
        public final String name;
        public final Object elementState;

        public Data(FriendlyByteBuf buf) {
            this.elementType = buf.readInt();
            this.name = buf.readUtf();
            Object state = null;
            if (this.elementType == 0) {
                state = buf.readUtf();
            } else if (this.elementType == 1) {
                state = buf.readBoolean();
            }
            this.elementState = state;
        }
    }
}
