package put.poznan.pl.michalxpz.hibernateapp.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import put.poznan.pl.michalxpz.hibernateapp.model.Tag;

@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {}

