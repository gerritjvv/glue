<table class="list">
    <thead>
    <tr>
      <td>Path</td>
      <td>Date</td>
      <td>Status</td>
      <td>
      </td>
    </tr>
    </thead>
    <tbody>
   
    <% data.each{ r -> %>
    <tr class="${r.status}">
      <th>${r.path}</th>
      <td>${r.start}</td>
      <td>${r.status}</td>
      <td>
      <g:checkBox name="triggerFileIds" value="${r.id}" class="triggerFileIds" checked="false"/>  
      </td>
    </tr>
     <% } %>

    </tbody>
  </table>
