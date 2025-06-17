package put.poznan.pl.michalxpz.mybatisapp.repository;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import put.poznan.pl.michalxpz.mybatisapp.model.User;
import java.util.List;

public interface UserMapper {
    List<User> findAll();
    User findById(Long id);
    void insert(User user);

    @Select({
            "<script>",
            "SELECT * FROM users",
            "ORDER BY",
            "<choose>",
            "  <when test='db == \"postgresql\"'>RANDOM()</when>",
            "  <otherwise>RAND()</otherwise>",
            "</choose>",
            "LIMIT 1",
            "</script>"
    })
    User findRandomUser(@Param("db") String db);
}

