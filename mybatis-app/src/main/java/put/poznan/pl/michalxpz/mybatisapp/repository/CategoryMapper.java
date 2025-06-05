package put.poznan.pl.michalxpz.mybatisapp.repository;

import put.poznan.pl.michalxpz.mybatisapp.model.Category;

import java.util.List;

public interface CategoryMapper {
    List<Category> findAll();
    Category findById(Long id);
    void insert(Category category);
}
