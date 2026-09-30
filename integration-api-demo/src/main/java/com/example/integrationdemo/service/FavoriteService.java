package com.example.integrationdemo.service;

import com.example.integrationdemo.common.exception.ApiException;
import com.example.integrationdemo.dao.FavoriteDao;
import com.example.integrationdemo.dao.entity.Favorite;
import com.example.integrationdemo.dto.FavoritePage;
import com.example.integrationdemo.dto.FavoriteResponse;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FavoriteService {
    private final FavoriteDao dao;
    private final BookService books;

    public FavoriteService(FavoriteDao dao, BookService books) {
        this.dao = dao;
        this.books = books;
    }

    // 외부 조회가 끝난 뒤 DAO의 DB 트랜잭션을 시작한다.
    public FavoriteResponse add(String isbn) {
        var book = books.findByIsbn(isbn);
        if (dao.existsByIsbn(book.isbn())) throw duplicate();
        try {
            return dao.insert(new Favorite(book)).toResponse();
        } catch (DuplicateKeyException ex) {
            // DAO 트랜잭션 롤백 후 확인하므로 동시 등록 충돌도 처리할 수 있다.
            if (dao.existsByIsbn(book.isbn())) throw duplicate();
            throw ex;
        }
    }

    @Transactional(readOnly = true, isolation = Isolation.REPEATABLE_READ)
    public FavoritePage list(int page, int size) {
        long total = dao.count();
        int totalPages = (int) Math.min(Integer.MAX_VALUE, total / size + (total % size == 0 ? 0 : 1));
        return new FavoritePage(dao.findPage(page, size).stream().map(Favorite::toResponse).toList(),
                page, size, total, totalPages);
    }

    public void delete(long id) {
        if (!dao.delete(id)) {
            throw new ApiException(HttpStatus.NOT_FOUND, "FAVORITE_NOT_FOUND", "즐겨찾기를 찾을 수 없습니다.");
        }
    }

    private ApiException duplicate() {
        return new ApiException(HttpStatus.CONFLICT, "FAVORITE_ALREADY_EXISTS", "이미 등록한 도서입니다.");
    }
}
