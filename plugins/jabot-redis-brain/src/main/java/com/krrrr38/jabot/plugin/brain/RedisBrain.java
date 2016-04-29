package com.krrrr38.jabot.plugin.brain;

import java.util.Map;
import java.util.Optional;

import org.apache.commons.pool2.impl.BaseObjectPoolConfig;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.Protocol;

public class RedisBrain extends Brain {

    // basic db settings
    private static final String OPTIONS_DB_HOST = "host";
    private static final String OPTIONS_DB_PORT = "port";
    private static final String OPTIONS_DB_PASSWORD = "password";
    private static final String OPTIONS_DB_CONECTION_TIMEOUT = "connectionTimeout";
    private static final String OPTIONS_DB_SOCKET_TIMEOUT = "socketTimeout";
    private static final String OPTIONS_DB_DATABASE = "database";
    private static final String OPTIONS_DB_CLIENT_NAME = "clientName";
    // pool settings
    private static final String OPTIONS_POOL_MAX_TOTAL = "maxTotal";
    private static final String OPTIONS_POOL_MAX_IDLE = "maxIdle";
    private static final String OPTIONS_POOL_MIN_IDLE = "minIdle";
    private static final String OPTIONS_POOL_TEST_WHILE_IDLE = "testWhileIdle";
    private static final String OPTIONS_POOL_TEST_ON_BORROW = "testOnBorrow";
    private static final String OPTIONS_POOL_TEST_ON_CREATE = "testOnCreate";
    private static final String OPTIONS_POOL_TEST_ON_RETURN = "testOnReturn";

    private JedisPool pool;

    @Override
    public void afterSetup(Map<String, String> options) {
        // basic db settings
        String host = optionString(options, OPTIONS_DB_HOST, Protocol.DEFAULT_HOST);
        int port = optionInteger(options, OPTIONS_DB_PORT, Protocol.DEFAULT_PORT);
        String password = optionString(options, OPTIONS_DB_PASSWORD, null);
        int connectionTimeout = optionInteger(options, OPTIONS_DB_CONECTION_TIMEOUT, Protocol.DEFAULT_TIMEOUT);
        int socketTimeout = optionInteger(options, OPTIONS_DB_SOCKET_TIMEOUT, Protocol.DEFAULT_TIMEOUT);
        int database = optionInteger(options, OPTIONS_DB_DATABASE, Protocol.DEFAULT_DATABASE);
        String clientName = optionString(options, OPTIONS_DB_CLIENT_NAME, null);

        // pool settings
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        jedisPoolConfig.setMaxTotal(optionInteger(options, OPTIONS_POOL_MAX_TOTAL, GenericObjectPoolConfig.DEFAULT_MAX_TOTAL));
        jedisPoolConfig.setMaxIdle(optionInteger(options, OPTIONS_POOL_MAX_IDLE, GenericObjectPoolConfig.DEFAULT_MAX_IDLE));
        jedisPoolConfig.setMinIdle(optionInteger(options, OPTIONS_POOL_MIN_IDLE, GenericObjectPoolConfig.DEFAULT_MIN_IDLE));
        jedisPoolConfig.setTestWhileIdle(optionBoolean(options, OPTIONS_POOL_TEST_WHILE_IDLE, true)); // JedisPoolConfig
        jedisPoolConfig.setTestOnBorrow(optionBoolean(options, OPTIONS_POOL_TEST_ON_BORROW, BaseObjectPoolConfig.DEFAULT_TEST_ON_BORROW));
        jedisPoolConfig.setTestOnCreate(optionBoolean(options, OPTIONS_POOL_TEST_ON_CREATE, BaseObjectPoolConfig.DEFAULT_TEST_ON_CREATE));
        jedisPoolConfig.setTestOnReturn(optionBoolean(options, OPTIONS_POOL_TEST_ON_RETURN, BaseObjectPoolConfig.DEFAULT_TEST_ON_RETURN));
        pool = new JedisPool(jedisPoolConfig, host, port, connectionTimeout, socketTimeout, password, database, clientName);
    }

    @Override
    public void beforeDestroy() {
        if (pool != null && !pool.isClosed()) {
            pool.close();
        }
    }

    @Override
    public Map<String, String> getAll(String namespace) throws JabotBrainException {
        return wrap(jedis -> jedis.hgetAll(namespace));
    }

    @Override
    public Optional<String> get(String namespace, String key) throws JabotBrainException {
        return wrap(jedis -> Optional.ofNullable(jedis.hget(namespace, key)));
    }

    @Override
    public boolean store(String namespace, String key, String value) throws JabotBrainException {
        return wrap(jedis -> jedis.hset(namespace, key, value) > 0);
    }

    @Override
    public boolean store(String namespace, String key1, String value1, String key2, String value2) throws JabotBrainException {
        return wrap(jedis -> {
            jedis.hset(namespace, key1, value1);
            jedis.hset(namespace, key2, value2);
            return true;
        });
    }

    @Override
    public boolean store(String namespace, String key1, String value1, String key2, String value2, String key3, String value3) throws JabotBrainException {
        return wrap(jedis -> {
            jedis.hset(namespace, key1, value1);
            jedis.hset(namespace, key2, value2);
            jedis.hset(namespace, key3, value3);
            return true;
        });
    }

    @Override
    public boolean storeAll(String namespace, Map<String, String> keyvalues) throws JabotBrainException {
        return wrap(jedis -> {
            for (Map.Entry<String, String> entry : keyvalues.entrySet()) {
                jedis.hset(namespace, entry.getKey(), entry.getValue());
            }
            return true;
        });
    }

    @Override
    public boolean delete(String namespace, String key) throws JabotBrainException {
        return wrap(jedis -> jedis.hdel(namespace, key) > 0);
    }

    @Override
    public boolean clear(String namespace) throws JabotBrainException {
        return wrap(jedis -> jedis.del(namespace) > 0);
    }

    @Override
    public boolean isStored(String namespace, String key) throws JabotBrainException {
        return wrap(jedis -> jedis.hexists(namespace, key));
    }

    private <T> T wrap(ThrowableFunction<T> f) throws JabotBrainException {
        try (Jedis jedis = pool.getResource()) {
            return f.apply(jedis);
        } catch (Exception e) {
            throw new JabotBrainException(e);
        }
    }

    @FunctionalInterface
    public interface ThrowableFunction<T> {
        T apply(Jedis jedis) throws Exception;
    }
}
