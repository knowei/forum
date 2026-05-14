package com.forum.controller;

import com.forum.common.Result;
import com.forum.service.NoteService;
import com.forum.vo.NoteCommentVO;
import com.forum.vo.NoteVO;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/note")
public class NoteController {

    private final NoteService noteService;

    public NoteController(NoteService noteService) {
        this.noteService = noteService;
    }

    @GetMapping("/list")
    public Result<List<NoteVO>> list() {
        return Result.success(noteService.listPublished());
    }

    @GetMapping("/{id}")
    public Result<NoteVO> detail(@PathVariable Long id) {
        return Result.success(noteService.getDetail(id));
    }

    @PostMapping
    public Result<Void> create(@RequestBody Map<String, String> body) {
        noteService.create(body.get("title"), body.get("content"), body.get("images"));
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        noteService.delete(id);
        return Result.success();
    }

    @PostMapping("/{id}/like")
    public Result<Void> like(@PathVariable Long id) {
        noteService.like(id);
        return Result.success();
    }

    @DeleteMapping("/{id}/like")
    public Result<Void> unlike(@PathVariable Long id) {
        noteService.unlike(id);
        return Result.success();
    }

    @GetMapping("/{id}/comments")
    public Result<List<NoteCommentVO>> comments(@PathVariable Long id) {
        return Result.success(noteService.listComments(id));
    }

    @PostMapping("/{id}/comment")
    public Result<Void> createComment(@PathVariable Long id,
                                       @RequestParam String content,
                                       @RequestParam(required = false) Long parentId) {
        noteService.createComment(id, content, parentId);
        return Result.success();
    }

    @DeleteMapping("/comment/{id}")
    public Result<Void> deleteComment(@PathVariable Long id) {
        noteService.deleteComment(id);
        return Result.success();
    }
}
