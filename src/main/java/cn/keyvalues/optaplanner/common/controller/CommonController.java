package cn.keyvalues.optaplanner.common.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

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
        try (FileInputStream fin = new FileInputStream(new File("doc/自定义约束帮助.pdf"))) {
            resp.setContentType("application/pdf");
            resp.setHeader("Content-Disposition", "inline; filename=自定义约束帮助.pdf");
            OutputStream out=resp.getOutputStream();
            FileCopyUtils.copy(fin, out);
            out.flush();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

}
