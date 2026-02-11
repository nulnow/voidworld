# VoidWorld — Game Design Document

## 1. Overview

**Title**: VoidWorld
**Platform**: Minecraft 1.20.1 (Forge Mod)
**Genre**: Narrative RPG / Adventure / Sandbox
**Players**: Single-player (with potential future multiplayer support)
**Target Audience**: Minecraft players who enjoy story-driven mods, RPGs, and deep lore

### Elevator Pitch

A shipwreck survivor with no memory washes ashore on an unknown island. As they explore a vast world of medieval cities, floating castles, and cosmic horrors, they discover that a king's scientific experiments have torn a hole between universes — and a malevolent god from a dead reality threatens to consume all existence. The player must investigate, build alliances, and ultimately choose the fate of all consciousness in the multiverse.

### Core Pillars

1. **Narrative Depth** — A rich, multi-layered story with philosophical themes about consciousness, existence, and identity
2. **Player Agency** — Meaningful choices from backstory selection through to four distinct endings
3. **Minecraft Freedom** — Full block-breaking and building within the RPG framework, with consequences (law system)
4. **Exploration** — Diverse biomes, cities, dimensions, and hidden content rewarding curiosity
5. **Customization** — Backstory, housing, summoned companions, and playstyle (combat vs stealth vs pacifist)

---

## 2. Story

### Act 1 — The Castaway

The protagonist awakens on a beach after a shipwreck. They remember nothing — only fragments: a city, a name they can't quite grasp, the feeling of running from something. They were traveling illegally on a ship that went down in a storm.

**Locations**: Shipwreck Island → Dense Jungle → Dark Cave → The Valley → The Village

The player traverses the island's hostile jungle, navigates a cave system, and emerges into a valley leading to a small village. In the village, they learn that strange events are occurring across the land: the king has disappeared, and the capital city has been sealed shut. No one enters or leaves.

The village elder and NPCs provide context and the first quests. After completing tasks to earn the villagers' trust, the player sets out for the capital.

### Act 2 — The Sealed Capital

**Location**: The Capital — a medieval fortress city on a bay

The capital is a sprawling walled city. At its center stands an impossibly tall castle — sections of it float in the air, held aloft by magic. The top of the castle is wreathed in black clouds. Sometimes explosions echo from above. Townspeople whisper of demonic screams.

The player finds ways to enter the locked-down city, completes tasks for various inhabitants, and eventually encounters a group of well-armed mercenaries. After proving their worth, the mercenaries take the player into the castle.

### Act 3 — The King's Secret

Inside the castle, the player discovers the truth:

- The king was a scientist-mage researching ways to extract energy from the quantum vacuum
- His experiments created a **false vacuum** bubble — a region where the laws of physics shift to a lower energy state, erasing everything
- This breach connected our universe to **another universe** where false vacuum had already won — a dead universe where all existence was annihilated
- Points of contact between the universes appeared — expanding zones of nothingness
- The king used his last reserves of magical power to cast a spell that **constricts space** around each contact point, temporarily containing the false vacuum's expansion
- The king is now in a suspended state, his entire being devoted to maintaining this containment spell

### Act 4 — The Voice in the Void

Using the ancient spell **"Word of the Dead Man"** on the king, the player discovers something extraordinary: the **soul of the dead universe** — its god — is trapped inside the king. This being is what actually holds the containment spell in place.

The player begins communicating with this void god. Together, they work to understand the breach and find a permanent solution. But as they climb the castle's upper floors and conduct their own investigation, they realize:

- The other universe had been seeping into ours **before** the king's experiments
- The void god may not be as benevolent as it appears
- The other universe is not merely dead — it is a **conscious predator** that harvests and absorbs all sentient minds

### Act 5 — Beyond the Stars

The player teleports to the **king's cosmic platform** — a research station hidden beyond the Great Attractor, shielded from the false vacuum by powerful magic. Here they battle demons from the void universe and discover:

- The void universe, as a living entity-god, captures consciousness and stores it in capsules
- It feeds on collected sentient minds
- Entire civilizations have been consumed

The player then reaches the **Noosphere** — a planet made entirely of frozen, materialized consciousness. The surface speaks to them. Fragments of consumed beings share their stories, their warnings, their pleas.

### Act 6 — The Choice

Four endings are available:

#### Ending 1: Unification
Allow the universes to merge, creating one vast collective consciousness. All beings enter a perfect simulation — the same world, but simulated. The false vacuum is trapped in an infinitely shrinking pocket of space. Reality becomes a dream that feels perfectly real.

