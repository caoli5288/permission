#  Permission
This is a light weight permission plugin for bukkit based minecraft server.

## Command
- /permission him
  - This command query all permission not outdated for player named him.
- /permission @default_group
  - This command query all permission for group named default_group.
- /permission him xkit.use 7
  - This command give permission xkit.use to player him with 7day expires.
  - Multiple this command will multiple expires time.
- /permission him @default_group 7
  - This command make player him inherit group default_group with 7day expires.
- /permission @vip_group @default_group
  - This command make group vip_group inherit group default_group.
- /permission @default_group xkit.use
  - This command give permission xkit.use to group default_group.
- /permission him xkit.use cancel
  - This command remove not expires permission xkit.use from player him.
  - I recommend donot use this operation to remove player's permission,
  - Use permission him xkit.use -7 to withdraw 7day expires is goodway.
- /permission @default_group xkit.use cancel
  - This command remove permission xkit.use from group default_group.
- /permission him ?xkit.use 7
  - This command give player him xkit.use extra 7 day if already had xkit.use permission(silence ignore if not).

## Permission
- permission.use
  - `Basic permission that player can use /permission command query self(suggest not give to everyone).`
- permission.admin
  - `Administrator permission.`
  
## Placeholder
- permission_expire_any.permission
  - Return `any.permission`'s outdated time by unix time stamp.
- permission_expiretime_@anygroup
  - Return `@anygroup`'s outdated time by human readable format.

## Require
- [SimpleORM](https://github.com/caoli5288/SimpleORM/releases)

## License
All source and binary file release under GPLv2.