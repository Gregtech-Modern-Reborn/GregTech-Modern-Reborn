package com.gregtechceu.gtceu.integration.ponder;

import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.minecraft.core.Direction;

public class CokeOvenScreenAnimationStage1 {

    public static void coke_oven(SceneBuilder scene, SceneBuildingUtil sceneBuildingUtil) {
        scene.title("coke_oven", "Using of Coke Oven");
        scene.configureBasePlate(0, 0, 7);
        scene.world().showSection(sceneBuildingUtil.select().layer(0), Direction.UP);
        scene.overlay().showText(20)
                .attachKeyFrame()
                .pointAt(sceneBuildingUtil.vector().topOf(sceneBuildingUtil.grid().at(1, 2, 2)))
                .placeNearTarget()
                .text("Coke Oven is a multiblock structure");
        scene.idle(10);
        scene.world().showSection(sceneBuildingUtil.select().layer(1), Direction.UP);
        scene.idle(10);
        scene.world().showSection(sceneBuildingUtil.select().layer(2), Direction.UP);
        scene.idle(10);
        scene.world().showSection(sceneBuildingUtil.select().layer(3), Direction.UP);
        scene.overlay().showText(20)
                .attachKeyFrame()
                .pointAt(sceneBuildingUtil.vector().topOf(sceneBuildingUtil.grid().at(1, 2, 2)))
                .placeNearTarget()
                .text("This is how a coke oven is built.");
        scene.idle(30);
        scene.overlay().showText(60)
                .attachKeyFrame()
                .pointAt(sceneBuildingUtil.vector().topOf(sceneBuildingUtil.grid().at(1, 2, 2)))
                .placeNearTarget()
                .text("After the construction is completed, right-click the coke oven, put the items that need to be burned, and the coke oven will start working.");
        scene.idle(70);
        scene.overlay().showText(40)
                .attachKeyFrame()
                .pointAt(sceneBuildingUtil.vector().topOf(sceneBuildingUtil.grid().at(1, 2, 2)))
                .placeNearTarget()
                .text("When the coke oven is working, it will produce creosote, which can be used as fuel to burn 32 items.");
        scene.idle(50);
        scene.markAsFinished();
    }
}
