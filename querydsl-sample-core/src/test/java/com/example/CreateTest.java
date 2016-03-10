package com.example;

import com.example.db.querydsl.gen.Book;
import com.example.db.querydsl.gen.QBook;
import com.querydsl.sql.SQLQueryFactory;
import com.querydsl.sql.dml.SQLInsertClause;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { Config.class })
public class CreateTest {

    private final Logger log = LoggerFactory.getLogger(CreateTest.class);

    private final QBook qBook = QBook.book;

    @Autowired
    protected SQLQueryFactory sqlQueryFactory;

    // =======================================================
    @Test
    @Transactional()
    public void insert1() {
        Book book = new Book();
        book.setIsbn("999-9999999999");
        book.setAuthorId(4);
        book.setTitle("進撃の巨人 （１）");
        java.sql.Date publishDate = java.sql.Date.valueOf(LocalDate.parse("2010-03-17"));
        book.setPublishDate(publishDate);
        sqlQueryFactory.insert(qBook).populate(book).execute();
    }

    // =======================================================
    @Test
    @Transactional
    public void insert2() {
        java.sql.Date publishDate = java.sql.Date.valueOf(LocalDate.parse("2010-03-17"));
        SQLInsertClause insert = sqlQueryFactory.insert(qBook)
                .set(qBook.isbn, "999-9999999999")
                .set(qBook.title, "Attack of Titan vol １")
                .set(qBook.authorId, 4)
                .set(qBook.publishDate, publishDate);
        insert.execute();
    }
}
