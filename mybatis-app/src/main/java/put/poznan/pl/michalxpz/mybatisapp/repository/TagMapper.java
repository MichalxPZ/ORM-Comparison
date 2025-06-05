package put.poznan.pl.michalxpz.mybatisapp.repository;

import org.apache.ibatis.annotations.Mapper;
import put.poznan.pl.michalxpz.mybatisapp.model.Tag;
import java.util.List;

public interface TagMapper {
    List<Tag> findAll();
    Tag findById(Long id);
    void insert(Tag tag);
}

