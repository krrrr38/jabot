# jabot-replace-handler

Replace given message with registered patterns for other handlers.

```sh
list patterns
replace <from> with <to>
delete pattern <from>
delete all patterns
```

plugins.yml
```yml
handlers:
  - plugin: com.krrrr38.jabot.plugin.handler.ReplaceHandler
    namespace: relpace-handler
```
