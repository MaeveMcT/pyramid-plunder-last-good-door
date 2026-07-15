package com.pyramidplunder.lastgooddoor;

import java.awt.BasicStroke;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Shape;
import javax.inject.Inject;
import net.runelite.api.GameObject;
import net.runelite.api.TileObject;
import net.runelite.api.WallObject;
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

		Shape hull = getConvexHull(door);
		if (hull == null)
		{
			hull = door.getClickbox();
		}
		renderShape(graphics, hull);

		// Wall objects can consist of two separately rendered model pieces.
		if (door instanceof WallObject)
		{
			renderShape(graphics, ((WallObject) door).getConvexHull2());
		}
		return null;
	}

	private static Shape getConvexHull(TileObject object)
	{
		if (object instanceof GameObject)
		{
			return ((GameObject) object).getConvexHull();
		}
		if (object instanceof WallObject)
		{
			return ((WallObject) object).getConvexHull();
		}
		return null;
	}

	private void renderShape(Graphics2D graphics, Shape shape)
	{
		if (shape != null)
		{
			OverlayUtil.renderPolygon(
				graphics,
				shape,
				config.highlightColor(),
				config.highlightColor(),
				new BasicStroke(2));
		}
	}
}
