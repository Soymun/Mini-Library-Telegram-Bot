package com.example.demo.Service;

import com.example.demo.Entity.Books;
import org.springframework.data.util.Pair;

import java.util.List;

public interface BookServiceInt {

     Pair<Integer, String> getBooks(List<String> list, int page);
     void afterCheck(Long bookId, boolean check);
     Books saveBook(List<String> strings);
     void updateBook(Books books);
    Pair<String, String> getBook(List<String> strings, boolean checked);
    Pair<Integer, String> myBook(Long chatId, int page);
    Pair<Integer, String> checkList(Long chatId, int page);
}
