package com.forum.service;

import com.forum.entity.Category;

import java.util.List;

public interface CategoryService {

    List<Category> listEnabled();

    List<Category> listAll();

    void create(String name, String description, Integer sort);

    void update(Integer id, String name, String description, Integer sort, Integer status);

    void delete(Integer id);
}
