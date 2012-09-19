<%--
  Created by IntelliJ IDEA.
  User: dimi
  Date: 21-Jun-2011
  Time: 14:44:38
  To change this template use File | Settings | File Templates.
--%>

<%@ page contentType="text/html;charset=UTF-8" %>
<html>
  <head><title>Glue error page</title>
   <meta name="layout" content="main" />
  </head>

<body>
<h1><center>Error</center></h1>

  <div class="message">
                <p>Please check the configuration and values:<p/>
                Current config variables: <br/>
                System.getenv("GLUE_UI_CONFIG") : ${System.getenv("GLUE_UI_CONFIG")}<br/>
                System.getProperty("GLUE_UI_CONFIG") : ${System.getProperty("GLUE_UI_CONFIG")}><br/>
                  
                <p/>
                <strong>Error ${request.'javax.servlet.error.status_code'}:</strong> ${request.'javax.servlet.error.message'.encodeAsHTML()}<br/>
                <strong>Servlet:</strong> ${request.'javax.servlet.error.servlet_name'}<br/>
                <strong>URI:</strong> ${request.'javax.servlet.error.request_uri'}<br/>
                <g:if test="${exception}">
                        <strong>Exception Message:</strong> ${exception.message?.encodeAsHTML()} <br />
                        <strong>Caused by:</strong> ${exception.cause?.message?.encodeAsHTML()} <br />
                        <strong>Class:</strong> ${exception.className} <br />
                        <strong>At Line:</strong> [${exception.lineNumber}] <br />
                        <strong>Code Snippet:</strong><br />
                        <div class="snippet">
                                <g:each var="cs" in="${exception.codeSnippet}">
                                        ${cs?.encodeAsHTML()}<br />
                                </g:each>
                        </div>
                </g:if>
        </div>
        <g:if test="${exception}">
            <h2>Stack Trace</h2>
            <div class="stack">
              <pre><g:each in="${exception.stackTraceLines}">${it.encodeAsHTML()}<br/></g:each></pre>
            </div>

          <g:if test="${exception.getCause()}">
            <h2>Caused by</h2>
            <div class="stack">
              <pre><g:each in="${exception.getCause().getStackTrace()}">${it.encodeAsHTML()}<br/></g:each></pre>
            </div>
        </g:if>
        </g:if>


  </body>
</html>