#### Ending 2: Oblivion
Allow the false vacuum to consume everything. All universes, all inter-universal space, all existence is erased. The ultimate entropy. Nothing remains.

#### Ending 3: Stasis
Freeze everything as it is. The containment holds forever. The king remains suspended. The void god remains trapped. The contact points remain sealed. Nothing changes. Nothing is resolved. The universe persists in an eternal, fragile balance.

#### Ending 4: Liberation
Negotiate with the void god. Create a physical body for it using the king's research and void matter. Free both the god and the king. The god, now embodied, helps seal the breaches permanently. The king awakens. The world heals, though scars remain.

---

## 3. Character System

### Backstory Selection

At game start, the player answers a series of questions forming a decision tree:

1. **Origin** — Where did you come from? (affects NPC reactions, available dialog)
2. **Profession** — What was your trade? (affects starting abilities/equipment)
3. **Motivation** — Why were you on that ship? (affects story branches)
4. **Personality** — How do you handle conflict? (affects available approaches)
5. **Secret** — What are you hiding? (unlocks unique quest paths)

Backstory is revealed gradually through memory fragments triggered by story events.

### Player Progression

- Quest-based progression (not traditional XP grinding)
- Skill unlocks through story milestones and NPC training
- Equipment found through exploration and crafting
- Summoned companion grows stronger with use

---

## 4. Game Systems

### 4.1 Quest System

- Data-driven quests defined in JSON
- Multiple stage progression with varied objective types
- Branching based on player choices and backstory
- Categories: Main Story, Side Quest, City Quest, Campaign, Bounty, Exploration
- Quest journal GUI with tracking HUD overlay
- Prerequisites system for quest chains

### 4.2 Dialog System

- Tree-structured conversations with NPCs
- Conditional choices (based on quests, backstory, items, crime level, currency)
- Dialog actions: start quests, give/take items, give/take currency, teleport
- Text reveal animation with configurable speed

### 4.3 Economy

- **Void Coins** — primary currency
- **Wallet** — carried on person (lost partially on death)
- **Bank** — safe storage with daily interest
- **Fines** — deducted from wallet, then bank
- **Trading** — buy/sell with NPC merchants
- **Quest Rewards** — primary income source
- **Player Transfer** — give currency to other NPCs/players

### 4.4 Law & Prison

**Protected Zones**: Cities have zones with varying protection levels:
- INDESTRUCTIBLE: Cannot be broken (castle walls, key structures)
- HEAVILY_PROTECTED: Breaking triggers severe punishment
- PROTECTED: Breaking triggers fines
- PLAYER_ZONE: Free building (housing plots)

**Crimes**: Block destruction, NPC assault, theft, trespassing, murder, contraband
**Punishment**: Fines from wallet/bank, increasing crime level, imprisonment
**Prison**: Located in the capital, with escape mechanics (stealth/puzzle mini-game)

### 4.5 Stealth System

Used in rooftop missions, castle infiltration, gothic city vampire gameplay, and prison escapes.

**Detection factors**:
- Light level at player position
- Distance to detector NPCs
- Movement speed (sneaking reduces visibility)
- Equipment (stealth gear)
- Line of sight

**Alert Levels**: Unaware → Suspicious → Searching → Combat

### 4.6 Summon Companion System

The void god can create clones of beings to assist the player.

**Acquisition**: Find void consciousness fragments at void cracks
**Customization**:
- Base form (determines stats and movement type)
- Abilities (up to 6 equippable skills)
- Appearance (colors, patterns)
- Personality (affects AI behavior)

**Ability Types**: Attack, Defend, Heal, Buff, Debuff, Utility, Transport, Stealth, Special

### 4.7 Housing

**Plot Types**:
- City plots (near services, costs currency, protected)
- Free plots (outside city, larger, no cost)
- Wilderness (anywhere, no protection)
- Floating island plots (campaign unlock)
- Waterfront plots (Venetian city)

All plots support full Minecraft building within boundaries.

### 4.8 Puzzle System

- **Programming**: Redstone logic gates, command sequences
- **Combinatorics**: Mathematical pattern matching, lock combinations
- **Riddles**: Text-based puzzles from NPCs or inscriptions
- **Environmental**: Pressure plates, lever sequences, parkour
- **Void Logic**: Unique mechanics from the other universe

### 4.9 Taming

- Magical and ordinary creatures can be tamed
- Void creatures controlled by cerebral generals are mind-controlled zombies (no consciousness)
- Liberation campaign: free creatures without killing (pacifist route)
- Tamed creatures can assist in combat and exploration

---

## 5. Locations

