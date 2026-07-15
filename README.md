# Pyramid Plunder Last Good Door

A RuneLite plugin that remembers and highlights the last Sophanem pyramid entrance that led to the Guardian mummy.

## How it works

The four entrances are separate directional object IDs, but the useful entrance can move. The plugin:

1. Records the entrance tile when one of the four Pyramid Plunder doors is clicked.
2. Confirms that tile when the Guardian mummy (NPC 1779/1780) appears, with the Pyramid Plunder timer as a fallback signal.
3. Highlights that entrance when it is visible again during the current session.

The remembered door is reset whenever the plugin stops or RuneLite restarts. An unconfirmed door attempt expires after 30 game ticks. Trying the remembered good door without finding the Guardian mummy clears it; trying any other bad door leaves the last known good door unchanged.

## Development

```sh
./gradlew test
./gradlew run
```
