package com.gregtechceu.gtceu.api.material.material.properties;

public class WoodProperty implements IMaterialProperty {

    @Override
    public void verifyProperty(MaterialProperties properties) {
        properties.ensureSet(PropertyKey.DUST, true);
    }
}
