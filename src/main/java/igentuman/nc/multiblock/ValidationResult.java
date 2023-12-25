package igentuman.nc.multiblock;

public enum ValidationResult {
    TOO_BIG(false, 0, "validation.structure.too_big"),
    TOO_SMALL(false, 1, "validation.structure.too_small"),
    INCOMPLETE(false, 2, "validation.structure.incomplete"),
    WRONG_OUTER(false, 3, "validation.structure.wrong_outer"),
    WRONG_INNER(false, 4, "validation.structure.wrong_inner"),
    TOO_MANY_CONTROLLERS(false, 5, "validation.structure.too_many_controllers"),
    NO_CONTROLLER(false, 6, "validation.structure.no_controller"),
    NO_PORT(false, 7, "validation.structure.no_port"),
    VALID(true, 8, "validation.structure.valid"),
    WRONG_PROPORTIONS(false,9, "validation.structure.wrong_proportions");

    public boolean isValid;
    public int id;
    public String messageKey = "";

    ValidationResult(boolean isValid, int id, String messageKey) {
        this.isValid = isValid;
        this.id = id;
        this.messageKey = messageKey;
    }

    public static ValidationResult byId(int id)
    {
        for(ValidationResult res: ValidationResult.values()) {
            if(res.id == id) return res;
        }
        return ValidationResult.VALID;
    }

}
