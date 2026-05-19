package com.forum.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserUpdateDTO {

    @NotBlank
    @Size(max = 50)
    private String nickname;

    @Email
    @NotBlank
    @Size(max = 100)
    private String email;

    @Size(max = 500)
    private String bio;
}
