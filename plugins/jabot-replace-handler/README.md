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
handlers: # messages are sent in this order
  - plugin: com.krrrr38.jabot.plugin.handler.ReplaceHandler
    namespace: relpace-handler
```

__NOTE__: ReplaceHandler should be put first in handlers to pass replaced message to other handlers.
