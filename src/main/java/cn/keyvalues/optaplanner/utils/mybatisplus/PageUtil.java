package cn.keyvalues.optaplanner.utils.mybatisplus;

import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import jakarta.servlet.http.HttpServletRequest;

public class PageUtil {

    public static <T> Page<T> getPage() {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String pageNumString = request.getParameter("pageNum");
        String pageSizeString = request.getParameter("pageSize");
        int pageNum=pageNumString==null?1:Integer.parseInt(pageNumString);
        int pageSize=pageSizeString==null?Integer.MAX_VALUE:Integer.parseInt(pageSizeString);
        return new Page<>(pageNum, pageSize);
    }
    
}