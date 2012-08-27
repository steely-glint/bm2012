import groovy.xml.QName;


def sdpIn = request.getReader().readLines();
System.err.println("read sdp in ${sdpIn.size()} lines");
        
def audioOnly(lines) {        
        def copying = true;

        
        def audio_only = lines.collect{ line ->
            if ( line.startsWith("m=audio")) copying= true;
            if ( line.startsWith("m=video")) copying= false;
            return copying?line:null;
        };
        def out=  audio_only.findAll{it != null}
        return out;
}

def findCandidates(lines){
        def candidates = [];
        lines.each { 
            if (it.startsWith("a=candidate")){
                def bits = it.split(" ");
                if (bits[2].equals("udp")){
                    def cand = [:];
                    cand.foundation=bits[0].substring("a=candidate:".length());
                    cand.componentId = bits[1];
                    cand.transport = bits[2];
                    cand.priority = bits[3];
                    cand.connectionAddress = bits[4];
                    cand.port = bits[5];
                    for (int i=6; i<bits.length-1;i+=2){
                        cand.put(bits[i],bits[i+1]);
                    }
                    candidates.add(cand);
                }
            }
        };
        return candidates
}
def findCrypto(lines){
        def cryptoline = lines.find{
            (it.startsWith("a=crypto") && (it.indexOf("AES_CM_128_HMAC_SHA1_80") > 0))
        };
        def crypto = [:];
        def bits = cryptoline.split(" ");
        crypto.tag = bits[0].substring("a=crypto:".length());
        def keySpec = bits.find{ it.startsWith("inline:") };
        crypto.ks = keySpec.substring("inline:".length());
        if (crypto.ks.indexOf("|") > 0) {
            String[] kbits = crypto.ks.split("\\|");
            crypto.ks = kbits[0];
                // now deal with the ugly optional syntax
            if ((kbits.length > 1) && kbits[1].contains(":")) {
                crypto.mkil = kbits[1];
            } else {
                if (kbits.length > 1) {
                    crypto.life = kbits[1];
                }
                if ((kbits.length > 2) && kbits[1].contains(":")) {
                    crypto.mkil = kbits[2];
                }
            }
        }
        return crypto; 
}

def findAudio(lines){
    def audioLine = lines.find{ it.startsWith("m=audio") }
    def audio=[:];
    String[] bits = audioLine.split(" ");
    audio.port = bits[1];
    audio.protocol = bits[2];
    def ptypes = [];
    for (int i =3; i<bits.length;i++){ ptypes.add(bits[i])};
    audio.ptypes = ptypes;
    return audio;
}

def findRTPmap(lines){
    def rtplines = lines.findAll{ it.startsWith("a=rtpmap:") };
    def ret = rtplines.collect { 
        def r = [:];
        def bits = it.split(" ");
        r.ptype = bits[0].substring("a=rtpmap:".length());
        def nsbits = bits[1].split("/");
        r.name = nsbits[0];
        r.rate = nsbits[1];
        return r;
    };
}

def findIceFrag(lines){
    def ret = [:];
    def iceLines = lines.findAll{ it.startsWith("a=ice-")};
    iceLines.each{
        def bits = it.split(":");
        def key = bits[0].substring("a=ice-".length());
        ret.put(key,bits[1]);
    }
    return ret;
}

def addPayloads(x,rtpmap){
   def ret = x.get("description")[0];
   rtpmap.each{
       def n= new Node(ret, "payload-type",[id:it.ptype , name:it.name , clockrate:it.rate]);
   }
   return ret;
}

def addCandidates(x,cands){
   def ret = x.get("transport")[0];
   cands.each{
       def n= new Node(ret, "candidate",[ip:it.connectionAddress , port:it.port , generation:it.generation , component:it.componentId]);
   }
   return ret;
}

def addCrypto(x,cr){
   def ret = x.get("description")[0].get("encryption")[0];
   def n= new Node(ret, "crypto",[tag:cr.tag , 'crypto-suite':"AES_CM_128_HMAC_SHA1_80" , 'key-params':cr.ks] );
   return ret;
}


def audio = audioOnly(sdpIn)
def candidates = findCandidates(audio);
System.err.println("->>>>> candidates"); 
candidates.each{ System.err.println it};

def crypto =  findCrypto(audio);
System.err.println("->>>>> crypto"); 
System.err.println(crypto);

def audioL = findAudio(audio);
System.err.println ("->>>>> audio");
System.err.println(audioL);

def rtpmap = findRTPmap(audio);
System.err.println ("->>>>> rtpmap");
System.err.println(rtpmap);

def icefrag = findIceFrag(audio);
System.err.println ("->>>>> icefrag");
System.err.println(icefrag);


// now create some xml....
String sample = "<content xmlns='urn:xmpp:jingle:1' creator='initiator' name='voice'>\
    <description xmlns='urn:xmpp:jingle:apps:rtp:1' media='audio'>\
    <encryption required='0'>\
    </encryption> \
    </description>\
    <transport xmlns='urn:xmpp:jingle:transports:raw-udp:1'> \
    </transport> \
    </content>";

def jMess = new XmlParser().parseText(sample);
addPayloads(jMess,rtpmap);
addCandidates(jMess,candidates);
addCrypto(jMess,crypto);


response.setContentType('text/xml')
def printer = new XmlNodePrinter(new PrintWriter(out))
printer.preserveWhitespace = true
printer.print(jMess)


