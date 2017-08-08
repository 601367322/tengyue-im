package com.tengyue.im.exception;

import com.tengyue.im.model.MyResponseBody;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by Shen on 2015/12/26.
 */
public class MyExceptionHandler implements HandlerExceptionResolver {

    @Override
    public ModelAndView resolveException(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, Exception e) {
        //使用response返回
        httpServletResponse.setStatus(HttpStatus.OK.value()); //设置状态码
        httpServletResponse.setContentType(MediaType.APPLICATION_JSON_VALUE); //设置ContentType
        httpServletResponse.setCharacterEncoding("UTF-8"); //避免乱码
        httpServletResponse.setHeader("Cache-Control", "no-cache, must-revalidate");
        try {
            if (e instanceof MyException) {
                MyException exception = (MyException) e;
                String responseError = new MyResponseBody(exception.getCode(), exception.getMessage()).toString();
                httpServletResponse.getWriter().write(responseError);
            } else {
                StringBuilder builder = new StringBuilder("\n");
                for (int i = 0; i < e.getStackTrace().length; i++) {
                    builder.append(e.getStackTrace()[i] + "\n");
                }
                httpServletResponse.getWriter().write(builder.toString());
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return new ModelAndView();
    }
}
