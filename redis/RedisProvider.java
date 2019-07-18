package inetbas.web.outsys.redis;

import java.util.ResourceBundle;

import org.apache.log4j.Logger;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.exceptions.JedisConnectionException;

/**
 * Created by www.bip-soft.com on 2017/3/16.
 */
public class RedisProvider {

//    protected static final Logger LOG = LoggerFactory.getLogger(RedisProvider.class);
    protected static Logger LOG = Logger.getLogger(RedisProvider.class);
    protected static JedisPool jedispool;
    protected static int EXPIRE = 120;

    static {
        initRedis();
    }

    private static synchronized void initRedis() {
        ResourceBundle bundle = ResourceBundle.getBundle("redis");
        if (bundle == null) {
            throw new IllegalArgumentException(
                    "[redis.properties] is not found!");
        }

        EXPIRE = Integer.valueOf(bundle.getString("redis.expire"));
        
        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxTotal(Integer.valueOf(bundle.getString("redis.pool.maxActive")));
        config.setMaxIdle(Integer.valueOf(bundle
                .getString("redis.pool.maxIdle")));
        config.setMaxWaitMillis(Long.valueOf(bundle
                .getString("redis.pool.maxWait")));
        config.setTestOnBorrow(Boolean.valueOf(bundle
                .getString("redis.pool.testOnBorrow")));
        config.setTestOnReturn(Boolean.valueOf(bundle
                .getString("redis.pool.testOnReturn")));
        config.setTimeBetweenEvictionRunsMillis(Long.valueOf(bundle.getString("redis.pool.timeBetweenEvictionRunsMillis")));
        String auth = bundle.getString("redis.auth");

        int db = Integer.parseInt(bundle.getString("redis.database"));
        int timeout = Integer.parseInt(bundle.getString("redis.timeout"));
        jedispool = new JedisPool(config, bundle.getString("redis.ip"),
                Integer.valueOf(bundle.getString("redis.port")),timeout,auth,db);
    }

    /**
     * @return
     */
    public static synchronized Jedis getJedis() {
        Jedis jedis = null;
        try {
            jedis = jedispool.getResource();
        } catch (JedisConnectionException jce) {
            ExceptionUtil.getTrace(jce);
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                ExceptionUtil.getTrace(e);
            }
            jedis = jedispool.getResource();
        }
        return jedis;
    }

    public static synchronized void returnResource(Jedis jedis) {
        if (jedis != null) {
            jedis.close();
        }
    }

    public static void close(){
        if(jedispool==null)
            return ;
        if(!jedispool.isClosed())
            jedispool.close();
    }
}
