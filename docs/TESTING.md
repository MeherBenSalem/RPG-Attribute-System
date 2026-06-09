# v3.4.0 Test Matrix

Run on Fabric and NeoForge/Forge per supported MC branch.

| # | Scenario | Expected |
|---|----------|----------|
| 1 | Fresh world | Default attributes, stats overview opens |
| 2 | Existing save | Legacy NBT migrates, no point loss |
| 3 | Dedicated server | Client icons/tips from sync, not local config |
| 4 | `/ras respec` | Points refunded, health recalculated |
| 5 | `/ras template apply warrior` | Template applied or validation error |
| 6 | Custom attribute 9+ | Persists relog + restart |
| 7 | Health scaling | 20+80×1=100 HP matches tooltip/docs |
| 8 | Player death | Stats persist unless `on_death_reset` |
| 9 | Player relog | No desync |
| 10 | Dimension change | max_health reapplied |
| 11 | Server restart | Player + attribute data intact |
| 12 | Bad config | `[RPGAS]` messages at startup |

## Build verification (automated)

| Branch | Fabric | NeoForge/Forge | Status |
|--------|--------|----------------|--------|
| 1.21.11 | Yes | Yes | PASS |
| 1.21.1 | Yes | Yes | PASS |
| 1.20.1 | Yes | Forge | PASS |
| 26.1.2 | Yes | Yes | PASS |

Run compile: `.\gradlew.bat :common:compileJava :fabric:compileJava :neoforge:compileJava`

**Note:** Player Stats Overview GUI ships on 1.21.11 and 1.21.1. Backend respec/templates/validation ships on all branches.
