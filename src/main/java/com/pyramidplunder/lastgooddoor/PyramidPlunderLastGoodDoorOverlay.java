package com.pyramidplunder.lastgooddoor;

import java.awt.BasicStroke;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Polygon;
import javax.inject.Inject;
import net.runelite.api.TileObject;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;
import net.runelite.client.ui.overlay.OverlayUtil;

public class PyramidPlunderLastGoodDoorOverlay extends Overlay
{
	private final PyramidPlunderLastGoodDoorPlugin plugin;
	private final PyramidPlunderLastGoodDoorConfig config;

	@Inject
	private PyramidPlunderLastGoodDoorOverlay(
		PyramidPlunderLastGoodDoorPlugin plugin,
		PyramidPlunderLastGoodDoorConfig config)
	{
		this.plugin = plugin;
		this.config = config;
		setPosition(OverlayPosition.DYNAMIC);
		setLayer(OverlayLayer.ABOVE_SCENE);
		setPriority(OverlayPriority.HIGH);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		TileObject door = plugin.getHighlightedDoor();
		if (door == null)
		{
			return null;
		}

		Polygon polygon = door.getCanvasTilePoly();
		if (polygon != null)
		{
			OverlayUtil.renderPolygon(
				graphics,
				polygon,
				config.highlightColor(),
				config.highlightColor(),
				new BasicStroke(2));
		}
		return null;
	}
}
