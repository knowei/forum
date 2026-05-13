package com.forum.service.impl;

import com.forum.common.ResultCode;
import com.forum.entity.Resource;
import com.forum.entity.ResourceReport;
import com.forum.exception.BusinessException;
import com.forum.mapper.ResourceMapper;
import com.forum.mapper.ResourceReportMapper;
import com.forum.security.LoginUser;
import com.forum.service.ReportService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ReportServiceImpl implements ReportService {

    private final ResourceReportMapper reportMapper;
    private final ResourceMapper resourceMapper;

    public ReportServiceImpl(ResourceReportMapper reportMapper, ResourceMapper resourceMapper) {
        this.reportMapper = reportMapper;
        this.resourceMapper = resourceMapper;
    }

    @Override
    public void create(Long resourceId, String reason) {
        LoginUser loginUser = getLoginUser();
        Resource resource = resourceMapper.selectById(resourceId);
        if (resource == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "资源不存在");
        }

        ResourceReport report = new ResourceReport();
        report.setResourceId(resourceId);
        report.setUserId(loginUser.getId());
        report.setReason(reason);
        report.setStatus(0);
        reportMapper.insert(report);
    }

    @Override
    public List<ResourceReport> listAll() {
        return reportMapper.selectList(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ResourceReport>()
                .orderByDesc(ResourceReport::getCreateTime));
    }

    @Override
    @Transactional
    public void handle(Long id, Integer status, String handleNote) {
        ResourceReport report = reportMapper.selectById(id);
        if (report == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "举报不存在");
        }
        report.setStatus(status);
        report.setHandleNote(handleNote);
        reportMapper.updateById(report);
    }

    private LoginUser getLoginUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof LoginUser loginUser)) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
        return loginUser;
    }
}
