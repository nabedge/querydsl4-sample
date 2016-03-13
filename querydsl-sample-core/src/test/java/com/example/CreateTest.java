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
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Map;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { Config.class })
public class CreateTest {

    private final Logger log = LoggerFactory.getLogger(CreateTest.class);

    private final QBook qBook = QBook.book;

    @Autowired
    protected SQLQueryFactory sqlQueryFactory;

    @Autowired
    protected JdbcTemplate jdbcTemplate;

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

    @Test
    @Transactional
    public void multiple_ORMapper() {

        // insert by jdbcTemplate
        String insertSql = "insert into book (isbn,title,author_id,publish_date) " +
                "values ('001-0000000003', 'TThe Hound of the Baskervilles', 1, '1901-01-01')";
        jdbcTemplate.execute(insertSql);

        // update the above record by QueryDSL
        sqlQueryFactory
                .update(qBook)
                .set(qBook.title, "The Hound of the Baskervilles")
                .where(qBook.isbn.eq("001-0000000003"))
                .execute();

        // select by jdbcTemplate
        String selectSql = "select isbn from book where isbn='001-0000000003'";
        Map<String, Object> result = jdbcTemplate.queryForMap(selectSql);
        log.info(result.toString());

    }

}
