## **ARK Faucet Discord bot**
---
A Discord bot that giving free ARK token (and any token based ARK).

What this discord bot support:
- [x] ARK (mainnet, devnet)
- [x] BIND (devnet, waiting to test mainnet)
- [ ] bARK 
- [ ] Qredit

#### Setup
- Clone this repo
- run `mvn install`
- Build to jar file or run with Spring Boot

#### application.properties setup
```# Discord bot setup
discord-bot.token=

# App setup
app.passphrase-location=D:\\

# Bind Token
app.token.bind.sender-address=
app.token.bind.explorer-url=https://testnet.bindscan.io/
app.token.bind.api-url=https://api.nos.dev/api/v2/
app.token.bind.allow-vendor-field=false
# reward: 1 BIND, fee: 0.1 BIND
app.token.bind.fee=10000000
app.token.bind.reward-amount=100000000
app.token.bind.network=90
app.token.bind.token-symbol=\u00DF

# ARK Token
app.token.ark.sender-address=
app.token.ark.explorer-url=https://dexplorer.ark.io/
app.token.ark.api-url=http://167.114.43.43:4003/api/
app.token.ark.backup-api-url[0]=http://167.114.43.43:4003/api/
# reward: 1 ARK, fee: 0.008ARK
app.token.ark.reward-amount=100000000
app.token.ark.fee=800000
app.token.ark.network=30
app.token.ark.token-symbol=DARK

#....
```