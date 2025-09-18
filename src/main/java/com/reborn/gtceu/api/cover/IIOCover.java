package com.reborn.gtceu.api.cover;

import com.reborn.gtceu.api.capability.recipe.IO;
import com.reborn.gtceu.common.cover.data.ManualIOMode;

public interface IIOCover {

    int getTransferRate();

    IO getIo();

    ManualIOMode getManualIOMode();
}
