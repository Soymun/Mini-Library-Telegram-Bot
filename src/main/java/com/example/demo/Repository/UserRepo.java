package com.example.demo.Repository;

import com.example.demo.Entity.Person;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepo extends JpaRepository<Person, Long> {

    Optional<Person> getPersonByUserId(Long id);

    Optional<Person> getPersonByChatId(Long id);
}
