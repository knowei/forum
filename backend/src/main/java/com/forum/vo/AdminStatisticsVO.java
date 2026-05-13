package com.forum.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AdminStatisticsVO {

    private Long userCount;
    private Long resourceCount;
    private Long pendingResourceCount;
    private Long onlineCount;
}
