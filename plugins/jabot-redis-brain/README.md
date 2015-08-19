# jabot-redis-brain

Redis brain for Jabot

plugins.yml
```yml
brain:
  plugin: com.krrrr38.jabot.plugin.brain.RedisBrain
  namespace: redis-brain
  options: # all options are optional
    # redis db settings
    host: 127.0.0.1
    port: 6379
    password: password
    connectionTimeout: 2000
    socketTimeout: 2000
    database: 0
    # redis connection pool settings
    maxTotal: 8
    maxIdle: 8
    minIdle: 0
    testWhileIdle: true
    testOnBorrow: false
    testOnReturn: false
```

To pass the test, you need to start redis server on `127.0.0.1:6379`.
