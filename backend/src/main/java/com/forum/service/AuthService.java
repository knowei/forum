package com.forum.service;

import com.forum.dto.LoginDTO;
import com.forum.dto.RegisterDTO;
import com.forum.vo.LoginVO;

public interface AuthService {

    void register(RegisterDTO registerDTO);

    LoginVO login(LoginDTO loginDTO);
}
