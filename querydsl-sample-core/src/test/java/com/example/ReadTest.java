package com.example;

import com.example.db.querydsl.gen.Author;
import com.example.db.querydsl.gen.Book;
import com.example.db.querydsl.gen.QAuthor;
import com.example.db.querydsl.gen.QBook;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.sql.SQLBindings;
import com.querydsl.sql.SQLExpressions;
import com.querydsl.sql.SQLQuery;
import com.querydsl.sql.SQLQueryFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.Assert.assertNull;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {Config.class})
@Transactional
public class ReadTest {

    private final Logger log = LoggerFactory.getLogger(ReadTest.class);

    private final QAuthor qAuthor = QAuthor.author;
    private final QBook qBook = QBook.book;

    @Autowired
    protected SQLQueryFactory sqlQueryFactory;

    @Test
    public void simpleFetch() {
        Book book = sqlQueryFactory
                .select(qBook)
                .from(qBook)
                .where(qBook.isbn.eq("001-0000000001"))
                .fetchOne();
        log.info(book.toString());
    }

    @Test
    public void simpleFetch2() {
        SQLQuery<Book> query = sqlQueryFactory.select(qBook);
        query.from(qBook);
        query.where(qBook.isbn.eq("001-0000000001"));
        Book book = query.fetchOne();
        log.info(book.toString());
    }

    @Test
    public void simpleFetch3() {
        SQLQuery<Tuple> query = sqlQueryFactory.select(qBook.isbn, qBook.title);
        query.from(qBook);
        query.where(qBook.isbn.eq("001-0000000001"));
        Tuple tuple = query.fetchOne();
        log.info("isbn = {}, title = {}", tuple.get(qBook.isbn), tuple.get(qBook.title));
    }

    @Test
    public void simpleFetchList() {
        List<Book> books = sqlQueryFactory
                .select(qBook)
                .from(qBook)
                .orderBy(qBook.publishDate.asc())
                .fetch();
        books.forEach(book -> log.info(book.toString()));
    }

    @Test
    public void simpleFetchList_logging() {
        SQLQuery<Book> query = sqlQueryFactory
                .select(qBook)
                .from(qBook);

        // You can get sql statement.
        SQLBindings sqlBindings = query.getSQL();
        log.debug("SQL = {}", sqlBindings.getSQL());

        query.fetch();
    }

    @Test
    public void simpleJoin() {
        List<Book> books = sqlQueryFactory
                .select(qBook)
                .from(qBook, qAuthor)
                .where(
                    qBook.authorId.eq(qAuthor.id)
                    .and(qAuthor.name.eq("Haruki Murakami"))
                )
                .fetch();
        books.forEach(book -> log.info(book.toString()));
    }

    @Test
    public void simpleJoin2() {
        List<Book> books = sqlQueryFactory
                .select(qBook)
                .from(qBook, qAuthor)
                // two "whare" clause becomes to " foo AND bar "
                .where(qBook.authorId.eq(qAuthor.id))
                .where(qAuthor.name.eq("Haruki Murakami"))
                .fetch();
        books.forEach(book -> log.info(book.toString()));
    }

    @Test
    public void select21() {
        SQLQuery<Book> query = sqlQueryFactory.select(qBook);
        query.from(qBook);
        // The case that you need special dialect.
        // (supposing that "BETWEEN" is a dialect.)
        String template = "publish_date between {0} and {1}";
        query.where(Expressions.booleanTemplate(template, "1980-01-01", "2000-12-31"));
        List<Book> books = query.fetch();
        books.forEach(book -> log.info(book.toString()));
    }

    @Test
    public void select22() {
        SQLQuery<BookAndAuthorDTO> query = sqlQueryFactory.select(
                Projections.bean(BookAndAuthorDTO.class,
                        qBook.isbn,
                        qBook.title,
                        qAuthor.name.as("authorName"),
                        qBook.publishDate));
        query.from(qBook, qAuthor);
        query.where(qBook.authorId.eq(qAuthor.id));
        query.orderBy(qBook.publishDate.asc(), qAuthor.id.asc());

        List<BookAndAuthorDTO> list = query.fetch();
        list.stream().forEach(dto -> {
            log.debug("-------");
            log.debug("isbn: {}", dto.getIsbn());
            log.debug("title: {}", dto.getTitle());
            log.debug("author: {}", dto.getAuthorName());
            log.debug("publishDate: {}", dto.getPublishDate());
        });
    }

    @Test
    public void select23() {
        SQLQuery<BookAndAuthorDTO> query = sqlQueryFactory.select(
                Projections.bean(BookAndAuthorDTO.class,
                        qBook.isbn,
                        qBook.title,
                        qAuthor.name.as("authorName"),
                        qBook.publishDate));
        query.from(qBook);
        query.innerJoin(qAuthor).on(qAuthor.id.eq(qBook.authorId));
        query.where(qBook.authorId.eq(qAuthor.id));
        query.orderBy(qBook.publishDate.asc(), qAuthor.id.asc());

        List<BookAndAuthorDTO> list = query.fetch();
        list.stream().forEach(e -> {
            log.debug("---------");
            log.debug("isbn: {}", e.getIsbn());
            log.debug("title: {}", e.getTitle());
            log.debug("author: {}", e.getAuthorName());
            log.debug("publishDate: {}", e.getPublishDate());
        });
    }

    @Test
    public void select_no_hit() {
        SQLQuery<Book> query = sqlQueryFactory.select(qBook);
        query.from(qBook);
        query.where(qBook.isbn.eq("foobar"));
        Book book = query.fetchOne();
        assertNull(book);
    }

    @Test
    public void subQuerySample() {
        List<Author> list = sqlQueryFactory.select(qAuthor)
                .from(qAuthor)
                .where(SQLExpressions.select(qBook.count()).from(qBook).where(qBook.authorId.eq(qAuthor.id)).gt(0L))
                .fetch();
        log.info(list.toString());
    }

}
