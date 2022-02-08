package com.example.chapter6.config;

import com.example.chapter6.jwt.JwtTokenValidator;
import com.example.chapter6.model.MemberVO;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@Component
public class LoginInterceptor implements HandlerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(LoginInterceptor.class);
    private JwtTokenValidator jwtTokenValidator;

    public LoginInterceptor(JwtTokenValidator jwtTokenValidator) {
        this.jwtTokenValidator = jwtTokenValidator;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

//        String token =request.getHeader("Authorization");
//
//        logger.info(request.getMethod());
//
//        if(StringUtils.equals(request.getMethod(), "OPTIONS")){
//            logger.info("OPTIONS 메소드이다.");
//            return true;
//        }
//
//        logger.info("token before- {}", token);
//        token = token.replace("Bearer ","");
//        logger.info("token after c- {}", token);
//
//        // 토큰 검증 메서드
//        Boolean valid = jwtTokenValidator.validateToken(token);
//
//        if(valid){
//            return true;
//        }else {
//            return false;
//        }
        HttpSession session = request.getSession();

        MemberVO m = new MemberVO();
        m.setUserId("test");
        session.setAttribute("memberVO", m);

        MemberVO memberVO = (MemberVO) session.getAttribute("memberVO");

        logger.info("로그인 인터셉트 - {}", memberVO);

        if(memberVO != null){
            return true;
        }else{
            response.sendRedirect("/member/login");
            return false;
        }
    }
}
