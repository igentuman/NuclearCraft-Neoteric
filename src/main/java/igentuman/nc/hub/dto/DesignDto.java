package igentuman.nc.hub.dto;

import com.google.gson.annotations.SerializedName;

public class DesignDto {
    public String id;
    public String name;
    public String author;
    public String version;
    public String channel;
    public String description;
    public String design;
    public int upvotes;
    public int downvotes;
    public double score;
    @SerializedName("created_at")
    public long createdAt;
}
