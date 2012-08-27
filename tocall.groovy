import redis.clients.jedis.*


response.setContentType('text/plain')

Jedis jedis = new Jedis("localhost")
jedis.set(params.CalledNum,params.CallerNum);
jedis.expire(params.CalledNum, 3600)

out.write("Ok");
