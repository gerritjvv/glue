<%--
  Created by IntelliJ IDEA.
  User: dimi
  Date: 20-Jun-2011
  Time: 16:26:28
  To change this template use File | Settings | File Templates.
--%>

<%@ page contentType="text/html;charset=UTF-8" %>

  <table class="list statsForUnit">
    <thead>
    <tr>
      <td rowspan="2">Day</td>
      <td colspan="25">Unit run count</td>
      <td colspan="25">Trigger files count</td>
    </tr>
    <tr>
      <% hours.each { hour -> %><td>${hour}</td>
      <% } %>
      <td>Total</td>
      <% hours.each { hour -> %><td>${hour}</td>
      <% } %>
      <td>Total</td>
      <td title="Max Delay in hours" class="triggerFileDelaysPartition">d</td>
      <td title="Percent of files with more than 3 hr delay">%</td>
    </tr>
    </thead>
    <tbody>
    <%
      def rowCount=0;
      days.each{ day->

    %>
         <g:if test="${rowCount==days}"><tr class="spacer"><td colspan="51">Days 2-14</td></tr></g:if>
         <g:if test="${rowCount==15}"><tr  class="spacer"><td colspan="51">Days after first two weeks</td></tr></g:if>
    <tr
         <g:if test="${rowCount==0}">class="thisday"</g:if>
         <g:if test="${rowCount<15 && rowCount>0}">class="firstXday"</g:if>
         <g:if test="${rowCount>=15}">class="oldday"</g:if>
    >
    <th>${day}</th>
      <%
          def total=0;
          hours.each { hour ->
      total+=data[day+' '+hour]['count'];
      %><td
      title="${data[day+" "+hour]['wcount']} waiting, ${data[day+" "+hour]['rcount']} running, ${data[day+" "+hour]['fcount']} finished, ${data[day+" "+hour]['failedcount']} failed"
      <g:if test="${data[day+' '+hour]['failedcount']>0}">class="hasfailed units"</g:if>
        <g:if test="${data[day+' '+hour]['failedcount']==0}">class="units"</g:if>
        >

        <g:if test="${data[day+' '+hour]['count']>0}">
        <g:link action="getUnitList" params="[name:params.name,day:day, hour:hour, height: 400, width: 400]" class="thickbox">${data[day+" "+hour]['count']}</g:link>
          </g:if>
        <g:if test="${data[day+' '+hour]['count']==0}">0</g:if>

      </td><% } %>
       <td class="utotal">${total}</td>
      <%
          total=0;
          hours.each { hour ->
      total+=tdata[day+' '+hour]['count'];
      %><td
      title="${tdata[day+" "+hour]['rcount']} ready, ${tdata[day+" "+hour]['pcount']} processed "
      <g:if test="${tdata[day+' '+hour]['rcount']>0}">class="hasnotprocessed triggers"</g:if>
        <g:if test="${tdata[day+' '+hour]['rcount']==0}">class="triggers"</g:if>
        >

        <g:if test="${tdata[day+' '+hour]['count']>0}">
        <g:link action="getTriggerFileList" params="[name:params.name,day:day, hour:hour, height: 600, width: 1100]" class="thickbox">${tdata[day+" "+hour]['count']}</g:link>
          </g:if>
        <g:if test="${tdata[day+' '+hour]['count']==0}">0</g:if>

      </td><% } %>
      <td class="ttotal">${total}</td>
      <td class="delay triggerFileDelaysPartition"><%= triggerFileDelaysData.find{it.days==day}?.maxdelay?:0 %></td>
      <td class="delayPercent"><%def delayPercent = triggerFileDelaysData.find{it.days==day}?.percent?:0 %>
          <g:formatNumber type="number" maxFractionDigits="1" number="${delayPercent}"/>
      </td>
    </tr>
     <% rowCount++;
       } %>



    </tbody>
  </table>

