/**
 * Проектирует крупное многоэтажное здание и выводит структуру в JSON.
 * План: 9x9 footprint, 5 этажей (y=0..4), 4 комнаты на этаж + центральный коридор.
 *
 * Этаж 0: Фундамент (пол)
 * Этаж 1-3: Чередование полов (slab) и помещений (air) + стены с окнами
 * Этаж 4: Крыша из плит
 *
 * Комнаты: NW(1-3,1-3), NE(5-7,1-3), SW(1-3,5-7), SE(5-7,5-7)
 * Коридор: крест — x=4 z=1-7, x=1-7 z=4
 */

const fs = require('fs');
const path = require('path');

const W = 9, D = 9;

function wall(x, z) { return x === 0 || x === W - 1 || z === 0 || z === D - 1; }
function interior(x, z) { return x >= 1 && x <= W - 2 && z >= 1 && z <= D - 2; }
function mainDoor(x, z, y) { return x === 4 && z === 0 && y === 1; }
function corridorDoor(x, z, y) {
  if (y < 1 || y > 7) return false;
  return (x === 4 && (z === 2 || z === 6)) || (z === 4 && (x === 2 || x === 6));
}
function window(x, z, y) {
  if (y < 1 || y > 7) return false;
  if (!wall(x, z)) return false;
  return x === 2 || x === 6 || z === 2 || z === 6;
}

const blocks = [];

for (let y = 0; y <= 9; y++) {
  for (let z = 0; z < D; z++) {
    for (let x = 0; x < W; x++) {
      if (y === 0) {
        blocks.push({ type: 'minecraft:stone_bricks', relativePosition: [x, y, z] });
      } else if (y === 9) {
        blocks.push({ type: 'minecraft:stone_brick_slab', relativePosition: [x, y, z] });
      } else if (mainDoor(x, z, y)) {
        blocks.push({ type: 'minecraft:air', relativePosition: [x, y, z] });
      } else if (wall(x, z)) {
        if (window(x, z, y)) {
          blocks.push({ type: 'minecraft:glass_pane', relativePosition: [x, y, z] });
        } else {
          blocks.push({ type: 'minecraft:stone_bricks', relativePosition: [x, y, z] });
        }
      } else if (interior(x, z)) {
        if (corridorDoor(x, z, y)) {
          blocks.push({ type: 'minecraft:air', relativePosition: [x, y, z] });
        } else if (y % 2 === 1) {
          blocks.push({ type: 'minecraft:stone_brick_slab', relativePosition: [x, y, z] });
        } else {
          blocks.push({ type: 'minecraft:air', relativePosition: [x, y, z] });
        }
      }
    }
  }
}

// Факелы в комнатах (на полах, y нечётные)
for (let y = 1; y <= 7; y += 2) {
  [[2, 2], [6, 2], [2, 6], [6, 6]].forEach(([x, z]) => {
    blocks.push({ type: 'minecraft:torch', relativePosition: [x, y + 1, z] });
  });
}

const structure = {
  name: 'Large Multi-Floor Building',
  blocks,
  blockImage: 'minecraft:block/stone_bricks'
};

const outPath = path.join(__dirname, '..', 'src', 'main', 'resources', 'data', 'voidworld', 'structures', 'building_large.json');
fs.writeFileSync(outPath, JSON.stringify(structure, null, 2));
console.log(`Структура сохранена: ${outPath}`);
console.log(`Блоков: ${blocks.length}`);
