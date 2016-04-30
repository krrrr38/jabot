# jabot-memo-handler

all user's sharing memo by key

```sh
list memos # show memo key list or `memos`
memo <key> # show memo
add memo <key> <memo>
delete memo <key>
delete all memos
```

plugins.yml
```yml
handlers: # messages are sent in this order
  - plugin: com.krrrr38.jabot.plugin.handler.MemoHandler
    namespace: memo-handler
```
