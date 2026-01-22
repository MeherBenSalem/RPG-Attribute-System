package tn.nightbeam.ras.neoforge;

import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.registries.DeferredHolder;
import tn.nightbeam.ras.RpgAttributeSystemMod;
import tn.nightbeam.ras.network.PlayerVariables;

/**
 * NeoForge Data Attachments for storing player variables.
 * Replaces Forge capabilities for 1.21.x.
 */
public class NeoForgeDataAttachments {
        public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister
                        .create(NeoForgeRegistries.ATTACHMENT_TYPES, RpgAttributeSystemMod.MOD_ID);

        public static final DeferredHolder<AttachmentType<?>, AttachmentType<PlayerVariables>> PLAYER_VARIABLES = ATTACHMENT_TYPES
                        .register("player_variables", () -> AttachmentType.builder(PlayerVariables::new)
                                        .serialize(new net.neoforged.neoforge.attachment.IAttachmentSerializer<net.minecraft.nbt.Tag, PlayerVariables>() {
                                                @Override
                                                public net.minecraft.nbt.Tag write(PlayerVariables attachment,
                                                                net.minecraft.core.HolderLookup.Provider provider) {
                                                        return attachment.writeNBT();
                                                }

                                                @Override
                                                public PlayerVariables read(
                                                                net.neoforged.neoforge.attachment.IAttachmentHolder holder,
                                                                net.minecraft.nbt.Tag tag,
                                                                net.minecraft.core.HolderLookup.Provider provider) {
                                                        PlayerVariables vars = new PlayerVariables();
                                                        vars.readNBT(tag);
                                                        return vars;
                                                }
                                        })
                                        .copyOnDeath()
                                        .build());
}
