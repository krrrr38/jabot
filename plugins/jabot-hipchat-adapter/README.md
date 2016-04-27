# jabot-hipchat-adapter

[HipChat](https://hipchat.com/) adapter for Jabot

1. Access Integrations page https://your.hipchat.com/addons/
2. Select a room
3. Build your own integration
  - `Name your integration`: what you want (e.g. jabot)
  - Copy `Send messages to this room by posting to this URL` into plugins.yml
  - Check `Add a command`
  - `Enter your slash command`: "/jabot" (see plugins.yml)
  - `We will POST to this URL`: your webhook endpoint (e.g. http://exmaple.com:4000)
    - :fire: http access point to receive webhook is required :fire:

plugins.yml
```yml
name: jabot
adapter: # require one adapter
  plugin: com.krrrr38.jabot.plugin.adapter.HipChatAdapter
  namespace: hipchat-adapter
  options:
    postUrl: https://your.hipchat.com/v2/room/859000/notification?auth_token=cccaBQvNEvn9mp14bbbnjSy5SIZTGADBI4BN1aaa # required
    messageColor: "green" # optional (default "gray")
    messageNotify: false # optional (default false)
    webhookPort: 4000 # optional (default 4000)
    slashCommand: "/jabot" # optional (default "/" + your bot name)
```
