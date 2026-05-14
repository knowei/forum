package com.forum.controller;

import com.forum.common.PageResult;
import com.forum.common.Result;
import com.forum.dto.PageQueryDTO;
import com.forum.dto.ResourceCreateDTO;
import com.forum.service.ResourceService;
import com.forum.vo.ResourceDetailVO;
import com.forum.vo.ResourceListItemVO;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@Validated
@RequestMapping("/api/resource")
public class ResourceController {

    private final ResourceService resourceService;
    private final com.forum.service.ReportService reportService;

    public ResourceController(ResourceService resourceService, com.forum.service.ReportService reportService) {
        this.resourceService = resourceService;
        this.reportService = reportService;
    }

    @PostMapping
    public Result<Void> create(@Valid @RequestBody ResourceCreateDTO resourceCreateDTO) {
        resourceService.create(resourceCreateDTO);
        return Result.success();
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody ResourceCreateDTO resourceCreateDTO) {
        resourceService.update(id, resourceCreateDTO);
        return Result.success();
    }

    @GetMapping("/list")
    public Result<PageResult<ResourceListItemVO>> list(PageQueryDTO pageQueryDTO) {
        return Result.success(resourceService.listPublished(pageQueryDTO));
    }

    @GetMapping("/my")
    public Result<List<ResourceListItemVO>> myResources() {
        return Result.success(resourceService.listMyResources());
    }

    @GetMapping("/collections")
    public Result<List<ResourceListItemVO>> myCollections() {
        return Result.success(resourceService.listMyCollections());
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        resourceService.deleteById(id);
        return Result.success();
    }

    @GetMapping("/{id}")
    public Result<ResourceDetailVO> detail(@PathVariable Long id) {
        return Result.success(resourceService.getDetail(id));
    }

    @PostMapping("/{id}/like")
    public Result<Void> like(@PathVariable Long id) {
        resourceService.like(id);
        return Result.success();
    }

    @DeleteMapping("/{id}/like")
    public Result<Void> unlike(@PathVariable Long id) {
        resourceService.unlike(id);
        return Result.success();
    }

    @PostMapping("/{id}/collect")
    public Result<Void> collect(@PathVariable Long id) {
        resourceService.collect(id);
        return Result.success();
    }

    @DeleteMapping("/{id}/collect")
    public Result<Void> uncollect(@PathVariable Long id) {
        resourceService.uncollect(id);
        return Result.success();
    }

    @PostMapping("/{id}/report")
    public Result<Void> report(@PathVariable Long id, @RequestBody Map<String, String> body) {
        reportService.create(id, body.get("reason"));
        return Result.success();
    }
}
