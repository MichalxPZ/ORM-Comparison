package put.poznan.pl.michalxpz.jooqapp.repository;


import org.jooq.DSLContext;
import put.poznan.pl.michalxpz.generated.tables.records.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public class TagRepository {
    @Autowired private DSLContext dsl;

    public void saveAll(List<TagRecord> tags) {
        for (TagRecord tag : tags) {
            tag.store();
        }
    }
}

