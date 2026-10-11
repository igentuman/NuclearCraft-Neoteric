package igentuman.nc.compat.computer;

@FunctionalInterface
public interface ComputerErrors {

    ComputerErrors DEFAULT = IllegalArgumentException::new;

    RuntimeException create(String message);
}
