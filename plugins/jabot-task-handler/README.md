# jabot-task-handler

task management handler for __message sender__

```sh
list tasks # or `tasks`
add task <message>
delete task <index:\\d+>
delete all tasks
```

plugins.yml
```yml
handlers: # messages are sent in this order
  - plugin: com.krrrr38.jabot.plugin.handler.TaskHandler
    namespace: task-handler
```
