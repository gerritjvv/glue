function colorformat(objSelector,fromColorS,toColorS,ignoreZero){
    var values=new Array();
    var objects=new Array();
    $(objSelector).each(function(index,obj){
        var v=Number($(obj).text().replace(/\,/g,''));
        if(v!=0 || !ignoreZero)
        {
          values.push(v);
          objects.push($(obj));
        }
        else
        {
          $(obj).addClass("zeroValue");  
        }
    })
    //alert(values);

 
    var fromColor=strToRGB(fromColorS);
    var toColor=strToRGB(toColorS);

    if(values.length==0) return;
    var minv=values[0];
    var maxv=values[0];
    for(var i=1;i<values.length;i++)
    {    if(minv>values[i]) minv=values[i];
            if(maxv<values[i]) maxv=values[i];
     }
    if(minv==maxv) return;
    for(var i=0;i<values.length;i++)
    {
            var val=(values[i]-minv)/(maxv-minv);
            var red = mid(fromColor[0],toColor[0],val)
            var green = mid(fromColor[1],toColor[1],val)
            var blue = mid(fromColor[2],toColor[2],val)
            var fontColour = brightness(red,green,blue) > 130 ? "black" : "white"
         objects[i].css("background-color","rgb("+red+","+green+","+blue+")")
         objects[i].css("color",fontColour)
                //objects[i].attr("style","background-color:rgb("+mid(fromColor[0],toColor[0],val)+","+mid(fromColor[1],toColor[1],val)+","+mid(fromColor[2],toColor[2],val))
            }
                    
    //alert(values);
}



function mid(from,to,q) {
  return parseInt(from + (to-from)*q)
}

function strToRGB(str) {
    function hexToR(h) {return parseInt((cutHex(h)).substring(0,2),16)}
    function hexToG(h) {return parseInt((cutHex(h)).substring(2,4),16)}
    function hexToB(h) {return parseInt((cutHex(h)).substring(4,6),16)}
    function cutHex(h) {return (h.charAt(0)=="#") ? h.substring(1,7):h}
    return new Array(hexToR(str),hexToG(str),hexToB(str));
}

function brightness(r,g,b){
    
    return Math.sqrt((r*r*0.241)+(g*g*0.691)+(b*b*0.068))
}




/***
You can specify a midpoint value, say 100, so that values closer to 100 will be more 
'toColourS' colour whereas values further away from 100 will have more of the 'fromColourS' colour.
***/
function colorformatMidpoint(objSelector,fromColorS,toColorS,midpointValue,ignoreZero){
    var values=new Array();
    var objects=new Array();
    $(objSelector).each(function(index,obj){
        var v=Number($(obj).text().replace(/\,/g,''));
        if(v!=0 || !ignoreZero)
        {
          values.push(v);
          objects.push($(obj));
        }
        else
        {
          $(obj).addClass("zeroValue");  
        }
    })

    var fromColor=strToRGB(fromColorS);
    var toColor=strToRGB(toColorS);

    if(values.length==0) return;
    var minv=values[0];
    for(var i=1;i<values.length;i++) {
       if(minv>values[i]) minv=values[i];
     }


    for(var i=0;i<values.length;i++)  {
       var val=(values[i])/(midpointValue);
       if(values[i]>midpointValue){
        val = 1 - (values[i]-midpointValue)/(midpointValue);
       }
      objects[i].css("background-color","rgb("+mid(fromColor[0],toColor[0],val)+","+mid(fromColor[1],toColor[1],val)+","+mid(fromColor[2],toColor[2],val)+")")                
   }                    

}



