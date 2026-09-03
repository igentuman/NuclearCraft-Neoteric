package igentuman.nc.hub;

public sealed interface HubResult<T> {

    record Success<T>(T value) implements HubResult<T> {}

    record RateLimited<T>(int retryAfterSeconds) implements HubResult<T> {}

    record Conflict<T>(String code) implements HubResult<T> {}

    record ValidationError<T>(String code) implements HubResult<T> {}

    record NotFound<T>() implements HubResult<T> {}

    record Forbidden<T>(String code) implements HubResult<T> {}

    record NetworkError<T>(String message) implements HubResult<T> {}
}
