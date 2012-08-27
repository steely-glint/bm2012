var amqScope = JavaImporter(Packages.org.apache.activemq.ActiveMQConnection,
Packages.org.apache.activemq.ActiveMQConnectionFactory,
Packages.javax.jms.Session,
Packages.javax.jms.MessageConsumer,
Packages.javax.jms.MessageProducer,
Packages.javax.jms.MessageListener,
Packages.javax.jms.Message,
Packages.javax.jms.TextMessage,
Packages.javax.jms.Destination,
Packages.javax.jms.DeliveryMode);
var myAppNum = "9996142361";
var myPublicNo = "+17027510535";
var lastcall =  "http://gont.westhawk.co.uk/bm2012/lastcall.groovy";
var tocall= "http://gont.westhawk.co.uk/bm2012/tocall.groovy";
//
////
//// fake up a URL sluper
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


function enqueue(jmess){
   with(amqScope){
        var connectionFactory = new ActiveMQConnectionFactory("","", "tcp://gont.westhawk.co.uk:61616");
        var upcon = connectionFactory.createConnection();
        upcon.start();

        // Create the session

        var outSess = upcon.createSession(false, Session.AUTO_ACKNOWLEDGE);

        var destination = outSess.createQueue("smstobm");

        var prod = outSess.createProducer(destination);
        // Create the producer.
        prod.setDeliveryMode(DeliveryMode.PERSISTENT);

        var tm = outSess.createTextMessage(jmess);
        prod.send(tm);
        log("--------> sending "+jmess);

        // send something here.
        outSess.close();
        upcon.stop();
    }
}
if (currentCall != null) {
   if (currentCall.network == 'SMS') {
     log("------->sms from the PSTN to OpenBTS");
     var calltoken = currentCall.sessionId;
     var target = currentCall.calledID;
     var caller = currentCall.callerID;
     var num = target;
     log ("------>num = "+num);
     var text = "{from: \""+caller+"\" ,to: \""+num+"\",text: \"" +currentCall.initialText + "\"}";
    enqueue(text);
  }
}else {
        // token init - so an outbound sms
      log("------>sms from the OpenBTS to PSTN to "+to+" from "+ from);
      log("------> message is "+messText);
        message(messText, {
            to:to,
            network:"SMS",
            callerID: myPublicNo,
            channel:"text"
        });
}

