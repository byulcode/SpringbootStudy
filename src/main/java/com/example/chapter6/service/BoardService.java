package com.example.chapter6.service;

import com.example.chapter6.model.BoardVO;
import com.example.chapter6.model.SearchHelper;
import org.springframework.validation.Errors;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface BoardService {

    void insertBoardVO(BoardVO boardVO) throws Exception;

    HashMap<String, Object> selectBoardVO(SearchHelper searchHelper) throws Exception;

    List<BoardVO> selectBoardVO();

    Optional<BoardVO> selectBoardVOById(int id) throws Exception;

    void updateBoardVO(BoardVO boardVO);

    void deleteById(int id);

    public Map<String, String> formValidation(Errors errors);


}
