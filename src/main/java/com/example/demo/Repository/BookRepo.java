package com.example.demo.Repository;

import com.example.demo.Entity.Books;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BookRepo extends JpaRepository<Books, Long> {
    Optional<Books> getBooksByIdAndChecked(Long id, boolean checked);

    Optional<Books> getBooksById(Long id);
}
