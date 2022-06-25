<!-- modrinth_exclude.start -->

# Pipez ![](http://cf.way2muchnoise.eu/full_443900_downloads.svg) ![](http://cf.way2muchnoise.eu/versions/443900.svg)

## Links

- [CurseForge](https://www.curseforge.com/minecraft/mc-mods/pipez)
- [ModRepo](https://modrepo.de/minecraft/pipez/overview)
- [GitHub](https://github.com/henkelmax/pipez)
- [FAQ](https://modrepo.de/minecraft/pipez/faq)
- [Credits](https://modrepo.de/minecraft/pipez/credits)

---

<!-- modrinth_exclude.end -->

This mod adds simple and highly configurable pipes that are designed to create as little lag as possible.

![](https://i.imgur.com/F3Ja4m4.png)

## Pipe Types

- Item Pipes
- Fluid Pipes
- Energy Pipes
- Gas Pipes ([Mekanism](https://www.curseforge.com/minecraft/mc-mods/mekanism))
- Universal Pipes (All 4 Pipes in one!)

## Features

- Highly configurable filter system
  - Black-/Whitelisting
  - Three redstone modes (ignore/on when powered/off when powered)
  - Four distribution modes (round robin/nearest first/furthest first/random)
  - Tag filtering
  - Item/Fluid/Gas filtering
  - Multiple filters at once
  - Configurable destinations for each filter
  - Blacklisting/Inverting of individual filters
  - Copyable filters
  - Custom NBT data
  - Three NBT matching modes (match exact/match existing/match none)
- Five upgrade tiers
  - Basic
  - Improved
  - Advanced
  - Ultimate
  - Infinity (Not craftable by default)
- Fully customizable transfer speeds
- Disconnectable with wrenches
- Extremely lag friendly
  - **Only extracting pipes have block entities**
  - Pipes don't load unnecessary chunks
  - Server friendly
  - Efficient rendering

## Configuring Pipes

**Pipes don't transfer anything if they are not set to extract.**
You can sneak-click onto the end of a connected pipe with a wrench to set it to extract.

Click the extracting part of the pipe to change modes, add filters or add upgrades.

Pipes can be disconnected/reconnected by sneak-clicking with a wrench.

By default pipes are relatively slow. The speed can only be increased by adding an upgrade.
Higher tiers of upgrades mean a higher transfer rate.

![](https://media.giphy.com/media/RknAMZ8BPsAX73SCj9/giphy.gif)

Certain features of pipes can only be used by adding a high enough upgrade:

- Basic Upgrade
  - Redstone modes
- Improved Upgrade
  - Distribution modes
- Advanced Upgrade
  - Filter modes
  - Filters
  
Every configuration you change and every filter you add is stored in the upgrade.
You can copy the data to other upgrades by crafting them together.

### Filters

Filters can be used with item pipes, fluid pipes and gas pipes.

![](https://i.imgur.com/cO7Hr4Y.png)

![](https://i.imgur.com/ThD0ou1.png)

![](https://i.imgur.com/sFDCKEa.png)

To add a specific item, just click it on the slot in the filter GUI.
Alternatively you can type the item ID in the text field next to it.

If the object you inserted had NBT data, the NBT string will be displayed in the second text area.
You can remove the NBT text if you don't want to match NBT tags.

There is also an option to only match the provided NBT data (Button with NBT on it).
This can be set to exact mode.
This causes only items that match that tag exactly to be accepted by the filter.

Filters can be inverted by pressing the button with the paper on it.
This causes the filter to apply for the exact opposite of the provided data (except the destination).

You can also set the filter to only apply for a certain destination.
For this you need a filter destination tool.
Just click the block your pipe inserts into with it so store the location and face of the block.
Note that this doesn't work if you clicked a side of the block that the cable isn't connected to.

![](https://media.giphy.com/media/TpGoZ3sLyuDXU9B5re/giphy.gif)

To set the location to a filter, put it into the destination slot of the filter GUI.
This will also display the destination block in the filter list.

![](https://i.imgur.com/N5T9HWR.png)

The **Universal Pipe** has every feature of all other pipes combined.
You can choose which pipe type you want to configure with the tabs on the left of the GUI.

![](https://i.imgur.com/0IXNja8.png)

## Performance Comparison

Comparison between Pipez' item pipes and Mekanism's logistical transporters with about 4200 pipes.

![](https://i.imgur.com/t6iJe4H.png)


Comparison between Pipez' item pipes and Mekanism's logistical transporters with about 7200 pipes.
Since Mekanism calculates its pipe connections with a recursive algorithm it just crashes, due to a stack overflow.

![](https://i.imgur.com/QTzFnqz.png)