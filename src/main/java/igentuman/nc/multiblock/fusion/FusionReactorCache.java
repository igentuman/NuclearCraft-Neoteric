package igentuman.nc.multiblock.fusion;

import igentuman.nc.api.multiblock.AbstractMultiblockCache;

public class FusionReactorCache extends AbstractMultiblockCache {

    double workingMagneticFieldStrength;
    int workingMagnetsEfficiency;
    int workingMagnetsPower;
    int workingMaxMagnetsTemp;
    int workingRfAmplification;
    int workingRfAmplifiersPower;
    int workingRfEfficiency;
    int workingMinRFAmplifiersTemp;
    int workingMagnetCount;
    int workingAmplifierCount;
    int workingConnectorCount;
    int workingCasingCount;
    int workingSize;

    public volatile double magneticFieldStrength;
    public volatile int magnetsEfficiency;
    public volatile int magnetsPower;
    public volatile int maxMagnetsTemp;
    public volatile int rfAmplification;
    public volatile int rfAmplifiersPower;
    public volatile int rfEfficiency;
    public volatile int minRFAmplifiersTemp;
    public volatile int magnetCount;
    public volatile int amplifierCount;
    public volatile int connectorCount;
    public volatile int casingCount;
    public volatile int size;

    @Override
    protected void resetWorkingData() {
        workingMagneticFieldStrength = 0;
        workingMagnetsEfficiency = 0;
        workingMagnetsPower = 0;
        workingMaxMagnetsTemp = 0;
        workingRfAmplification = 0;
        workingRfAmplifiersPower = 0;
        workingRfEfficiency = 0;
        workingMinRFAmplifiersTemp = 0;
        workingMagnetCount = 0;
        workingAmplifierCount = 0;
        workingConnectorCount = 0;
        workingCasingCount = 0;
        workingSize = 0;
    }

    @Override
    protected void publishData() {
        magneticFieldStrength = workingMagneticFieldStrength;
        magnetsEfficiency = workingMagnetsEfficiency;
        magnetsPower = workingMagnetsPower;
        maxMagnetsTemp = workingMaxMagnetsTemp;
        rfAmplification = workingRfAmplification;
        rfAmplifiersPower = workingRfAmplifiersPower;
        rfEfficiency = workingRfEfficiency;
        minRFAmplifiersTemp = workingMinRFAmplifiersTemp;
        magnetCount = workingMagnetCount;
        amplifierCount = workingAmplifierCount;
        connectorCount = workingConnectorCount;
        casingCount = workingCasingCount;
        size = workingSize;
    }
}
