# Native fluid distribution regression fixture

This optional addon exercises the actual Pipez fluid transfer method and NeoForge
fluid handlers in a disposable, loopback-only server. Do not install it on a public
server or use a player world. It refuses to load without the fixture system property
and its test command only accepts a loopback server console with no players.

Build with JDK 25 and Gradle 9.2.1:

```text
gradle -p tests/round-robin-native build -PtestMods=/absolute/path/to/disposable/server/mods
```

Use Minecraft 26.1.2 / NeoForge 26.1.2.126 and Pipez 1.2.31 with its dependencies.
Put the test addon in the disposable server mods folder, bind server-ip=127.0.0.1,
and start with -Dpipez.roundrobin.fixture=true. Run `pipezroundrobinqa` from its
console. The result is written to `pipez-round-robin-test.json`.

Run once with the original Pipez jar and once with the patched jar. Both must pass,
and `fluid_vectors` must match exactly. The vectors cover empty/1/2/7/31 targets,
every starting cursor, four transfer budgets, full/partial/null destinations,
empty sources and inverted wildcard filters. They include every destination's
amount, remaining source amount and final round-robin cursor. Native nested
transfer rollback and invalid-root caller diagnostics are also checked. The
2048-target timing is a synthetic empty-source workload, not a server TPS estimate.
