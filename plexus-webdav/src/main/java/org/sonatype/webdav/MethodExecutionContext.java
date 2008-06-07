package org.sonatype.webdav;

import org.sonatype.webdav.security.User;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


public interface MethodExecutionContext
{

    User getUser();

    HttpServletRequest getHttpServletRequest();
    
    HttpServletResponse getHttpServletResponse();

}
