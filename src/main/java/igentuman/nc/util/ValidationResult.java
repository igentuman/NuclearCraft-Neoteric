package igentuman.nc.util;

import net.minecraft.core.BlockPos;

public class ValidationResult {
    public boolean isValid = false;
    public String messageKey;
    public BlockPos errorBlock;

    public ValidationResult(boolean isValid) {
        this.isValid = isValid;
    }

    public ValidationResult(boolean isValid, String s, BlockPos offset) {
        this.isValid = false;
        messageKey = s;
        errorBlock = offset;
    }
}
