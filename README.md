# **ARK Faucet Discord bot**
![ARK Faucet Discord bot logo](https://raw.githubusercontent.com/qvanphong/ark-faucet-discord-bot/main/images/arkfaucetlogo.png)
---
A Discord bot that giving free ARK token (and any token based ARK). With own wallet for every guild.

What this discord bot support:
- [x] ARK (mainnet, devnet)
- [x] BIND (mainnet, devnet)
- [x] bARK
- [x] Qredit

## Setup
- Clone this repo
- run `mvn install`
- Build to jar file using `mvn clean package`
- Setting up your token profiles following `faucet.token-setting-location` in `application.properties`
- run jar file by `java -jar built_jar_file.jar`


## application.properties
| Key                              | Property                                                                                                                                         |
|----------------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------|
| discord-bot.token                | Discord bot token                                                                                                                               |
| discord-bot.owner-id             | Your Discord user id, use to send report to your private message when getting exception and consider you are have permission to limited commands |
| faucet.aslp-api-url              | ASLP RESTful API.                                                                                                                                |
| faucet.default-cool-down-minutes | Default minutes every time user can get reward.                                                                                                  |


## Config JSON Structure (used for /config editconfig)

| Key                | Value   | Required | Description                                                                                |
|--------------------|---------|----------|--------------------------------------------------------------------------------------------|
| name               |  string |     ✔️    | Token name, value must match with choice option in faucet.json                             |
| symbol             |  string |     ✔️    | Token symbol/ticker. In case using special character, convert it to Java Entity first      |
| explorer_url       |  string |     ✔️    | Explorer URL                                                                               |
| api_url            |  string |     ✔️    | Chain's RESTful API URL                                                                    |
| network            |  number |     ✔️    | Network public key hash                                                                    |
| reward_amount      |  number |     ✔️    | Reward that user can claim. Currency is arktoshi (100.000.000 arktoshi = 1 ARK)            |
| fee                |  number |     ✔️    | Transaction fee. Currency is arktoshi                                                      |
| sender_address     |  string |     ✔️    | Address of wallet that use to work with Faucet.                                            |
| passphrase         |  string |     ✔️    | Wallet passphrase                                                               |
| passphrase         |  string |          | Wallet second passphrase                                                               |
| allow_vendor_field | boolean |          | Is chain allow vendor field in transaction. Default is true.                               |
| vendor_field       | string  |          | Vendor field a.k.a transaction message. Default is "From ARK Faucet Discord bot with love" |
| is_aslp            | boolean |          | Is ASLP token. default is false                                                            |
| aslp_reward        | number  |          | ASLP Token reward, human readable value. (ex: 1 bARK = 1 bARK, not like arktoshi)          |
| aslp_token_id      | string  |          | ASLP Token ID                                                                              |
| aslp_api_url       | string  |          | ASLP RESTful API URL                                                                       |
