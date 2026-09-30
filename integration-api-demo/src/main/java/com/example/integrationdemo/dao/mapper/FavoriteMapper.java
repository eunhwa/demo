package com.example.integrationdemo.dao.mapper;

import com.example.integrationdemo.dao.entity.Favorite;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface FavoriteMapper {
    void insertFavorite(Favorite favorite);
    void insertAuthors(Favorite favorite);
    boolean existsByIsbn(@Param("isbn") String isbn);
    long count();
    Favorite findById(@Param("id") long id);
    List<Favorite> findPage(@Param("offset") long offset, @Param("size") int size);
    Long lockById(@Param("id") long id);
    int deleteAuthors(@Param("id") long id);
    int deleteFavorite(@Param("id") long id);
}
