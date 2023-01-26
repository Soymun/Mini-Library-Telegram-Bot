package com.example.demo.Service;

import com.example.demo.Entity.Books;
import com.example.demo.Entity.Books_;
import com.example.demo.Repository.BookRepo;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BookService {

    private final BookRepo bookRepo;

    @PersistenceContext
    EntityManager entityManager;

    @Autowired
    public BookService(BookRepo bookRepo) {
        this.bookRepo = bookRepo;
    }


    public String getBooks(List<String> list, int page){
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Books> cq = cb.createQuery(Books.class);
        Root<Books> root = cq.from(Books.class);
        StringBuilder stringBuilder = new StringBuilder("Ваш запрос:");
        cq.where(cb.and(cb.equal(root.get(Books_.checked), true)));
        for (String str : list){
            if(str.contains("=")) {
                String[] s = str.split("=");
                switch (s[0]) {
                    case "a" -> {
                        stringBuilder.append(" a=").append(s[1]);
                        cq.where(cb.and(cb.equal(root.get(Books_.author), s[1].replaceAll("\"", ""))));}
                    case "n" -> {
                        stringBuilder.append(" n=").append(s[1]);
                        cq.where(cb.and(cb.equal(root.get(Books_.nameBook), s[1].replaceAll("\"", ""))));}
                    case "g" -> {
                        stringBuilder.append(" g=").append(s[1]);
                        cq.where(cb.and(cb.equal(root.get(Books_.genre), s[1].replaceAll("\"", ""))));}
                }
            }

        }
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
        List<Books> list1 = entityManager.createQuery(cq).setFirstResult((page-1) * 5).setMaxResults(5).getResultList();
        for (Books books: list1){
            stringBuilder.append(books.toString().concat("\n"));
        }
        return stringBuilder.toString();
    }

    public int count(List<String> list){
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Books> cq = cb.createQuery(Books.class);
        Root<Books> root = cq.from(Books.class);
        cq.where(cb.and(cb.equal(root.get(Books_.checked), true)));
        for (String str : list){
            if(str.contains("=")) {
                String[] s = str.split("=");
                switch (s[0]) {
                    case "a" -> cq.where(cb.and(cb.equal(root.get(Books_.author), s[1].replaceAll("\"", "").toLowerCase().trim())));
                    case "n" -> cq.where(cb.and(cb.equal(root.get(Books_.nameBook), s[1].replaceAll("\"", "").toLowerCase().trim())));
                    case "g" -> cq.where(cb.and(cb.equal(root.get(Books_.genre), s[1].replaceAll("\"", "").toLowerCase().trim())));
                }
            }
        }
        cq.multiselect(
                root.get(Books_.id),
                root.get(Books_.author),
                root.get(Books_.nameBook),
                root.get(Books_.genre),
                root.get(Books_.urlToFile),
                root.get(Books_.checked)
        );
        return entityManager.createQuery(cq).getResultList().size();
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
}
