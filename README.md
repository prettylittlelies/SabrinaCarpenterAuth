<div align="center">

# Sabrina Carpenter auth

![GitHub stars](https://img.shields.io/github/stars/prettylittlelies/SabrinaCarpenterAuth&color=yellow)
![GitHub forks](https://img.shields.io/github/stars/prettylittlelies/SabrinaCarpenterAuth)
![License](https://img.shields.io/github/license/prettylittlelies/SabrinaCarpenterAuth)
![Minecraft](https://img.shields.io/badge/minecraft-1.8.9-green)
![Forge](https://img.shields.io/badge/forge-MCP-blue)

session auth mod for 1.8.9 forge with full account management, sqlite storage, skin/cape previews and bulk export

</div>

---

### what it does

- login with session tokens and auto-save accounts to a local sqlite db
- scrollable account list with player heads pulled from mc-heads.net
- select an account to see full body render + cape + all account info
- export accounts individually or all at once as json
- session validation on the multiplayer screen
- restore original session anytime
- open per-account folders straight from the gui

### install

grab the jar from [releases](https://github.com/prettylittlelies/SabrinaCarpenterAuth/releases) and throw it in your mods folder

### build from source

```
./gradlew shadowJar
```

jar goes to `build/libs/`

### apis used

- **api.minecraftservices.com** — profile data, skins, capes, token validation
- **mc-heads.net** — head and body renders
- **crafthead.net** — cape textures

### license

GPL-3.0
