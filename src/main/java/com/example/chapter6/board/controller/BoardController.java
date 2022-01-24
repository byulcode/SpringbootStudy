package com.example.chapter6.board.controller;

import com.example.chapter6.Util.MediaUtil;
import com.example.chapter6.board.service.BoardService;
import com.example.chapter6.file.FIleMapService;
import com.example.chapter6.file.UploadFileService;
import com.example.chapter6.model.*;
import com.example.chapter6.payload.response.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Controller
@RequestMapping("/board")
public class BoardController {

    private static final Logger logger = LoggerFactory.getLogger(BoardController.class);

    private BoardService boardService;
    private UploadFileService uploadFileService;
    private FIleMapService fIleMapService;

    public BoardController(BoardService boardService, UploadFileService uploadFileService, FIleMapService fIleMapService) {
        this.boardService = boardService;
        this.uploadFileService = uploadFileService;
        this.fIleMapService = fIleMapService;
    }

    //    게시물 목록
    @RequestMapping("/list")
    public String boardList(
            @ModelAttribute SearchHelper searchHelper,
            Model model
    ) throws Exception {

        HashMap<String, Object> result = boardService.selectBoardVO(searchHelper);

        logger.info(searchHelper.toString());

        model.addAttribute("searchHelper", result.get("searchHelper"));
        model.addAttribute("result", result.get("list"));

        return "board/list";
    }

//    게시물 조회
    @RequestMapping("/view")
    public String boardView(
            @RequestParam(value = "id", defaultValue = "0") int id,
            @ModelAttribute SearchHelper searchHelper,
            Model model
    ) throws Exception {
        if(id > 0){
            // 게시물 조회
            BoardVO boardVO = boardService.selectBoardVOById(id);
            model.addAttribute("boardVO", boardVO);
            model.addAttribute("searchHelper", searchHelper);

            logger.info(boardVO.toString());

            if(boardVO.getTitle() != null && boardVO.getContent() != null){
                List<UploadFileVO> fileList = uploadFileService.selectFileByBoardId(boardVO.getId());
                model.addAttribute("uploadFileVO", fileList);
            } else {
                model.addAttribute("uploadFileVO", null);
            }
        }else{
            // 오류 출력 및 목록으로
            Message message = new Message();
            message.setMessage("게시물이 없습니다.");
            message.setHref("/board/list");
            model.addAttribute("data", message);
            model.addAttribute("searchHelper", searchHelper);
            return "message/message";
        }

        return "board/view";
    }

//    글쓰기 페이지로 이동
    @RequestMapping("/write")
    public String boardWrite(
            @ModelAttribute SearchHelper searchHelper,
            Model model
    ) {
//        계정을 따라가도록 수정 해야함
        BoardVO boardVO = new BoardVO();
        boardVO.setCode(1000);
        model.addAttribute("boardVO", boardVO);
        model.addAttribute("searchHelper", searchHelper);
        return "board/write";
    }

//    새글 저장 및 수정 저장
    @PostMapping("/save")
    public String boardSave(
            @ModelAttribute BoardVO boardVO,
            @RequestParam("file") List<MultipartFile> multipartFile,
            HttpServletRequest request,
            Model model
            ) throws Exception {
        HttpSession session = request.getSession();
        MemberVO sessionResult = (MemberVO) session.getAttribute("memberVO");

        if(sessionResult != null){
            // 저장 또는 수정
            String userId = sessionResult.getUserId();

            boardVO.setRegId(userId);

            if(boardVO.getId() > 0){
                // 수정
                String regId = boardVO.getRegId();
                boardService.updateBoardVO(boardVO);
            } else {
                // 저장
                boardService.insertBoardVO(boardVO);
            }

            for (int i = 0; i < multipartFile.size(); i++){
               UploadFileVO uploadFileVO = uploadFileService.saveFile(multipartFile.get(i));
               logger.info("uploadFileVO - {}" , uploadFileVO);
               FileMapVO fileMapVO = new FileMapVO();
               fileMapVO.setFileId(uploadFileVO.getId());
               fileMapVO.setBoardId(boardVO.getId());
               fIleMapService.insertFileMap(fileMapVO);
            }

        }else{
            // 세션 없음 (로그인 안됨)
            model.addAttribute("data", new Message("로그인 후 이용하실 수 있습니다.", "/member/login"));
            return "message/message";
        }

        return "redirect:/board/list";
    }

//    게시물 수정하러 글쓰기 페이지로 이동
    @GetMapping("/modify")
    public String boardModify(
            @RequestParam(value = "id") int id,
            @ModelAttribute SearchHelper searchHelper,
            Model model
    ) throws Exception {
        if(id > 0){
            // 게시물 조회
            BoardVO boardVO = boardService.selectBoardVOById(id);
            model.addAttribute("boardVO", boardVO);
            model.addAttribute("searchHelper", searchHelper);

            if(boardVO.getTitle() != null && boardVO.getContent() != null){
                List<UploadFileVO> fileList = uploadFileService.selectFileByBoardId(boardVO.getId());
                model.addAttribute("uploadFileVO", fileList);
            } else {
                model.addAttribute("uploadFileVO", null);
            }

        }else{
            // 오류 출력 및 목록으로
            Message message = new Message();
            message.setMessage("게시물이 없습니다.");
            message.setHref("/board/list?srchCode=" + searchHelper.getSrchCode() + "&srchType=" + searchHelper.getSrchType()
                    + "&srchKeyword=" + searchHelper.getSrchKeyword());
            model.addAttribute("data", new Message());
            return "message/message";
        }

        return "board/write";
    }

//    배열 형태로 게시물 삭제
    @PostMapping("/delete")
    public String delete(
            @RequestParam(value = "del[]", defaultValue = "") int[] del,
            Model model
    ) throws Exception {
        logger.info("삭제 배열 - {}", del);

        Message message = new Message();
        message.setHref("/board/list");

        if(del.length > 0){
            for(int i=0; i<del.length; i++){
                boardService.deleteById(del[i]);
            }
            message.setMessage("삭제되었습니다.");
        }else{
            message.setMessage("삭제할 게시물을 선택하세요.");
        }

        model.addAttribute("data", message);
        return "message/message";
    }

