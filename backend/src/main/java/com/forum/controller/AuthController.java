package com.forum.controller;

import com.forum.common.Result;
import com.forum.dto.LoginDTO;
import com.forum.dto.RegisterDTO;
import com.forum.service.AuthService;
import com.forum.service.EmailService;
import com.forum.service.VerifyCodeService;
import com.forum.vo.LoginVO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final EmailService emailService;
    private final VerifyCodeService verifyCodeService;

    public AuthController(AuthService authService, EmailService emailService, VerifyCodeService verifyCodeService) {
        this.authService = authService;
        this.emailService = emailService;
        this.verifyCodeService = verifyCodeService;
    }

    @PostMapping("/register")
    public Result<Void> register(@Valid @RequestBody RegisterDTO registerDTO) {
        verifyCodeService.validate(registerDTO.getEmail(), registerDTO.getCode());
        authService.register(registerDTO);
        return Result.success();
    }

    @PostMapping("/send-code")
    public Result<Void> sendCode(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        if (email == null || !email.matches("^[\\w.-]+@[\\w.-]+\\.\\w{2,}$")) {
            return Result.failure(com.forum.common.ResultCode.BAD_REQUEST, "邮箱格式不正确");
        }
        String code = verifyCodeService.generateAndStore(email);
        emailService.sendVerifyCode(email, code);
        return Result.success();
    }

    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginDTO loginDTO) {
        return Result.success(authService.login(loginDTO));
    }
}
