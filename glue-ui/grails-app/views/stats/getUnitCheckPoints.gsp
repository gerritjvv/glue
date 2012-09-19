<%--
  Created by IntelliJ IDEA.
  User: Don
  Date: 20-Jun-2011
  Time: 16:26:28
  To change this template use File | Settings | File Templates.
--%>

<%@ page contentType="text/html;charset=UTF-8" %>
<h2>List of checkpoints that ran on ${params.name}</h2>
  <table class="list">
    <thead>
    <tr>
      <td>Path</td>
      <td>CheckPoint</td>
    </tr>
    </thead>
    <tbody>
    <% data.each{ r -> %>
    <tr class="${r.status}">
      <td>${r.path}</td>
      <th>       ${r.checkpoint}</th>

    </tr>
     <% } %>

    </tbody>
  </table>

