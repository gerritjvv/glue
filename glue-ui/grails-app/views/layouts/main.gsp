<html xmlns="http://www.w3.org/1999/xhtml">
  <head>
    <script >
      var tb_pathToImage = "${resource(dir:'images',file:'loadingAnimation.gif')}";
    </script>
    <link rel="stylesheet" href="${resource(dir:'css',file:'main.css')}" />
    <link rel="stylesheet" href="${resource(dir:'css',file:'thickbox.css')}" />
    <g:javascript library="jquery"/>
    <script type="text/javascript" src="${resource(dir:'js',file:'thickbox-compressed.js')}"></script>
    <script type="text/javascript" src="${resource(dir:'js',file:'jquery.periodicalupdater.js')}"></script>

  <g:layoutHead />
</head>
<body>

  <div class="container">
    <div id=menu>
      <ul>
        <li><g:link controller="glue" action="index">Monitoring</g:link></li>
        <li><g:link controller="stats" action="index">Stat</g:link></li>
         <glue:pluginMenuItems/>
      </ul>
      <g:include controller="dsSelector" action="loadDsList"/>
    </div>
    <g:layoutBody />
    <hr style="visibility:hidden;clear:both"/>
  </div>


</body>	
</html>