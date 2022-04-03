package hello.core.web;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

@Controller
@RequiredArgsConstructor
public class LogDemoController {

    private final LogDemoService logDemoService;
    private final ObjectProvider<MyLogger> myLoggerProvider;

    @RequestMapping("log-demo")
    @ResponseBody
    public String logDemo(HttpServletRequest request) {
        String requestURL = request.getRequestURL().toString();
        MyLogger mylogger = myLoggerProvider.getObject();
        myLogger.setRequestURL(requestURL);

        //이후 Service, Controller, Repository 아무대서나 휘뚜루 마뚜루 맘대로 로그 찍기
        mylogger.log("controller이다~");
        logDemoService.log("서비스 로그~");
        //한 request에서는 동일한 mylogger 빈이 유지가 된다는 사실!
        //따라서 클라이언트별로 UUID로 구분이 가능하다.
    }
}
