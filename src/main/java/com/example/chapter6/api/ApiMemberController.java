package com.example.chapter6.api;
// 비동기통신
import com.example.chapter6.Util.ExceptionMessage;
import com.example.chapter6.Util.Util;
import com.example.chapter6.event.OnLogoutSuccessEvent;
import com.example.chapter6.exception.BadRequestException;
import com.example.chapter6.exception.InsertFailException;
import com.example.chapter6.exception.ResourceAlreadyInUseException;
import com.example.chapter6.exception.UserNotFoundException;
import com.example.chapter6.jwt.AuthService;
import com.example.chapter6.service.MemberService;
import com.example.chapter6.model.MemberVO;
import com.example.chapter6.payload.request.LoginRequest;
import com.example.chapter6.payload.response.ApiResponse;
import com.example.chapter6.payload.response.JwtAuthenticationResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.swing.text.html.Option;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/member")
public class ApiMemberController {

    private static final Logger logger = LoggerFactory.getLogger(ApiMemberController.class);

    private final MemberService memberService;
    private AuthService authService;
    private ApplicationEventPublisher applicationEventPublisher;

    public ApiMemberController(MemberService memberService, AuthService authService,ApplicationEventPublisher applicationEventPublisher) {
        this.memberService = memberService;
        this.authService = authService;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    // 로그아웃
    @GetMapping("/logout")
    public ApiResponse logout(HttpServletRequest request){

        String accessToken = request.getHeader("Authorization");
        accessToken = accessToken.replace("Bearer ", "");
        //토큰 검증...
        String res = authService.getUserIdFromJWT(accessToken);
        //test id
        OnLogoutSuccessEvent event = new OnLogoutSuccessEvent(res, accessToken);
        applicationEventPublisher.publishEvent(event);

        return new ApiResponse(true,"완료");
    }

    //로그인 처리
    @PostMapping("/login")
    public ResponseEntity loginProcess(@RequestBody @Valid LoginRequest loginRequest) {
        MemberVO memberVO = new MemberVO();
        memberVO.setUserId(loginRequest.getUserId());
        memberVO.setPassword(loginRequest.getPassword());

        JwtAuthenticationResponse result = memberService.loginProcess(memberVO);
        return ResponseEntity.ok(result);
    }

    // 토큰 갱신
    @GetMapping("/regenToken")
    public ResponseEntity regenToken(@RequestParam(value = "refreshToken", defaultValue = "") String refreshToken){

        if(refreshToken.equals("")){
            throw new BadRequestException("리프레시 토큰이 없습니다.");
        }else {
            JwtAuthenticationResponse response = authService.regenToken(refreshToken);
            return ResponseEntity.ok(response);
        }
    }

    @GetMapping("/apiTest")
    public ApiResponse loginProcess(HttpServletRequest request){
        String token = request.getHeader("Authorization");
        token = token.replace("Bearer ","");
        logger.info("토큰으로 반환된 ID - {}",authService.getUserIdFromJWT(token) );
        logger.info("토큰으로 반환된 만료일 - {}",authService.getTokenExpiryFromJWT(token) );
        return new ApiResponse(true,"완료");
    }

    // 아이디 중복 여부 체크
   @GetMapping("/exist/id/{userId}")
    public ApiResponse existId(@PathVariable String userId) throws Exception {
        if (userId.equals("")) throw new BadRequestException(ExceptionMessage.EMPTY_USER_ID);

        Boolean result = memberService.duplicateId(userId);

        // true로 리턴되면 아이디가 선점되어 있다.
       if(result) return new ApiResponse(false,"사용중인 아이디 입니다.");
//        if(result) {
//            throw new ResourceAlreadyInUseException("입력하신 아이디","userId", userId);
//        }

        return new ApiResponse(true,ExceptionMessage.USE_ID_AVAILABLE);
    }

    // 이메일 중복 여부 체크
    @GetMapping("/exist/email/{email}")
    public ApiResponse existEmail(@PathVariable String email) throws Exception {
        if (email.equals("")) throw new BadRequestException(ExceptionMessage.EMPTY_USER_ID);

        Boolean result = memberService.duplicateEmail(email);

        // true로 리턴되면 email이 선점되어 있다.
        if(result) {
            if(result) return new ApiResponse(false,"사용중인 이메일 입니다.");
        }

        return new ApiResponse(true,ExceptionMessage.USE_EMAIL_AVAILABLE);
    }

    // 아이디 찾기
    @GetMapping("/find/id/{name}/{email}")
    public ApiResponse findId(
            @PathVariable String name,
            @PathVariable String email
    ) {
        if(!name.equals("") && !email.equals("")){
            MemberVO memberVO = new MemberVO();
            memberVO.setName(name);
            memberVO.setEmail(email);

            String id = memberService.findUserId(memberVO);

            logger.info("찾은 id -{}", id);

            if(id == null){
                //찾는 id가 없습니다.
                throw new UserNotFoundException("계정을 찾을 수 없습니다.");
            }else {
                //찾는 id가 있습니다.
                int idLength = id.length();
                id = id.substring(0, idLength - 2);
                id += "**";
                logger.info("id 마스킹 -{}", id);
                return new ApiResponse(true, name + "님이 찾으시는 ID는 " + id + "입니다.");
            }
        }
        throw new BadRequestException(ExceptionMessage.EMPTY_INFO);
    }

    // 비밀번호 변경
    @GetMapping("/find/password/{name}/{email}/{userId}")
    public ApiResponse findPw(
            @PathVariable String name,
            @PathVariable String email,
            @PathVariable String userId
    ) {
        if(!name.equals("") && !email.equals("") && !userId.equals("")){
            MemberVO memberVO = new MemberVO();
            memberVO.setName(name);
            memberVO.setEmail(email);
            memberVO.setUserId(userId);

            String id = memberService.findUserId(memberVO);
            try {
                if(id == null){
                    //계정 없음
                    throw new UserNotFoundException(ExceptionMessage.NOT_FOUND_USER_ID);
                }else {
                    //계정이 있으면 비번을 임의로 변경하고 고지한다.
                    String pw = Util.generateRandomString(10);
                    logger.info("pw -{}", pw);
                    memberVO.setPassword(pw);
                    memberService.updatePassword(memberVO);
                    return new ApiResponse(true,"변경된 비밀번호는 " + pw + "입니다.");
                }
            }catch (Exception e){
                throw new UserNotFoundException(ExceptionMessage.NOT_FOUND_USER_ID);
            }
        }
        throw new BadRequestException(ExceptionMessage.EMPTY_INFO);
    }

    // 회원가입
    @PostMapping("/join")
    public ApiResponse memberJoin(@RequestBody @Valid MemberVO memberVO, Errors errors) {

        HashMap<String, Object> errorMap = new HashMap<>();

        if(errors.hasErrors()){
            Map<String,String> validate = memberService.formValidation(errors);

            for(String key : validate.keySet()){
                errorMap.put(key,validate.get(key));
            }
            return new ApiResponse(false,ExceptionMessage.SAVE_FAIL, errorMap);
        }

        boolean idCheck = false;
        boolean emailCheck = false;
        try{
            idCheck = memberService.duplicateId(memberVO.getUserId());
            emailCheck = memberService.duplicateEmail(memberVO.getEmail());
        }catch (Exception e){
            throw new UserNotFoundException(ExceptionMessage.NOT_FOUND_USER_ID);
        }
        if(!idCheck && !emailCheck){
            try{
                memberService.insertMember(memberVO);
                return new ApiResponse(true,ExceptionMessage.JOIN_COMPLETE);
            }catch (Exception e){
                throw new InsertFailException(ExceptionMessage.SAVE_FAIL);
            }
        }
        throw new InsertFailException(ExceptionMessage.SAVE_FAIL);
    }

}