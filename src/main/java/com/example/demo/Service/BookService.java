package com.example.demo.Service;

import com.example.demo.Entity.*;
import com.example.demo.Entity.Books_;
import com.example.demo.Entity.PersonBooks_;
import com.example.demo.Entity.Person_;
import com.example.demo.Repository.BookRepo;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class BookService {

    private final BookRepo bookRepo;

    @PersistenceContext
    EntityManager entityManager;

    @Autowired
    public BookService(BookRepo bookRepo) {
        this.bookRepo = bookRepo;
    }


    public Pair<Integer, String> getBooks(List<String> list, int page){
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Books> cq = cb.createQuery(Books.class);
        Root<Books> root = cq.from(Books.class);
        StringBuilder stringBuilder = new StringBuilder("Ваш запрос:");
        Predicate predicate = cb.equal(root.get(Books_.checked), true);
        for (String str : list){
            if(str.contains("=")) {
                String[] s = str.split("=");
                String o = s[1].replaceAll("\"", "").trim().toLowerCase();
                switch (s[0]) {
                    case "a" -> {
                        stringBuilder.append(" a=").append(s[1]);
                        predicate = cb.and(cb.equal(root.get(Books_.author), o), predicate);}
                    case "n" -> {
                        stringBuilder.append(" n=").append(s[1]);
                        predicate = cb.and(cb.equal(root.get(Books_.nameBook), o), predicate);}
                    case "g" -> {
                        stringBuilder.append(" g=").append(s[1]);
                        predicate = cb.and(cb.equal(root.get(Books_.genre), o),predicate);}
                }
            }

        }
        cq.where(predicate);
        stringBuilder.append("\nСтраница: ").append(page);
        stringBuilder.append("\nПо вашему запросу найденно:\n\n");
        cq.orderBy(cb.asc(root.get(Books_.id)));
        cq.multiselect(
                root.get(Books_.id),
                root.get(Books_.author),
                root.get(Books_.nameBook),
                root.get(Books_.genre),
                root.get(Books_.urlToFile),
                root.get(Books_.checked)
        );
        List<Books> list1 = entityManager.createQuery(cq).getResultList();
        int count = list1.size();
        if(count == 0){
            return Pair.of(0, "Ничего не найденно");
        }
        else {
            list1 = list1.stream().skip((page - 1) * 5L).limit(5).toList();
            for (Books books : list1) {
                stringBuilder.append(books.toString().concat("\n"));
            }
            return Pair.of(count, stringBuilder.toString());
        }
    }

    public void afterCheck(Long bookId, boolean check){
        Optional<Books> books = bookRepo.getBooksById(bookId);
        if(books.isPresent()){
            Books books1 = books.get();
            if(check) {
                books1.setChecked(true);
                bookRepo.save(books1);
            }
            else {
                bookRepo.deleteById(bookId);
            }
        }
    }

    public Books saveBook(List<String> strings){
        Books books = new Books();
        for (String str : strings){
            if(str.contains("=")) {
                String[] s = str.split("=");
                switch (s[0]) {
                    case "a" -> books.setAuthor(s[1].replaceAll("\"", "").toLowerCase().trim());
                    case "n" -> books.setNameBook(s[1].replaceAll("\"", "").toLowerCase().trim());
                    case "g" -> books.setGenre(s[1].replaceAll("\"", "").toLowerCase().trim());
                }
            }
        }
        books.setChecked(false);
        return bookRepo.save(books);
    }

    public void updateBook(Books books){
        bookRepo.save(books);
    }

    public Pair<String, String> getBook(List<String> strings, boolean checked){
        if(strings.size() == 2){
            Long bookId = Long.parseLong(strings.get(1).split("=")[1]);
            Books books = bookRepo.getBooksByIdAndChecked(bookId, checked).orElseThrow((()-> new RuntimeException("Книга не найденна")));
            if(books.getUrlToFile() == null){
                bookRepo.deleteById(bookId);
                throw new RuntimeException("Книга не найденна");
            }
            return Pair.of("Ваша книга:\n" + books, books.getUrlToFile());
        }
        throw new RuntimeException("Книга не найденна");
    }


    public Pair<Integer, String> myBook(Long chatId, int page){
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Books> cq = cb.createQuery(Books.class);
        Root<PersonBooks> root = cq.from(PersonBooks.class);
        Join<PersonBooks, Person> join = root.join(PersonBooks_.PERSON);
        Join<PersonBooks, Books> join1 = root.join(PersonBooks_.BOOKS);
        cq.where(cb.equal(join.get(Person_.chatId), chatId));
        StringBuilder stringBuilder = new StringBuilder("Мои книги:");
        stringBuilder.append("\nСтраница: ").append(page);
        stringBuilder.append("\nПо вашему запросу найденно:\n\n");
        cq.orderBy(cb.asc(join1.get(Books_.id)));
        cq.multiselect(
                join1.get(Books_.id),
                join1.get(Books_.author),
                join1.get(Books_.nameBook),
                join1.get(Books_.genre),
                join1.get(Books_.urlToFile),
                join1.get(Books_.checked)
        );
        List<Books> list1 = entityManager.createQuery(cq).getResultList();
        int count = list1.size();
        if(count == 0){
            return Pair.of(0, "Ничего не найденно");
        }
        else {
            list1 = list1.stream().skip((page - 1) * 5L).limit(5).toList();
            for (Books books : list1) {
                stringBuilder.append(books.toString().concat("\n"));
            }
            return Pair.of(count, stringBuilder.toString());
        }
    }

    public Pair<Integer, String> checkList(Long chatId, int page){
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Books> cq = cb.createQuery(Books.class);
        Root<Books> root = cq.from(Books.class);
        cq.where(cb.equal(root.get(Books_.checked), false));
        StringBuilder stringBuilder = new StringBuilder("Список книг для проверки: ");
        stringBuilder.append("\nСтраница: ").append(page);
        stringBuilder.append("\nПо вашему запросу найденно:\n\n");
        cq.orderBy(cb.asc(root.get(Books_.id)));
        cq.multiselect(
                root.get(Books_.id),
                root.get(Books_.author),
                root.get(Books_.nameBook),
                root.get(Books_.genre),
                root.get(Books_.urlToFile),
                root.get(Books_.checked)
        );
        List<Books> list1 = entityManager.createQuery(cq).getResultList();
        int count = list1.size();
        if(count == 0){
            return Pair.of(0, "Ничего не найденно");
        }
        else {
            list1 = list1.stream().skip((page - 1) * 5L).limit(5).toList();
            for (Books books : list1) {
                stringBuilder.append(books.toString().concat("\n"));
            }
            return Pair.of(count, stringBuilder.toString());
        }
    }
}
