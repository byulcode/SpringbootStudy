package com.example.chapter6.mapper;

import com.example.chapter6.model.BoardVO;
import com.example.chapter6.model.SearchHelper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Optional;

@Mapper
public interface BoardMapper {

    void insertBoardVO(BoardVO boardVO) throws Exception;

    List<BoardVO> selectBoardVO();

    Optional<BoardVO> selectBoardVOById(int id);

    List<BoardVO> selectBoardVO(SearchHelper searchHelper) throws Exception;

    int countBoardVO(SearchHelper searchHelper) throws Exception;

    //BoardVO selectBoardVOById(int id) throws Exception;

    void updateBoardVO(BoardVO boardVO);

    void deleteById(int id);

    void updateCount(int id);

}
