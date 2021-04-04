# EffectMC
Various effects triggered in Minecraft by devices like an Elgato Stream Deck.

## How to setup:

Download the Stream Deck plugin and Minecraft mod from the [latest release](https://github.com/MoSadie/EffectMC/releases/latest)

Install the Stream Deck plugin by running the plugin file and following the prompt.

Install the Minecraft mod using the standard method for your mod loader. (Usually involves placing the correct jar file into the mods folder)

Add the actions to your Stream Deck! (See [here](docs) for the different types of actions and how to configure them.)

In order to have the buttons actually trigger the effects, you need to first add the device to the trusted devices list. See below for how to do that.

Here is a video running through the setup process: https://youtu.be/l7haArORkNY

## How to trust a device

1) Run the command `/effectmctrust` in Minecraft.
2) Send a effect to trigger the trust prompt in Minecraft. Follow the instructions on the prompt to add the device as a trusted device.

## How to configure the port:

After running the mod one, there should be a `effectmc` directory either with the mod jar or in the game directory. In there will be a config file that allows you to change the port the mod listens on. Don't forget to update your actions as well!
