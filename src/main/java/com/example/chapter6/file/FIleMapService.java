package com.example.chapter6.file;

import com.example.chapter6.mapper.FileMapMapper;
import com.example.chapter6.model.FileMapVO;
import groovy.util.logging.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class FIleMapService {

    private FileMapMapper fileMapMapper;

    public FIleMapService(FileMapMapper fileMapMapper) {
        this.fileMapMapper = fileMapMapper;
    }

    public void insertFileMap(FileMapVO fileMapVO){
        fileMapMapper.insertFileMap(fileMapVO);
    }
}
