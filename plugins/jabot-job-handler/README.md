# jabot-job-handler

Schedule message management handler.

```sh
list jobs
add job "<cron syntax>" <message> 
delete job <id>
```

plugins.yml
```yml
handlers: # messages are sent in this order
  - plugin: com.krrrr38.jabot.plugin.handler.JobHandler
    namespace: job-handler
```
