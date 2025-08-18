package de.maxhenkel.pipez.integration.kubejs;

import dev.latvian.mods.kubejs.plugin.KubeJSPlugin;
import dev.latvian.mods.kubejs.registry.BuilderTypeRegistry;
import net.minecraft.core.registries.Registries;

/**
 * @description: add kubejs support
 * @author: HowXu
 * @date: 2025/8/17 23:26
 */

public class KJSPlugin implements KubeJSPlugin {

    @Override
    public void registerBuilderTypes(BuilderTypeRegistry registry) {
        registry.of(
                Registries.ITEM,
                r -> r.add("custom_pipez_upgrade", KJSUpgradeItemBuilder.class, KJSUpgradeItemBuilder::new)
        );
    }

}
