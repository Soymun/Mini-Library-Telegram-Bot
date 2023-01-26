package com.example.demo.Repository;

import com.example.demo.Entity.Books;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookRepo extends JpaRepository<Books, Long> {

}
