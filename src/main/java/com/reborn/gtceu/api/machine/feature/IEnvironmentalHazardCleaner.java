package com.reborn.gtceu.api.machine.feature;

import com.reborn.gtceu.api.data.medicalcondition.MedicalCondition;

public interface IEnvironmentalHazardCleaner extends IMachineFeature {

    float getRemovedLastSecond();

    void cleanHazard(MedicalCondition condition, float amount);
}
