# **ARK Faucet Discord bot**
---
A Discord bot that giving free ARK token (and any token based ARK). With own wallet for every guild.

What this discord bot support:
- [x] ARK (mainnet, devnet)
- [x] BIND (devnet, waiting to test mainnet)
- [x] bARK
- [ ] Qredit

## Setup
- Clone this repo
- run `mvn install`
- Build to jar file using `mvn clean package`
- Setting up your token profiles following `faucet.token-setting-location` in `application.properties`
- run jar file by `java -jar built_jar_file.jar`


## application.properties setup
```
# Discord bot setup
discord-bot.token=
discord-bot.owner-id=

# Database setup
spring.datasource.url=
spring.datasource.driverClassName=
spring.datasource.username=
spring.datasource.password=
spring.jpa.database-platform=
spring.jpa.hibernate.ddl-auto=update

# faucet setup
faucet.token-setting-location=
faucet.aslp-api-url=https://aslp.qredit.dev/api/
faucet.default-cool-down-minutes=180
```


## Token profile setting
- Create [token_symbol].token file following the that you setup in `faucet.token-setting-location` in `application.properties`/[guild_id]/

(token_symbol must match `value` value in `src\main\resources\commands\faucet.json`, for example: I setting up 3 token in faucet.json, these are ark, bind, bark (following their `value` key), the token profile file must be named ark.token, bind.token, bark.token)
- the json structure following `TokenConfig.java` structure
```
{
    "sender_address": "",
	"explorer_url": "https://explorer.ark.io/",
	"api_url": "https://api.ark.io/api/",
	"reward_amount": 1,
	"fee": 800000,
	"network": 23,
	"symbol": "ARK",
	"is_aslp": true,
	"aslp_reward": 1,
	"aslp_token_id": "",
	"passphrase": ""
}
```
(no need to setup these aslp if you are not using aslp token)