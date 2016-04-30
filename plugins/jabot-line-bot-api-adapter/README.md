# jabot-line-bot-api-adapter

[LINE Bot](https://developers.line.me/bot-api/overview) adapter for Jabot

1. Create BOT API from https://business.line.me
2. Get Bot Information from https://developers.line.me/channels/...
  - Copy `Channel ID` as `channelId` in plugins.yml
  - Copy `Channel Secret` as `channelSecret` in plugins.yml
  - Copy `MID` as `botMid` in plugins.yml
  - Enter `Callback URL`: your webhook endpoint (e.g. https://exmaple.com:433 to proxy your local api)
    - :fire: http access point to receive webhook is required :fire:

plugins.yml
```yml
name: jabot
adapter: # require one adapter
  plugin: com.krrrr38.jabot.plugin.adapter.LineBotApiAdapter
  namespace: line-bot-api-adapter
  options:
    channelId: 1234567890 # required
    channelSecret: "Channel Secret" # required
    botMid: "MID" # required
    webhookPort: 4000 # optional (default 4000)
```
