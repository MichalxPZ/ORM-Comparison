package put.poznan.pl.michalxpz.jooqapp.model;

import lombok.Getter;
import lombok.Setter;
import put.poznan.pl.michalxpz.generated.tables.records.*;
import java.time.LocalDateTime;
import java.util.List;

@Setter
@Getter
public class OrderWithItems {
    // Getters and setters
    private Long id;
    private LocalDateTime orderDate;
    private Long userId;
    private List<ProductRecord> products;

}
