package com.forum.controller;

import com.forum.common.Result;
import com.forum.service.CommentService;
import com.forum.vo.CommentVO;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/comment")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @GetMapping("/list")
    public Result<List<CommentVO>> list(@RequestParam Long resourceId) {
        return Result.success(commentService.listByResource(resourceId));
    }

    @PostMapping
    public Result<Void> create(@RequestParam Long resourceId,
                                @RequestParam String content,
                                @RequestParam(required = false) Long parentId) {
        commentService.create(resourceId, content, parentId);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        commentService.delete(id);
        return Result.success();
    }
}
