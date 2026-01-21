# Changelog

## [1.21.1-1.2.27] - 2026-01-21
### Author: bazzz

### Added
- **Pipe placement limit check** - Prevents placing pipes if it would exceed network limit
- **Network size indicator** - Shows current network size above hotbar when placing pipes
  - Color changes based on capacity: green (<50%), yellow (50-75%), orange (75-100%), red (limit reached)

### Changed
- **Improved pipe tooltips** - Removed "No Upgrade" text, cleaner display of transfer rates

### Translations
- Updated Russian localization (ru_ru.json)
- Updated English localization (en_us.json)

---

## [1.21.1-1.2.26] - Previous Release

### Added
- **Performance optimizations**
  - Replaced stream operations with loops for better performance
  - Added static comparators for connection sorting
- **Pipe network size limit feature**
  - New config option `pipe_network.limit_enabled` (default: false)
  - New config option `pipe_network.max_size` (default: 256)
  - Pipes stop working when network exceeds the configured limit
  - Players receive a chat notification when the limit is exceeded
- **Backoff mechanism for idle pipes**
  - New config option `backoff.enabled` (default: true)
  - New config option `backoff.max_delay` (default: 100)
  - New config option `backoff.increment` (default: 5)
  - New config option `backoff.decrement` (default: 10)
  - Pipes slow down when failing to transfer items/fluids/energy/gas
  - Pipes speed up on successful transfers
- **Transfer rate tooltips** to upgrades and pipes
  - Shows transfer rates from config based on upgrade tier

---

> **NOTE:** This version is in early development and might not be fully compatible with future versions!
> Pipe configurations from Minecraft versions 1.20.4 and lower won't be carried over to this version!
