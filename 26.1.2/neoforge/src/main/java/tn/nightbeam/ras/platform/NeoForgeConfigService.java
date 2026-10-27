package tn.nightbeam.ras.platform;

import net.neoforged.fml.loading.FMLPaths;

import java.nio.file.Path;

/**
 * NeoForge implementation of IConfigService: JSON files under NeoForge's config directory.
 */
public class NeoForgeConfigService extends JsonConfigService {

    @Override
    public Path getConfigDirectory() {
        return FMLPaths.CONFIGDIR.get();
    }
}
