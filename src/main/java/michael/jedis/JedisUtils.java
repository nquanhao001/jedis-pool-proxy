package michael.jedis;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.lang.reflect.Method;

/**
 * @author Micha31
 * @date 16/5/2
 */
public class JedisUtils {

    static {
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        jedisPoolConfig.setMaxIdle(200);
        pool=new JedisPool(jedisPoolConfig,"www.hostname.com",6379);
    }

    private static JedisUtils proxyInstance = createProxy();
    private static JedisPool pool;

    /**
     * jedis 不需要缓存Jedis实例 用完 会被代理类自动close()
     * no need to hold a jedis instance,
     * because in the end the proxy class will automate the source closed
     * @return
     */
    public static Jedis getProxy() {
        return proxyInstance.getJedis();
    }

    /**
     *
     * 需要代理的方法
     * 用private修饰 会代理失效
     * the proxy method must use public
     * @return
     */
    public Jedis getJedis() {

        if (pool != null) {
            return pool.getResource();
        } else {
            return null;
        }
    }

    /**
     * 直接在pool里取 缓存Jedis 实例时用此方法,需要手动close()
     * need to close on manual
     * @return
     */
    public static Jedis getInstance(){

        if (pool != null) {
            return pool.getResource();
        } else {
            return  null;
        }
    }

    /**
     * 创建代理实例 静态方法不会被代理 会对 getJedis 方法进行拦截
     * @return
     */
    private static JedisUtils createProxy() {

        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(JedisUtils.class);
        enhancer.setCallback(new MethodInterceptor() {

            public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {
                if (method.getName().equals("getJedis")) {
                    Jedis result = (Jedis) methodProxy.invokeSuper(o, objects);

                    result.close();
                    return result;

                } else {
                    return method.invoke(o, objects);
                }

            }
        });

        return (JedisUtils)  enhancer.create();
    }
}