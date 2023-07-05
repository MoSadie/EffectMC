# EffectMC for Minecraft 1.8.9

## DISCLAIMER

This is not going to get updated often because the tools are extremely out of date & hard to use compared to modern tools. 1.8.9 has been unsupported for years as a modding version at this point. There are no plans to backport to any other version of Minecraft.

This was a "I wonder if I could" situation. You'll also notice I did not add this to the CI build system, this version will only be manually built.

For the Stream Deck plugin, use the version from [this release (v2.2.0)](https://github.com/MoSadie/EffectMC/releases/tag/v2.2.0). It still has the built-in web dashboard and accepts the same http requests, if alternate methods are needed.

## Effect Changes

Due to the large Minecraft version difference, some effects do not work in the usual way:

- [Show Toast](https://github.com/MoSadie/EffectMC/wiki/show-toast) is a modified achievement toast, so the "Achievement Get!" text will show up.
- [Show Action Message](https://github.com/MoSadie/EffectMC/wiki/show-action-message) uses the "Now Playing" message, so the text will be rainbow by default.
- [Stop Sound](https://github.com/MoSadie/EffectMC/wiki/stop-sound) only works with stopping all sounds. (Use 'all' as the sound name)
- [Open Book](https://github.com/MoSadie/EffectMC/wiki/open-book) does not work.
- [Narrate](https://github.com/MoSadie/EffectMC/wiki/narrate) does not work.
- [Open Screen](https://github.com/MoSadie/EffectMC/wiki/open-screen) may crash your game. Sorry.
