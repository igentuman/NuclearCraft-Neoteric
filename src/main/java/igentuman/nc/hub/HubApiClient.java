package igentuman.nc.hub;

import com.google.gson.Gson;
import igentuman.nc.NuclearCraft;
import igentuman.nc.hub.dto.ChallengeDto;
import igentuman.nc.hub.dto.CreateDesignRequestDto;
import igentuman.nc.hub.dto.CreateDesignResponseDto;
import igentuman.nc.hub.dto.DesignDto;
import igentuman.nc.hub.dto.DesignListResponseDto;
import igentuman.nc.hub.dto.VoteResultDto;

import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public final class HubApiClient {

    private static final Gson GSON = new Gson();

    private HubApiClient() {}

    public static CompletableFuture<HubResult<ChallengeDto>> getChallenge() {
        return HubHttpClient.get(HubConfig.baseUrl() + "/api/v1/challenge")
                .handle((response, error) -> parse(response, error, ChallengeDto.class));
    }

    public static CompletableFuture<HubResult<DesignListResponseDto>> listDesigns(
            int page, int limit, String sortBy, String channel, String version, String author) {
        StringBuilder url = new StringBuilder(HubConfig.baseUrl())
                .append("/api/v1/designs?page=").append(page)
                .append("&limit=").append(limit)
                .append("&sortBy=").append(encode(sortBy))
                .append("&channel=").append(encode(channel));
        if (version != null && !version.isEmpty()) {
            url.append("&version=").append(encode(version));
        }
        if (author != null && !author.isEmpty()) {
            url.append("&author=").append(encode(author));
        }
        return HubHttpClient.get(url.toString())
                .handle((response, error) -> parse(response, error, DesignListResponseDto.class));
    }

    public static CompletableFuture<HubResult<DesignDto>> getDesign(String id) {
        return HubHttpClient.get(HubConfig.baseUrl() + "/api/v1/designs/" + encode(id))
                .handle((response, error) -> parse(response, error, DesignDto.class));
    }

    public static CompletableFuture<HubResult<CreateDesignResponseDto>> createDesign(CreateDesignRequestDto dto) {
        return HubHttpClient.post(HubConfig.baseUrl() + "/api/v1/designs", GSON.toJson(dto))
                .handle((response, error) -> parse(response, error, CreateDesignResponseDto.class));
    }

    public static CompletableFuture<HubResult<VoteResultDto>> vote(String id, int vote, String playerUuid) {
        String body = GSON.toJson(Map.of("vote", vote, "playerUuid", playerUuid));
        return HubHttpClient.post(HubConfig.baseUrl() + "/api/v1/designs/" + encode(id) + "/vote", body)
                .handle((response, error) -> parse(response, error, VoteResultDto.class));
    }

    private static <T> HubResult<T> parse(HttpResponse<String> response, Throwable error, Class<T> type) {
        if (error != null) {
            Throwable cause = error.getCause() != null ? error.getCause() : error;
            NuclearCraft.LOGGER.error("Designs Hub request failed", cause);
            return new HubResult.NetworkError<>(cause.getClass().getSimpleName() + ": " + cause.getMessage());
        }
        int status = response.statusCode();
        if (status >= 200 && status < 300) {
            return new HubResult.Success<>(GSON.fromJson(response.body(), type));
        }
        String code = extractErrorCode(response.body());
        return switch (status) {
            case 429 -> new HubResult.RateLimited<>(parseRetryAfter(response));
            case 409 -> new HubResult.Conflict<>(code);
            case 403 -> new HubResult.Forbidden<>(code);
            case 404 -> new HubResult.NotFound<>();
            case 400 -> new HubResult.ValidationError<>(code);
            default -> new HubResult.NetworkError<>(code);
        };
    }

    private static String extractErrorCode(String body) {
        try {
            Map<?, ?> map = GSON.fromJson(body, Map.class);
            Object error = map == null ? null : map.get("error");
            return error == null ? "unknown_error" : error.toString();
        } catch (RuntimeException e) {
            return "unknown_error";
        }
    }

    private static int parseRetryAfter(HttpResponse<String> response) {
        return response.headers().firstValue("Retry-After")
                .map(v -> {
                    try {
                        return Integer.parseInt(v);
                    } catch (NumberFormatException e) {
                        return 0;
                    }
                })
                .orElse(0);
    }

    private static String encode(String value) {
        return java.net.URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
