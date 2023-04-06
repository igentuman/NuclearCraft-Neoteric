package igentuman.nc.phosphophyllite.multiblock2.validated;

import igentuman.nc.phosphophyllite.multiblock2.ValidationException;
import igentuman.nc.util.annotation.NonnullDefault;

@NonnullDefault
public interface IValidatedMultiblockControllerModule {
    
    default void onStateTransition(IValidatedMultiblock.AssemblyState oldAssemblyState, IValidatedMultiblock.AssemblyState newAssemblyState) {
    }
    
    /**
     * the three validation stages are for ordering validation steps to ensure that the most expensive checks are only done after everything else has passed
     * put the cheapest checks in stage 1, most expensive in 3, anything in the middle in stage 2
     *
     * @throws ValidationException : validation failed if
     */
    default void validateStage1() throws ValidationException {
    }
    
    default void validateStage2() throws ValidationException {
    }
    
    default void validateStage3() throws ValidationException {
    }
    
    default boolean canValidate() {
        return true;
    }
    
    default boolean canTick(){
        return true;
    }
}
