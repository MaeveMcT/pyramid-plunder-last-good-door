package com.pyramidplunder.lastgooddoor;

import java.awt.Color;
import net.runelite.client.config.Alpha;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup(PyramidPlunderLastGoodDoorPlugin.CONFIG_GROUP)
public interface PyramidPlunderLastGoodDoorConfig extends Config
{
	@Alpha
	@ConfigItem(
		keyName = "highlightColor",
		name = "Highlight color",
		description = "Color used to highlight the last entrance containing the Guardian mummy"
	)
	default Color highlightColor()
	{
		return new Color(0, 255, 0, 100);
	}
}
