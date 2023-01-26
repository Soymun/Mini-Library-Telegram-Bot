package com.example.demo.Service;

import com.example.demo.Entity.Person;
import com.example.demo.Entity.Role;
import com.example.demo.Repository.UserRepo;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.Optional;

@Service
public class UserService {

    private final UserRepo userRepo;

    public UserService(UserRepo userRepo) {
        this.userRepo = userRepo;
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
}
