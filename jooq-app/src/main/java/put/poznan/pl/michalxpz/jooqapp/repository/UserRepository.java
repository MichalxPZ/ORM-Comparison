package put.poznan.pl.michalxpz.jooqapp.repository;


import org.jooq.DSLContext;
import put.poznan.pl.michalxpz.generated.tables.pojos.Users;
import put.poznan.pl.michalxpz.generated.tables.records.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import java.util.List;

import static put.poznan.pl.michalxpz.generated.Tables.*;

@Repository
public class UserRepository {
    @Autowired private DSLContext dsl;

    public List<Users> findAll() {
        return dsl.selectFrom(USERS)
                .fetchInto(Users.class);
    }

    public Users findById(Long userId) {
        Users user = dsl.selectFrom(USERS)
                .where(USERS.ID.eq(Math.toIntExact(userId)))
                .fetchOneInto(Users.class);
        if (user == null) {
            throw new IllegalArgumentException("User not found");
        }
        return user;
    }

    public void saveAll(List<UsersRecord> users) {
        // Batch insert (or loop insert) of users
        for (UsersRecord user : users) {
            user.store();
        }
    }
}

