package igentuman.nc.advancements;

import net.minecraft.advancements.CriteriaTriggers;

public class NCAdvancementTriggers {
    public static final RadiationContaminationTrigger RADIATION_CONTAMINATION = new RadiationContaminationTrigger();

    public static void register() {
        CriteriaTriggers.register(RADIATION_CONTAMINATION);
    }
}