### 5.1 Shipwreck Island (Starting Area)
Beach with wreckage, dense jungle, cave system, valley exit to mainland.

### 5.2 The Village
Small settlement with basic services. Quest hub for Act 1. Elder, blacksmith, trader NPCs.

### 5.3 The Capital
Medieval walled city on a bay. Market district, residential areas, harbor, guard barracks, prison. The king's castle at the center with floating sections and dark cloud crown.

### 5.4 Nocturn — Gothic City
Dark-themed city with tall spires, gargoyles, perpetual twilight atmosphere. Vampire gameplay: rooftop traversal, blood mechanics, sun sensitivity. Unique stealth-focused quests.

### 5.5 Sandport — Desert City
Inspired by Lut Gholein (Diablo II). Desert oasis trading hub with underground catacombs, bazaars, and desert survival elements.

### 5.6 Frosthold — Mountain Fortress
Snow-covered fortress high in the mountains. Cold survival mechanics, mining-focused economy, mountain pass exploration quests.

### 5.7 Mirewood — Swamp Settlement
Swamp biome village on stilts. Alchemy and potion focus, unique swamp creatures, fog effects, mysterious atmosphere.

### 5.8 Aquaverde — City on Water
Venice-inspired city built on water after flooding. Boat navigation, trade routes, religious lore, water-based challenges. Residents stayed for religious reasons despite flooding.

### 5.9 Sky Reaches — Floating Islands
Archipelago of floating islands with bridges between them. Building homes on islands, unique sky resources, aerial exploration campaign.

### 5.10 Corsair's Rest — Pirate Cove
Island chain where ships from the player's voyage sank. Pirates established a base. Underwater artifact recovery, ship exploration, pirate faction.

### 5.11 The Observatory — Cosmic Platform
Custom dimension: the king's research station beyond the Great Attractor. Magic-shielded from false vacuum. Void demon encounters, consciousness capsule discoveries.

### 5.12 Noosphere — Frozen Consciousness Planet
Custom dimension: planet of materialized, frozen consciousness. Terrain communicates with the player. Fragments of absorbed civilizations.

### 5.13 Void Cracks
Scattered across the world. Portals to pockets of the other universe. Source of void creature parts for summoned companions. Main story missions to seal them.

---

## 6. Enemies & Creatures

### From the Void Universe
- **Void Demons** — Combat-oriented hostile entities
- **Cerebral Generals** — Mind-controlling entities that command armies of mindless void creatures
- **Void Zombies** — Creatures stripped of consciousness, controlled by cerebral generals
- **Consciousness Collectors** — Entities that harvest sentient minds
- **The Void God** — The living universe itself (NPC, not enemy per se)

### Regular World
- **Giant Void Bugs** — Aggressive insectoid creatures from the void (interception missions)
- **Hyena-Men Bandits** — Aggressive humanoid raiders (can be pacified through diplomacy)
- **Zombies** — Virus-infected, with epicenters to destroy
- **City Guards** — Enforce law, pursue criminals
- **Wildlife** — Various tameable creatures

---

## 7. Special Missions

### Rooftop Stealth Missions
Given a building plan, navigate rooftops stealthily to complete secret objectives.

### Void Bug Interception
Stop waves of giant insects from the void universe before they reach populated areas.

### Hyena-Men Diplomacy
Option to negotiate peace with the hyena-men bandits instead of exterminating them.

### Zombie Epicenter Destruction
Track and destroy sources of zombie virus. Culminates in cleansing an entire zombie planet.

### Pirate Artifact Recovery
Dive to sunken ships from the protagonist's voyage to recover powerful artifacts.

### Cerebral General Liberation
Free mind-controlled void creatures by defeating cerebral generals without killing the controlled beings — enables pacifist playthrough.

---

## 8. Monetization

### Donation System
- Voluntary donations to fund future VoidWorld universe projects
- In-game button linking to donation page
- Cosmetic-only rewards for donors (titles, particle effects, pet skins)
- Zero pay-to-win: all gameplay content accessible without payment
- Transparent funding goals for upcoming content

---

## 9. Technical Notes

### Data-Driven Design
Quests, dialogs, NPCs, and puzzles are defined in JSON files under `data/voidworld/`. This allows content creation without code changes and supports future data pack extensions.

### Mod Compatibility
- Built on Forge 1.20.1 for maximum mod ecosystem compatibility
- Public API module for other mods to interact with VoidWorld systems
- Clean separation between core systems and content

### Performance Considerations
- Lazy loading of quest/dialog data
- Efficient chunk-based protected zone checking
- Client-side prediction for stealth visibility
- LOD system for distant castle floating sections
