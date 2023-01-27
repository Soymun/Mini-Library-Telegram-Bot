package com.example.demo.Repository;

import com.example.demo.Entity.PersonBooks;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PersonBookRepo extends JpaRepository<PersonBooks, Long> {

}
