<%--
  Created by IntelliJ IDEA.
  User: dimi
  Date: 20-Jun-2011
  Time: 16:26:28
  To change this template use File | Settings | File Templates.
--%>

<%@ page contentType="text/html;charset=UTF-8" %>
<div class="dsSelector">
            <g:form controller="dsSelector" action="setDataSourceNameToSession">
            <g:select name="dataSourceName" from="${list}" value="${currentDataSourceName}"/>
            <input type="submit" value="change"/>
            </g:form>
  <span class="clearCacheLink">
    <g:remoteLink action="clearCache">clear cache</g:remoteLink>
  </span>
</div>