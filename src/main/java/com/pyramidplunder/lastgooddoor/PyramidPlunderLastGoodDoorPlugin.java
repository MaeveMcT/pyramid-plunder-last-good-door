package com.pyramidplunder.lastgooddoor;

import com.google.inject.Provides;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import javax.inject.Inject;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameObject;
import net.runelite.api.GameState;
import net.runelite.api.NPC;
import net.runelite.api.NpcID;
import net.runelite.api.Tile;
import net.runelite.api.TileObject;
import net.runelite.api.Varbits;
import net.runelite.api.WallObject;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.GameObjectDespawned;
import net.runelite.api.events.GameObjectSpawned;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.events.NpcSpawned;
import net.runelite.api.events.WallObjectDespawned;
import net.runelite.api.events.WallObjectSpawned;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

@Slf4j
@PluginDescriptor(
	name = "Pyramid Plunder Last Good Door",
	description = "Highlights the last Pyramid Plunder entrance where the Guardian mummy was found",
	tags = {"pyramid", "plunder", "sophanem", "thieving", "door"}
)
public class PyramidPlunderLastGoodDoorPlugin extends Plugin
{
	static final String CONFIG_GROUP = "pyramidPlunderLastGoodDoor";
	private static final int MAX_PENDING_TICKS = 30;

	/* The four directional entrance objects and all of their transformed states. */
	private static final Set<Integer> ENTRANCE_IDS = new HashSet<>(Arrays.asList(
		20956, 20974, 20975, // north
		20976, 20977, 20978, // east
		20979, 20987, 21251, // south
		21252, 21253, 21254, // west
		26622, 26623, 26624, 26625 // multi-locs seen by some client revisions
	));

	@Inject
	private Client client;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private PyramidPlunderLastGoodDoorOverlay overlay;

	@Getter
	private TileObject highlightedDoor;
	private WorldPoint lastGoodDoor;
	private WorldPoint pendingDoor;
	private int pendingTicks;
	private int timerAtAttempt;

	@Override
	protected void startUp()
	{
		lastGoodDoor = null;
		pendingDoor = null;
		highlightedDoor = null;
		overlayManager.add(overlay);
	}

	@Override
	protected void shutDown()
	{
		overlayManager.remove(overlay);
		highlightedDoor = null;
		pendingDoor = null;
	}

	@Subscribe
	public void onMenuOptionClicked(MenuOptionClicked event)
	{
		if (!ENTRANCE_IDS.contains(event.getId()) || !"Search".equalsIgnoreCase(event.getMenuOption()))
		{
			return;
		}

		pendingDoor = WorldPoint.fromScene(client, event.getParam0(), event.getParam1(), client.getPlane());
		pendingTicks = 0;
		timerAtAttempt = client.getVarbitValue(Varbits.PYRAMID_PLUNDER_TIMER);
		log.debug("Trying Pyramid Plunder entrance {} at {}", event.getId(), pendingDoor);
	}

	@Subscribe
	public void onNpcSpawned(NpcSpawned event)
	{
		confirmIfGuardian(event.getNpc());
	}

	@Subscribe
	public void onGameTick(GameTick event)
	{
		if (pendingDoor != null)
		{
			for (NPC npc : client.getNpcs())
			{
				if (isGuardian(npc))
				{
					confirmPendingDoor();
					break;
				}
			}

			if (pendingDoor != null && timerAtAttempt <= 0
				&& client.getVarbitValue(Varbits.PYRAMID_PLUNDER_TIMER) > 0)
			{
				confirmPendingDoor();
			}

			if (pendingDoor != null && ++pendingTicks > MAX_PENDING_TICKS)
			{
				log.debug("Pyramid Plunder entrance attempt expired: {}", pendingDoor);
				if (pendingDoor.equals(lastGoodDoor))
				{
					clearLastGoodDoor();
				}
				pendingDoor = null;
			}
		}

		if (lastGoodDoor != null && highlightedDoor == null)
		{
			highlightedDoor = findDoorAt(lastGoodDoor);
		}
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged event)
	{
		if (event.getGameState() == GameState.LOADING || event.getGameState() == GameState.LOGIN_SCREEN)
		{
			highlightedDoor = null;
		}
	}

	@Subscribe
	public void onWallObjectSpawned(WallObjectSpawned event)
	{
		considerForHighlight(event.getWallObject());
	}

	@Subscribe
	public void onGameObjectSpawned(GameObjectSpawned event)
	{
		considerForHighlight(event.getGameObject());
	}

	@Subscribe
	public void onWallObjectDespawned(WallObjectDespawned event)
	{
		clearIfHighlighted(event.getWallObject());
	}

	@Subscribe
	public void onGameObjectDespawned(GameObjectDespawned event)
	{
		clearIfHighlighted(event.getGameObject());
	}

	private void confirmIfGuardian(NPC npc)
	{
		if (pendingDoor != null && isGuardian(npc))
		{
			confirmPendingDoor();
		}
	}

	private static boolean isGuardian(NPC npc)
	{
		return npc.getId() == NpcID.GUARDIAN_MUMMY || npc.getId() == NpcID.ANNOYED_GUARDIAN_MUMMY;
	}

	private void confirmPendingDoor()
	{
		lastGoodDoor = pendingDoor;
		pendingDoor = null;
		log.debug("Remembered good Pyramid Plunder entrance for this session: {}", lastGoodDoor);
	}

	private void clearLastGoodDoor()
	{
		log.debug("Previously good Pyramid Plunder entrance no longer contains the Guardian mummy: {}", lastGoodDoor);
		lastGoodDoor = null;
		highlightedDoor = null;
	}

	private void considerForHighlight(TileObject object)
	{
		if (lastGoodDoor != null && ENTRANCE_IDS.contains(object.getId())
			&& lastGoodDoor.equals(object.getWorldLocation()))
		{
			highlightedDoor = object;
		}
	}

	private void clearIfHighlighted(TileObject object)
	{
		if (object == highlightedDoor)
		{
			highlightedDoor = null;
		}
	}

	private TileObject findDoorAt(WorldPoint point)
	{
		Tile[][][] tiles = client.getScene().getTiles();
		for (Tile[][] plane : tiles)
		{
			for (Tile[] row : plane)
			{
				for (Tile tile : row)
				{
					if (tile == null)
					{
						continue;
					}

					WallObject wall = tile.getWallObject();
					if (matches(wall, point))
					{
						return wall;
					}

					for (GameObject gameObject : tile.getGameObjects())
					{
						if (matches(gameObject, point))
						{
							return gameObject;
						}
					}
				}
			}
		}
		return null;
	}

	private static boolean matches(TileObject object, WorldPoint point)
	{
		return object != null && ENTRANCE_IDS.contains(object.getId()) && point.equals(object.getWorldLocation());
	}

	@Provides
	PyramidPlunderLastGoodDoorConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(PyramidPlunderLastGoodDoorConfig.class);
	}
}
