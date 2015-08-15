# jabot-slack-adapter

[Slack](https://slack.com/) adapter for Jabot

1. Add Bots integration https://slack.com/services/new/bot
2. enter bot name which is same with `name` in plugins.yml
3. get token and edit plugins.yml like followings

plugins.yml
```yml
name: jabot
adapter: # require one adapter
  plugin: com.krrrr38.jabot.plugin.adapter.SlackAdapter
  namespace: slack-adapter
  options:
    token: "xoxb-1111111111-AAAAAAAAAAAAAAAAAAA" # required
    channel: "general" # required
```
