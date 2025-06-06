package put.poznan.pl.michalxpz.jooqapp.repository;


import org.jooq.DSLContext;
import put.poznan.pl.michalxpz.generated.tables.records.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public class CategoryRepository {
    @Autowired private DSLContext dsl;

    public void saveAll(List<CategoriesRecord> categories) {
        for (CategoriesRecord category : categories) {
            category.store();
        }
    }
}

