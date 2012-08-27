import redis.clients.jedis.*


response.setContentType('application/json')

Jedis jedis = new Jedis("localhost")
def t = jedis.incr("beanCounter");
def message = "this script has been run ${t} times."
def tropo = "{'tropo':[ { 'say': [ {'value':'${message}'}]}]}";
tropo = tropo.replaceAll("'",'"');


out.write(tropo);
