package com.forum.service;

import com.forum.entity.ResourceReport;

import java.util.List;

public interface ReportService {

    void create(Long resourceId, String reason);

    List<ResourceReport> listAll();

    void handle(Long id, Integer status, String handleNote);
}
