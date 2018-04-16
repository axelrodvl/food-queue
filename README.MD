# Food queue
Organize electronic queue for a corporate event with catering (i.e. burgers, ice cream and etc.).

Provides Telegram bot and web interface for end-users.

Integrated with Microsoft Exchange for authorization.

## Preparing
- Create [Telegram bot](https://core.telegram.org/bots#creating-a-new-bot) and retrieve the token
- Set Telegram bot token by implementing `co.axelrod.foodqueue.config.token.TelegramTokenStorage.java`
- Fill `application.properties` with proper settings
    - Server - your machine's public IP (would be used for web interface)
    - LDAP - your corporate domain (i.e. `domain.com`)

## Running
- Run application and database (MongoDB) by Docker Compose
    ``
    chmod +x run.sh
    ./run.sh
    ``

## Using
- End-user
    - Authorize and enqueue through Telegram bot
    - Or use web interface at `${server.host}:${server.port}`
- Admin
    - Start kitchen with `${server.host}:${server.port}/control/start`
    - Add queue number with `${server.host}:${server.port}/control/itReady`
    - Stop kitchen with `${server.host}:${server.port}/control/start`
    
## Notes
- MongoDB would start inside Docker container without mounting local storage.
If you want to keep data after restart - please, [mount local directory](https://hub.docker.com/_/mongo/).