package put.poznan.pl.michalxpz.mybatisapp.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.ibatis.mapping.FetchType;

import java.util.List;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class User {
    private Long id;
    private String name;
    private String email;
    private List<Order> orders;
}

