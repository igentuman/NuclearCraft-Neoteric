package igentuman.nc.hub.dto;

import com.google.gson.annotations.SerializedName;

public class CreateDesignResponseDto {
    public String id;
    public String name;
    public String author;
    public String version;
    public String channel;
    public String fuel;
    public String description;
    public int upvotes;
    public int downvotes;
    public double score;
    @SerializedName("cell_count")
    public int cellCount;
    @SerializedName("created_at")
    public long createdAt;
}
