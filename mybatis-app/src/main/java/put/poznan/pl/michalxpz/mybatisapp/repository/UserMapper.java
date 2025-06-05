package put.poznan.pl.michalxpz.mybatisapp.repository;

import org.apache.ibatis.annotations.Mapper;
import put.poznan.pl.michalxpz.mybatisapp.model.User;
import java.util.List;

public interface UserMapper {
    List<User> findAll();
    User findById(Long id);
    void insert(User user);
}

