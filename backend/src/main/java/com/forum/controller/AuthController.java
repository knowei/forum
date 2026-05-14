package com.forum.controller;

import com.forum.common.Result;
import com.forum.common.ResultCode;
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
    private final com.forum.mapper.UserMapper userMapper;
    private final org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    public AuthController(AuthService authService, EmailService emailService,
                          VerifyCodeService verifyCodeService, com.forum.mapper.UserMapper userMapper,
                          org.springframework.security.crypto.password.PasswordEncoder passwordEncoder) {
        this.authService = authService;
        this.emailService = emailService;
        this.verifyCodeService = verifyCodeService;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
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

    @PostMapping("/forgot-password")
    public Result<Void> forgotPassword(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        if (email == null || !email.matches("^[\\w.-]+@[\\w.-]+\\.\\w{2,}$")) {
            return Result.failure(ResultCode.BAD_REQUEST, "邮箱格式不正确");
        }
        if (userMapper.findByEmail(email) == null) {
            return Result.failure(ResultCode.NOT_FOUND, "该邮箱未注册");
        }
        String code = verifyCodeService.generateAndStore(email);
        emailService.sendVerifyCode(email, code);
        return Result.success();
    }

    @PostMapping("/reset-password")
    public Result<Void> resetPassword(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        String code = body.get("code");
        String newPassword = body.get("newPassword");
        if (email == null || code == null || newPassword == null) {
            return Result.failure(ResultCode.BAD_REQUEST, "参数不完整");
        }
        verifyCodeService.validate(email, code);
        com.forum.entity.User user = userMapper.findByEmail(email);
        if (user == null) {
            return Result.failure(ResultCode.NOT_FOUND, "用户不存在");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userMapper.updateById(user);
        return Result.success();
    }
}
