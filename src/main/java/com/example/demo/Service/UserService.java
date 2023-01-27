package com.example.demo.Service;

import com.example.demo.Entity.Person;
import com.example.demo.Entity.PersonBooks;
import com.example.demo.Entity.Role;
import com.example.demo.Repository.PersonBookRepo;
import com.example.demo.Repository.UserRepo;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.Optional;

@Service
public class UserService {

    private final UserRepo userRepo;

    private final PersonBookRepo personBookRepo;

    public UserService(UserRepo userRepo, PersonBookRepo personBookRepo) {
        this.userRepo = userRepo;
        this.personBookRepo = personBookRepo;
    }

    public void saveUser(Message msg){
        if(userRepo.getPersonByUserId(msg.getFrom().getId()).isPresent()){
            throw new RuntimeException("User already exist");
        }
        Person person = new Person();
        person.setChatId(msg.getChatId());
        person.setUserId(msg.getFrom().getId());
        person.setUserName(msg.getFrom().getUserName());
        person.setRole(Role.USER);
        person.setRegister(false);
        userRepo.save(person);
    }

    public void acceptRegister(Long chatId){
        Optional<Person> person = userRepo.getPersonByChatId(chatId);
        if(person.isPresent()){
            Person person1 = person.get();
            person1.setRegister(true);
            userRepo.save(person1);
        }
    }

    public void personBooks(Long chatId, Long bookId){
        Optional<Person> person = userRepo.getPersonByChatId(chatId);
        if(person.isPresent()){
            Person person1 = person.get();
            PersonBooks personBooks = new PersonBooks();
            personBooks.setBookId(bookId);
            personBooks.setPersonId(person1.getId());
            personBookRepo.save(personBooks);
        }
    }

    public boolean isAdmin(Long chatId){
        Optional<Person> person = userRepo.getPersonByChatId(chatId);
        return person.filter(value -> value.getRole() == Role.ADMIN).isPresent();
    }
}
