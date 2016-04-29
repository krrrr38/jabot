# jabot-health-check-handler

Health Check with http request every minute

check response http status is 200 ~ 399 or not

```sh
list health-check
add health-check <GET|HEAD> <url> <memo>
suspend health-check <index:\\d+>
suspend all health-check
resume health-check <index:\\d+>
resume all health-check
delete health-check <index:\\d+>
delete all health-check
```

plugins.yml
```yml
handlers: # messages are sent in this order
  - plugin: com.krrrr38.jabot.plugin.handler.HealthCheckHandler
    namespace: health-check-handler
    options:
      intervalCronSyntax: "* * * * *" # cron syntax
      alertPrefix: "[ERROR] @all " # alert message prefix
      connectTimeout: 2000 # http request connect timeout
      socketTimeout: 3000 # http request socket timeout
      userAgent: jabot-health-check # http request user agent
      customHeaderName: X-My-Header # http request custom header name
      customHeaderValue: x-my-header-value # http request custom header value
```
