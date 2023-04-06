package igentuman.nc.phosphophyllite.multiblock2;

import net.minecraft.network.chat.Component;

public class ValidationException extends Exception {
    private final Component cause;
    
    public ValidationException(String message) {
        super(message);
        cause = null;
    }
    
    public ValidationException(Component cause) {
        super();
        this.cause = cause;
    }
    
    public Component getTextComponent() {
        if (cause != null) {
            return cause;
        }
        return Component.translatable(getMessage());
    }
}
