package com.forum.vo;

import lombok.Data;

@Data
public class UserVO {

    private Long id;
    private String username;
    private String nickname;
    private String email;
    private String avatar;
    private String bgImage;
    private String bio;
    private Integer role;
}
