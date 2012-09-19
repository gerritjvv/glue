<%

%>
<table class="list">
       <tr><th>uid</th>
       <th>name</th>
         <th>started</th>
         <th>duration</th>
         <th>status</th>
         <th>&nbsp;</th>
       </tr>
       <g:if test="${list.size() == 0}">
         <tr><td colspan="6" class="nojobs">No tasks submitted yet</td></tr>
         </g:if >
       <% list.each{ day, r -> %>
       <tr class="dayheader">
       <td colspan="6"><h2>${day} (${r.size()} jobs)</h2><a href=# onclick="$('#day_${day}').toggle()"> (show/hide)</a></td></tr>
       <tbody id="day_${day}"

       <g:if test="${day != firstDay}">
               class="daybody"
               </g:if>
               >
        <% def i =0; %>
        <% r.each{ key, row ->
        i++;

        %>
         <tr class="${row.status}">
       <th><a href="glue/${row.uid}/">${row.uid}</a></th>
       <td>${row.name}</td>
       <td>&nbsp;${row.startDate}</td>
       <td>&nbsp;${row.endDate}</td>
       <td>${row.status}</td>
        <td>
          <g:if test="${row.status == 'RUNNING'}">
          <input type="checkbox" name="joblist" class="jobchecks" value="${row.uid}">
            </g:if>

          </td>

     </tr>
       

       <%} %>
       </tbody>
       <% } %>
     </table>
