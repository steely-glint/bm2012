var myAppNum = "9996142361";
var myPublicNo = "+17027510535";
var calltoken = currentCall.sessionId;
var target = currentCall.calledID;
var caller = currentCall.callerID;
var lastcall =  "http://gont.westhawk.co.uk/bm2012/lastcall.groovy";
var tocall= "http://gont.westhawk.co.uk/bm2012/tocall.groovy";

log ("-------------> target = "+target);
var to = "";
var from = caller;
//
//
// fake up a URL sluper
function fetch(urlStr) {
 log ("-------------> url is "+urlStr);
 importPackage(java.io, java.net);
   var url = new URL(urlStr);
   var urlStream = url.openStream();
   var reader = new BufferedReader(new InputStreamReader(urlStream, "latin1"));
   var html = "";
   var line;
   while (line = reader.readLine()) {
     if (line == null) break;
     html += line;
   }
   log ("-------------> reply is  "+html);
   return html;
}



if (target == myAppNum){
    to = currentCall.getHeader("x-numbertodial");
    imsi= currentCall.getHeader("x-fromnumber");
    from = myPublicNo; 
    if (to.indexOf("+")==0){
	    to = to.substring(1);
    }
    log("-------------> call from OpenBTS "+caller+" to PSTN " + to);
    fetch(tocall+"?CalledNum="+to+"&CallerNum="+imsi);
} else {
    target = fetch(lastcall+"?CallerNum=1"+caller);
    to = "sip:"+target+"@gont.westhawk.co.uk";
    log("-------------> call from PSTN "+caller+" to OpenBTS " + target);
}
transfer(to, {
	     callerID: from,
	     playvalue: "http://www.phono.com/audio/holdmusic.mp3",
	     terminator: "*",
	     onTimeout: function(event) {
	       say("Sorry, but nobody answered");
	    }
});
hangup();
