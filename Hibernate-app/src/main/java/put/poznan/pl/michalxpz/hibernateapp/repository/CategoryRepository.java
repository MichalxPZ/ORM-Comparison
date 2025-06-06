package put.poznan.pl.michalxpz.hibernateapp.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import put.poznan.pl.michalxpz.hibernateapp.model.Category;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Integer> {}
