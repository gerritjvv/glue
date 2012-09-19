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
    <tr class="head">
      <td rowspan="2">Unit name</td>
      <td colspan="5">Glue units</td>
      <td colspan="3">Triggers</td>
    </tr>
    <tr>
      <td>Waiting</td>
      <td>Running</td>
      <td>Finished</td>
      <td>Failed</td>
      <td>Total</td>
      <td>Ready</td>
      <td>Processed</td>
      <td>Total</td>
    </tr>
  </thead>
  <tbody>
<% data.each{ r -> %>
    <tr>
    <th><g:link action="statsForUnit" params="[name:r.name]">${r.name}</g:link></th>
    <td class="num_waiting">${r.wcount}</td>
      <td class="num_running">${r.rcount}</td>
      <td class="num_finished">${r.fcount}</td>
      <td class="num_failed">${r.failedcount}</td>
      <td class="num_total">${r.count}</td>
      <td class="num_t_ready">${r.trigger_r}</td>
      <td class="num_t_processed">${r.trigger_p}</td>
      <td class="num_t_total">${r.trigger_c}</td>
    </tr>
     <% } %>

</tbody>
</table>

