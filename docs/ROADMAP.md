# VoidWorld Development Roadmap

## Phase 1 — Core & MVP Vertical Slice

**Goal**: Playable experience from shipwreck to entering the capital.

### 1.1 Core Infrastructure
- [ ] Forge mod skeleton (complete)
- [ ] Registration system (complete)
- [ ] Network packet framework (complete)
- [ ] Config system (complete)
- [ ] Player data persistence (Capabilities) (complete)
- [ ] Data-driven JSON loader for quests, dialogs, NPCs

### 1.2 Quest System Implementation
- [ ] Quest loading from JSON data packs
- [ ] Quest state machine (start -> stages -> complete)
- [ ] Objective tracking (KILL, COLLECT, TALK_TO, VISIT)
- [ ] Quest journal GUI (client screen)
- [ ] Quest HUD tracker overlay

### 1.3 Dialog System Implementation
- [ ] Dialog tree loader from JSON
- [ ] Dialog GUI screen with NPC portrait and choices
- [ ] Condition evaluation engine
- [ ] Dialog action execution (quest start, give items, etc.)

### 1.4 NPC System
- [ ] Base NPC entity with custom AI
- [ ] NPC definition loader from JSON
- [ ] NPC daily schedule system
- [ ] NPC interaction (right-click -> dialog)
- [ ] NPC pathfinding within defined areas

### 1.5 Economy — Basic
- [ ] Currency item or virtual wallet
- [ ] Bank terminal block and GUI
- [ ] Basic trade GUI with NPCs
- [ ] Reward payouts from quests

### 1.6 First Locations
- [ ] Shipwreck island (starting area)
- [ ] Jungle biome traversal area
- [ ] Cave system
- [ ] Valley (open area connecting to village)
- [ ] Village with NPCs, quests, and basic services

### 1.7 Backstory Selection
- [ ] Backstory tree data structure
- [ ] Character creation GUI (shown on first join)
- [ ] Backstory choices stored in player data
- [ ] At least 3 backstory paths with different starting conditions

---

## Phase 2 — The Capital & Main Story

**Goal**: Full capital city experience with the castle and main storyline through meeting the mercenaries.

### 2.1 Capital City
- [ ] Medieval fortress city layout (structure generation or pre-built)
- [ ] Bay/harbor area
- [ ] Market district with traders
- [ ] Residential areas
- [ ] Guard patrols and city gate mechanics

### 2.2 The Castle
- [ ] Multi-level castle structure
- [ ] Floating sections (custom rendering or structure)
- [ ] Black clouds visual effect at the top
- [ ] Ambient sounds (explosions, demonic cries)
- [ ] Castle interior with NPCs and quests

### 2.3 Main Story Quests (Act 2)
- [ ] City entry questline
- [ ] Castle side-quests for random NPCs
- [ ] Meeting the mercenary group
- [ ] Entering the upper castle with mercenaries
- [ ] Learning about the king's experiments

### 2.4 Law System
- [ ] Protected zone definitions for capital
- [ ] Block break detection and fines
- [ ] Crime level tracking
- [ ] Prison location and imprisonment mechanics
- [ ] Prison escape mini-game

---

## Phase 3 — Advanced Systems

**Goal**: Summon companions, stealth gameplay, housing, and expanded mechanics.

### 3.1 Summon System
- [ ] Summon entity with customizable AI
- [ ] Void consciousness fragment items
- [ ] Summon customization GUI (abilities, appearance)
- [ ] Ability system with cooldowns
- [ ] Summon leveling and experience

### 3.2 Stealth System
- [ ] Visibility calculation (light, cover, movement)
- [ ] Guard detection AI
- [ ] Alert level system
- [ ] Stealth mission framework
- [ ] Rooftop traversal mechanics

### 3.3 Housing System
- [ ] Plot definition and claiming
- [ ] City plot purchase with currency
- [ ] Build permission checks
- [ ] Plot management GUI
- [ ] Free plots and wilderness building

---

## Phase 4 — Additional Cities & Biomes

**Goal**: Expand the world with unique themed locations.

### 4.1 Gothic City (Nocturn)
- [ ] Dark architecture biome
- [ ] Vampire gameplay mechanics (blood drinking, sun weakness)
- [ ] Rooftop traversal network
- [ ] Nocturn-specific quests and NPCs
- [ ] Day/night cycle gameplay differences

