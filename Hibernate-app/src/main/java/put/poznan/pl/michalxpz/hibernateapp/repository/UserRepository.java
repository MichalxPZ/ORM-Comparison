package put.poznan.pl.michalxpz.hibernateapp.repository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import put.poznan.pl.michalxpz.hibernateapp.model.User;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

    @Query(value = "SELECT * FROM users ORDER BY RANDOM() LIMIT 1", nativeQuery = true)
    User findRandomUserPostgres();

    @Query(value = "SELECT * FROM users ORDER BY RAND() LIMIT 1", nativeQuery = true)
    User findRandomUserMySQL();
}

