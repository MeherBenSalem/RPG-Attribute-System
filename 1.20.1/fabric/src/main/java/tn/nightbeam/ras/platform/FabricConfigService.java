package tn.nightbeam.ras.platform;

import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Path;

/**
 * Fabric implementation of IConfigService: JSON files under FabricLoader's config directory.
 */
public class FabricConfigService extends JsonConfigService {

    @Override
    public Path getConfigDirectory() {
        return FabricLoader.getInstance().getConfigDir();
    }
}
