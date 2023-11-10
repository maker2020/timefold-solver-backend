package cn.keyvalues.optaplanner.common.controller;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;

import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@RestController("/common")
@Slf4j
public class CommonController {
  
    @GetMapping("/defineDoc")
    @Operation(summary = "自定义约束文档")
    public void defineDoc(HttpServletResponse resp){
        try (InputStream in = getClass().getClassLoader().getResourceAsStream("doc/自定义约束帮助.pdf")) {
            resp.setContentType("application/pdf");
            resp.setHeader("Content-Disposition", "inline; filename="+URLEncoder.encode("自定义约束帮助.pdf", "UTF-8"));
            OutputStream out=resp.getOutputStream();
            FileCopyUtils.copy(in, out);
            out.flush();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

}
