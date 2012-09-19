<%--
  Created by IntelliJ IDEA.
  User: dimi
  Date: 20-Jun-2011
  Time: 16:26:28
  To change this template use File | Settings | File Templates.
--%>

<%@ page contentType="text/html;charset=UTF-8" %>

        <meta name="layout" content="main" />
  <table class="statsById list">
    <thead>
    <tr>
      <td>Unit name</td>
      <td>Num paths</td>
      <td>Last check</td>
      <td>Last check, seconds</td>
    </tr>
    </thead>
    <tbody>
    <% data.each{ r -> %>
    <tr>

    <th><g:link action="getUnitCheckPoints" params="[name:r.unit_name]" class="thickbox">${r.unit_name}</g:link></th>

    <td>${r.num}</td>
      <td>${r.minsecStr} ago</td>
      <td
      <g:if test="${r.minsec<3600*24*14}">
      class="lastchecksec"
      </g:if>
      >${r.minsec}</td>
    </tr>
     <% } %>

    </tbody>
  </table>

