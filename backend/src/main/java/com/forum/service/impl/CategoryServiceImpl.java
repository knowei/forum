package com.forum.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.forum.common.ResultCode;
import com.forum.entity.Category;
import com.forum.exception.BusinessException;
import com.forum.mapper.CategoryMapper;
import com.forum.service.CategoryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CategoryServiceImpl implements CategoryService {

    private final CategoryMapper categoryMapper;

    public CategoryServiceImpl(CategoryMapper categoryMapper) {
        this.categoryMapper = categoryMapper;
    }

    @Override
    public List<Category> listEnabled() {
        return categoryMapper.selectList(new LambdaQueryWrapper<Category>()
                .eq(Category::getStatus, 1)
                .orderByAsc(Category::getSort)
                .orderByAsc(Category::getId));
    }

    @Override
    public List<Category> listAll() {
        return categoryMapper.selectList(new LambdaQueryWrapper<Category>()
                .orderByAsc(Category::getSort)
                .orderByAsc(Category::getId));
    }

    @Override
    @Transactional
    public void create(String name, String description, Integer sort) {
        Category category = new Category();
        category.setName(name);
        category.setDescription(description);
        category.setSort(sort == null ? 0 : sort);
        category.setStatus(1);
        categoryMapper.insert(category);
    }

    @Override
    @Transactional
    public void update(Integer id, String name, String description, Integer sort, Integer status) {
        Category category = categoryMapper.selectById(id);
        if (category == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "分类不存在");
        }
        category.setName(name);
        category.setDescription(description);
        category.setSort(sort == null ? 0 : sort);
        category.setStatus(status == null ? 1 : status);
        categoryMapper.updateById(category);
    }

    @Override
    @Transactional
    public void delete(Integer id) {
        if (categoryMapper.selectById(id) == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "分类不存在");
        }
        categoryMapper.deleteById(id);
    }
}
