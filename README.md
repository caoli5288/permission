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
- /permission him xkit.use remove
  - This command remove not expires permission xkit.use from player him.
  - I recommend donot use this operation to remove player's permission,
  - Use permission him xkit.use -7 to withdraw 7day expires is goodway.
- /permission @default_group xkit.use remove
  - This command remove permission xkit.use from group default_group.

## Feature
- Player permission expires.
- Multiple group inherit.
- Database support(no lag).

## Require
- [SimpleORM](https://github.com/caoli5288/SimpleORM/releases)

## License
All source and binary file release under GPLv2.