    //파일 업로드
    @PostMapping("/boardSaveTest")
    public String boardSaveTest(@RequestParam("file") MultipartFile multipartFile) throws IOException {

        logger.info(multipartFile.getName());
        logger.info(multipartFile.getContentType());
        logger.info(multipartFile.getOriginalFilename());
        logger.info(String.valueOf(multipartFile.getSize()));

        Path path = Paths.get("C:/upload_file/").toAbsolutePath().normalize();
        Files.createDirectories(path);

        String generateFileName = UUID.randomUUID().toString();

        logger.info("선택한 파일 명 - {}", generateFileName);

        //파일명에서 . 위치 찾기 >> lastIndexOf abc.jpg abc.def.jpg
        //
        int dot = multipartFile.getOriginalFilename().lastIndexOf(".");

        logger.info("dot - {}", dot);

        String extension = "";

        if(-1 != dot && multipartFile.getOriginalFilename().length() - 1 > dot){
            extension = multipartFile.getOriginalFilename().substring(dot+1);
        }

        logger.info("확장명 - {}", extension);


        File file = new File("C:/upload_file/" + generateFileName + "." + extension);

        multipartFile.transferTo(file);

        return "test";
    }

    @GetMapping("/file/download")
    @ResponseBody
    public ResponseEntity fileDownload(
            @RequestParam(value = "name", defaultValue = "") String name
    ) throws UnsupportedEncodingException, MalformedURLException {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\""+ new String(name.getBytes(StandardCharsets.UTF_8), "ISO-8859-1") + "\"");
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);

        Path path = Paths.get("C:/upload_file/" + name).toAbsolutePath().normalize();
        logger.info(String.valueOf(path));
        Resource resource = new UrlResource(path.toUri());

        return ResponseEntity.ok().headers(headers).body(resource);
    }

    @GetMapping("/file/{fileId}")
    @ResponseBody
    public ResponseEntity showFile(@PathVariable int fileId) throws Exception {
        try {
            UploadFileVO uploadFileVO = uploadFileService.load(fileId);
            String fileName = uploadFileVO.getFileName();

            if(uploadFileVO == null) return ResponseEntity.badRequest().build();
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename=\""+ new String(fileName.getBytes(StandardCharsets.UTF_8), "ISO-8859-1") + "\"");

            if (MediaUtil.containsMediaType(uploadFileVO.getContentType())) {
                headers.setContentType(MediaType.valueOf(uploadFileVO.getContentType()));
            }else {
                headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            }

            Resource resource = uploadFileService.loadAsResource(uploadFileVO.getSaveFileName());

            return ResponseEntity.ok().headers(headers).body(resource);
        }catch (Exception e){
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/deleteFile/{fileId}")
    @ResponseBody
    public ApiResponse deleteFile(@PathVariable int fileId){
        Boolean result = uploadFileService.deleteFileById(fileId);
        if(result){
            return new ApiResponse(true, "삭제 완료");
        }else {
            return new ApiResponse(false, "삭제 완료");
        }
    }


}
