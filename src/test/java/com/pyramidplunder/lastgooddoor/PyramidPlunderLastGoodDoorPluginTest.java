package com.pyramidplunder.lastgooddoor;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class PyramidPlunderLastGoodDoorPluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(PyramidPlunderLastGoodDoorPlugin.class);
		RuneLite.main(args);
	}
}
