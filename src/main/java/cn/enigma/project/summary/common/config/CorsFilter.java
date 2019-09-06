package cn.enigma.project.summary.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/** 
 * @author luzh
 * createTime 2017年5月23日 下午2:58:47
 */
@Configuration
public class CorsFilter extends OncePerRequestFilter {

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		response.setHeader("Access-Control-Allow-Origin", request.getHeader("Origin"));
		response.setHeader("Access-Control-Allow-Methods","GET,POST,DELETE,PUT,OPTIONS");
		// 这块要注意
		response.setHeader("Access-Control-Allow-Headers","Origin,X-Requested-With,Content-Type,Access-Control,Accept");
		response.setHeader("Access-Control-Allow-Credentials","true");
		response.setHeader("Access-Control-Max-Age", "3600");  
		filterChain.doFilter(request, response);
	}

}
