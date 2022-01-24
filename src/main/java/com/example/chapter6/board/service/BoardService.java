package com.example.chapter6.board.service;

import com.example.chapter6.model.BoardVO;
import com.example.chapter6.model.SearchHelper;
import org.springframework.validation.Errors;

import java.util.HashMap;
import java.util.Map;

public interface BoardService {

    void insertBoardVO(BoardVO boardVO) throws Exception;

    HashMap<String, Object> selectBoardVO(SearchHelper searchHelper) throws Exception;

    BoardVO selectBoardVOById(int id) throws Exception;

    void updateBoardVO(BoardVO boardVO) throws Exception;

    void deleteById(int id) throws Exception;

    public Map<String, String> formValidation(Errors errors);

}
