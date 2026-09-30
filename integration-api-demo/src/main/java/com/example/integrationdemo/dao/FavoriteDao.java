package com.example.integrationdemo.dao;

import com.example.integrationdemo.dao.entity.Favorite;
import com.example.integrationdemo.dao.mapper.FavoriteMapper;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class FavoriteDao {
    private final FavoriteMapper mapper;

    public FavoriteDao(FavoriteMapper mapper) { this.mapper = mapper; }

    @Transactional
    public Favorite insert(Favorite favorite) {
        mapper.insertFavorite(favorite);
        if (!favorite.getAuthors().isEmpty()) mapper.insertAuthors(favorite);
        return favorite;
    }

    public boolean existsByIsbn(String isbn) { return mapper.existsByIsbn(isbn); }
    public long count() { return mapper.count(); }
    public Optional<Favorite> findById(long id) { return Optional.ofNullable(mapper.findById(id)); }
    public List<Favorite> findPage(int page, int size) {
        return mapper.findPage((long) page * size, size);
    }

    @Transactional
    public boolean delete(long id) {
        // 두 요청이 동시에 같은 도서를 삭제해도 결과를 정확히 판단한다.
        if (mapper.lockById(id) == null) return false;
        mapper.deleteAuthors(id);
        return mapper.deleteFavorite(id) == 1;
    }
}
