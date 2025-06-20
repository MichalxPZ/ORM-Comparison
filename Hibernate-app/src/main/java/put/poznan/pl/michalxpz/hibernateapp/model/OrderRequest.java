package put.poznan.pl.michalxpz.hibernateapp.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class OrderRequest {
    private Integer userId;
    private List<Integer> productIds;
}