package put.poznan.pl.michalxpz.jooqapp.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class OrderRequest {
    private Long userId;
    private List<Long> productIds;
}