package put.poznan.pl.michalxpz.mybatisapp.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class Tag {
    private Long id;
    private String name;
    private Set<Product> products = new HashSet<>();
}