### 4.2 Desert City (Sandport)
- [ ] Lut Gholein-inspired desert trading hub
- [ ] Desert-specific enemies and hazards
- [ ] Underground catacombs
- [ ] Trading caravan events

### 4.3 Snow Fortress (Frosthold)
- [ ] Mountain fortress in snowy biome
- [ ] Cold survival mechanics
- [ ] Mining and resource gathering focus
- [ ] Mountain pass exploration

### 4.4 Swamp Town (Mirewood)
- [ ] Swamp biome settlement
- [ ] Alchemical/potion crafting focus
- [ ] Unique swamp creatures
- [ ] Foggy atmosphere effects

### 4.5 Venetian City (Aquaverde)
- [ ] City on water (buildings on stilts/floating)
- [ ] Boat-based navigation
- [ ] Trade route mechanics
- [ ] Flooding events
- [ ] Religious lore questline

### 4.6 Floating Islands (Sky Reaches)
- [ ] Flying island biome
- [ ] Bridge building between islands
- [ ] Island-specific resources
- [ ] Sky campaign questline

---

## Phase 5 — Endgame & Cosmic Content

**Goal**: Complete the main storyline with cosmic locations and multiple endings.

### 5.1 Word of the Dead Man Spell
- [ ] Spell mechanics and casting system
- [ ] Revealing the void god inside the king
- [ ] Void god NPC and dialog system
- [ ] Working with the void god questline

### 5.2 Cosmic Platform
- [ ] Custom dimension: cosmic platform beyond the Great Attractor
- [ ] Magical barrier visualization
- [ ] Void demon encounters
- [ ] Discovery of consciousness capsules
- [ ] Platform exploration and puzzles

### 5.3 Consciousness Planet (Noosphere)
- [ ] Custom dimension: frozen consciousness world
- [ ] Terrain that "speaks" to the player
- [ ] Environmental storytelling mechanics
- [ ] Fragments of absorbed civilizations

### 5.4 Multiple Endings
- [ ] Ending 1: Merge universes — create unified consciousness simulation
- [ ] Ending 2: False vacuum triumph — all existence erased
- [ ] Ending 3: Freeze everything — eternal stasis
- [ ] Ending 4: Negotiate — give the void god a body, free the king
- [ ] Ending cinematics / sequences for each

---

## Phase 6 — Side Campaigns & Polish

**Goal**: Complete all side content, polish, and prepare for release.

### 6.1 Pirate Campaign
- [ ] Pirate cove base on islands
- [ ] Ship exploration (sunken fleet from protagonist's voyage)
- [ ] Underwater diving mechanics
- [ ] Artifact recovery missions
- [ ] Pirate faction reputation

### 6.2 Underwater Campaign
- [ ] Underwater exploration mechanics
- [ ] Underwater structures and ruins
- [ ] Aquatic creatures
- [ ] Breathing and depth mechanics

### 6.3 Pacifist Campaign (Cerebral Generals)
- [ ] Non-lethal takedown mechanics
- [ ] Mind-controlled creature liberation
- [ ] Cerebral general boss encounters (non-lethal)
- [ ] Complete game without killing

### 6.4 Puzzle Content
- [ ] Programming puzzles (redstone logic challenges)
- [ ] Combinatorics puzzles
- [ ] Riddle system with ancient inscriptions
- [ ] Puzzle reward integration

### 6.5 Void Crack Missions
- [ ] Void crack world feature generation
- [ ] Sealing mission mechanics
- [ ] Void creature hunting
- [ ] Parts collection for summon abilities

### 6.6 Taming System
- [ ] Taming mechanics for magical creatures
- [ ] Creature AI and companionship
- [ ] Creature breeding
- [ ] Mount system

### 6.7 Donation System
- [ ] Donation page integration
- [ ] Cosmetic reward verification
- [ ] Non-gameplay-affecting cosmetics
- [ ] Funding progress display

### 6.8 Final Polish
- [ ] Performance optimization
- [ ] Bug fixing pass
- [ ] Localization completion (EN, RU)
- [ ] Sound design and music
- [ ] Texture and model polish
- [ ] Play testing and balance
