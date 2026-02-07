package com.gregtechceu.gtceu.client.util;

import com.lowdragmc.lowdraglib.client.bakedpipeline.FaceQuad;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BlockModelRotation;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;

public class StaticFaceBakery {

    public static final AABB SLIGHTLY_OVER_BLOCK = new AABB(-0.001f, -0.001f, -0.001f,
            1.001f, 1.001f, 1.001f);
    public static final AABB OUTPUT_OVERLAY = new AABB(-.006f, -.006f, -.006f,
            1.006f, 1.006f, 1.006f);
    public static final AABB AUTO_OUTPUT_OVERLAY = new AABB(-.008f, -.008f, -.008f,
            1.008f, 1.008f, 1.008f);
    public static final AABB COVER_OVERLAY = new AABB(-.008f, -.008f, -.008f,
            1.008f, 1.008f, 1.008f);

    public static BakedQuad bakeFace(AABB cube, Direction face, TextureAtlasSprite sprite) {
        return FaceQuad.bakeFace(cube, face, sprite, BlockModelRotation.X0_Y0, -1, 0, true, true);
    }
}
