package com.example.demo.Entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PersonBooks {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "personId")
    private Long personId;

    @Column(name = "bookId")
    private Long bookId;

    @ManyToOne
    @JoinColumn(name = "personId", insertable = false, updatable = false)
    private Person person;

    @ManyToOne
    @JoinColumn(name = "bookId", insertable = false, updatable = false)
    private Books books;
}
