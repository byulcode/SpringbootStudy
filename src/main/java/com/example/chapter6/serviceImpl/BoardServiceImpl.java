package com.example.chapter6.serviceImpl;

import com.example.chapter6.service.BoardService;
import com.example.chapter6.mapper.BoardMapper;
import com.example.chapter6.model.BoardVO;
import com.example.chapter6.model.SearchHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.FieldError;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class BoardServiceImpl implements BoardService {

    private static final Logger logger = LoggerFactory.getLogger(BoardServiceImpl.class);

    private final BoardMapper boardMapper;

    public BoardServiceImpl(BoardMapper boardMapper){
        this.boardMapper = boardMapper;
    }

    // 게시물 저장
    @Override
    public void insertBoardVO(BoardVO boardVO) throws Exception {
        boardMapper.insertBoardVO(boardVO);
    }

    // 게시물 목록
    @Override
    public HashMap<String, Object> selectBoardVO(SearchHelper searchHelper) throws Exception {

        HashMap<String, Object> resultMap = new HashMap<>();

        int totalCnt = boardMapper.countBoardVO(searchHelper);

        int code = searchHelper.getSrchCode();
        String srchType = searchHelper.getSrchType();
        String srchKeyword = searchHelper.getSrchKeyword();
        int page = searchHelper.getPage();

        searchHelper = new SearchHelper(totalCnt, searchHelper.getPage());

        searchHelper.setSrchCode(code);
        searchHelper.setSrchType(srchType);
        searchHelper.setSrchKeyword(srchKeyword);
        searchHelper.setPage(page);

        List<BoardVO> list = boardMapper.selectBoardVO(searchHelper);

        resultMap.put("list", list);
        resultMap.put("searchHelper", searchHelper);

        return resultMap;
    }

    // 추가해봄(게시물 목록)
    @Override
    public List<BoardVO> selectBoardVO() {
        return boardMapper.selectBoardVO();
    }

    // 게시물 조회
    @Override
    public BoardVO selectBoardVOById(int id) throws Exception {
        boardMapper.updateCount(id);
        return boardMapper.selectBoardVOById(id);
    }

    // 게시물 수정
    @Override
    public void updateBoardVO(BoardVO boardVO) {
        boardMapper.updateBoardVO(boardVO);
    }

    // 게시물 삭제
    @Override
    public void deleteById(int id) throws Exception {
        boardMapper.deleteById(id);
    }

    @Override
    public Map<String, String> formValidation(Errors errors) {
        Map<String,String> result = new HashMap<>();

        for(FieldError error : errors.getFieldErrors()){
            String validKeyName = String.format("valid_%s", error.getField());
            result.put(validKeyName, error.getDefaultMessage());
        }
        return result;
    }

}
