package tn.nightbeam.ras.platform;

import net.minecraftforge.fml.loading.FMLPaths;

import java.nio.file.Path;

/**
 * Forge implementation of IConfigService: JSON files under Forge's config directory.
 */
public class ForgeConfigService extends JsonConfigService {

    @Override
    public Path getConfigDirectory() {
        return FMLPaths.CONFIGDIR.get();
    }
}
