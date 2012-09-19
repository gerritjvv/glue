<%--
  Created by IntelliJ IDEA.
  User: dimi
  Date: 20-Jun-2011
  Time: 16:26:28
  To change this template use File | Settings | File Templates.
--%>

<%@ page contentType="text/html;charset=UTF-8" %>
<h2>List of units that ran on ${params.day}, ${params.hour}h</h2>
  <table class="list">
    <thead>
    <tr>
      <td>Id</td>
      <td>Start</td>
      <td>End</td>
      <td>Status</td>
    </tr>
    </thead>
    <tbody>
    <% data.each{ r -> %>
    <tr class="${r.status}">
    <th><g:link action="jobInfo" controller="glue" params="[uid:r.unit_id]">${r.unit_id}</g:link></th>
      <td>${r.start}</td>
      <td>${r.end}</td>
      <td>${r.status}</td>
    </tr>
     <% } %>

    </tbody>
  </table>
