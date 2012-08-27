import redis.clients.jedis.*


response.setContentType('text/plain')

Jedis jedis = new Jedis("localhost")
def inner = jedis.get(params.CallerNum);
if (inner == null) inner = "2100";

out.write(inner);
