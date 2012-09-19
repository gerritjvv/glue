<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
  <head>
    <meta name="layout" content="main" />
    <title>Glue monitoring for ${url}</title>
    <script type="text/javascript">
      var oldData=" ";
      getList = function(data) {
       $('#youradhere').css({ opacity: 0.5 }).html(data).animate({opacity:1});

       $('.jobchecks').change(function(){
         if($('.jobchecks:checked').size()==0) {
             $('#refreshbutton').attr("value","Refresh");
         }
         else{
          $('#refreshbutton').attr("value","Kill selected jobs");
          }
        })
      }



      $(document).ready(function(){

        $('#listform').submit(function() {
                $.ajax({
                  type: "POST",
                  url: "kill",
                  data: $('#listform').serialize(),
                  success: function() {
                       $.ajax({
                          type: "GET",
                          url: "list",
                          success: getList
                          })
                  }
          //$('#st').html('Handler for .submit() called. <br />'+  $('#listform').serialize());

       });

        return false;
      });



     $.PeriodicalUpdater({
	      url : 'list',
	      minTimeout: 60000,
	      maxTimeout: 80000,
	      multiplier: 2

	   },
	   function(data) {
              if($('.jobchecks:checked').size()==0){
                getList(data)
              }
           });

      
    })

    </script>
    <title>Glue monitoring for ${url}</title>

  </head>
  <body>
    <h1>Glue monitoring for ${url} </h1>
    <span id="st"></span>
    <form action="kill" id="listform">
      <input type="submit" name="submit" value="Refresh" id="refreshbutton">

      <div id="youradhere"></div>

    </form>

    <h2>Modules</h2>
    <div class="modules">
      <ul>
<% modules.each{ name, info ->

  %>
        <li>
          <h3>${name}</h3>
          <a href="#module_${name}" name='module_${name}' onclick="$('#module_${name}').toggle()">(show/hide)</a>
          <pre class="moduleinfo" id="module_${name}">${info}</pre>
        </li>
  <% } %>
      </ul>
    </div>
  </div>
</body>
</html>