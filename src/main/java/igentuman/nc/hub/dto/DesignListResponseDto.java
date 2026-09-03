package igentuman.nc.hub.dto;

import java.util.List;

public class DesignListResponseDto {
    public List<DesignListItemDto> designs;
    public int total;
    public int page;
    public int pageSize;
    public String sortBy;
}
