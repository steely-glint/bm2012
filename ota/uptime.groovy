response.setContentType('application/json')
Double t  = new Double((new File('/proc/uptime')).getText().split(" ")[0]);
def message = "This server has been up "+ (int)(t / (60*60*24))+ " days";
message += " and "+ (int)((t % (60*60*24))/60)+ " minutes."
def tropo = "{'tropo':[ { 'say': [ {'value':'${message}'}]}]}";
tropo = tropo.replaceAll("'",'"');


out.write(tropo);
