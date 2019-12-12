package com.fullstack;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

import static org.springframework.cloud.netflix.zuul.filters.support.FilterConstants.PRE_DECORATION_FILTER_ORDER;
import static org.springframework.cloud.netflix.zuul.filters.support.FilterConstants.PRE_TYPE;

@CrossOrigin(maxAge = 3600)
@Component
public class ZuulPreFilter extends ZuulFilter {

    private final static Logger LOGGER = org.slf4j.LoggerFactory.getLogger(ZuulPreFilter.class);

    private String ignoredURIs[] = {"/signIn","/signUp","/activate"};

    @Autowired
    TokenValidationService tokenValidationService;

    @Override
    public String filterType() {
        return PRE_TYPE; // specify the type of filter
    }

    @Override
    public int filterOrder() {
        return PRE_DECORATION_FILTER_ORDER -1 ; // specify filter order
    }

    @Override
    public boolean shouldFilter() {
        HttpServletRequest request = RequestContext.getCurrentContext().getRequest();
        String requestUri = request.getRequestURI();
        for(String uri: ignoredURIs) {
            int i = requestUri.indexOf(uri);
            if (requestUri.indexOf(uri) >= 0) {
                LOGGER.info("No Authentication Required: Requesting URI======================> "+ requestUri);
                return false;
            }
        }
        return true;
    }

    // 过滤器具体执行的操作
    @Override
    public Object run() {

        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest springRequest = servletRequestAttributes.getRequest();
        //HttpServletRequest request = servletRequestAttributes.getRequest();
        String springToken = springRequest.getHeader("access-control-request-headers");
        LOGGER.info("request.getMethod() ==============================================" + springRequest.getMethod());
        if("OPTIONS".equals(springRequest.getMethod())) {
            return null;
        }

        RequestContext requestContext = RequestContext.getCurrentContext();
        HttpServletRequest request = requestContext.getRequest();
        HttpServletResponse response = requestContext.getResponse();
        String requestUri = request.getRequestURI();

        LOGGER.info("Header Host ============================>" + request.getHeader("host"));

        String jwt_token = request.getHeader("JWTToken");
        if(jwt_token == null) {
            requestContext.setSendZuulResponse(false);
            requestContext.setResponseStatusCode(HttpStatus.SC_FORBIDDEN);
            LOGGER.info("Authentication Error ===============================================>Token is empty");
            return null;
        }

        //Verify Token
        /*
        map.put("status",status);       success/fail
        map.put("userName", userName);
        map.put("userType", userType);
        map.put("message",message);     Token is valid/Token is expired/Token has been manipulated
         */
        Map<String, String> map = tokenValidationService.verifyToken(jwt_token);
        if("fail".equals(map.get("status"))) {
            requestContext.setSendZuulResponse(false);
            requestContext.setResponseStatusCode(HttpStatus.SC_FORBIDDEN);
            LOGGER.info("Authentication Error =========================================>" + map.get("message"));
            return null;
        }        LOGGER.info(map.get("message") + "; User:" + map.get("userName")+ "; User Type:" + map.get("userType"));

        //response.setStatus(403);
        LOGGER.info("Requesting URL: "+ requestUri + " response.getStatus():"+response.getStatus());
        return null;
    }

}