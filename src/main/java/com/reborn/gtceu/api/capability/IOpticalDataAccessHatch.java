package com.reborn.gtceu.api.capability;

public interface IOpticalDataAccessHatch extends IDataAccessHatch {

    /**
     * @return if this hatch transmits data through cables
     */
    boolean isTransmitter();
}
