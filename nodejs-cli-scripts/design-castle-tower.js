/**
 * Генерирует большую башню замка.
 * 7x7 в основании, 14 блоков в высоту.
 * Толстые стены, окна, зубцы наверху, факелы внутри.
 */

const fs = require('fs');
const path = require('path');

const SIZE = 7, H = 14;

function inTower(x, z) {
  return x >= 0 && x < SIZE && z >= 0 && z < SIZE;
}

function isWall(x, z) {
  return x === 0 || x === SIZE - 1 || z === 0 || z === SIZE - 1;
}

function isInterior(x, z) {
  return x >= 1 && x <= SIZE - 2 && z >= 1 && z <= SIZE - 2;
}

function isWindow(x, z, y) {
  if (y < 2 || y > H - 3) return false;
  if (!isWall(x, z)) return false;
  return (x === 3 && (z === 0 || z === SIZE - 1)) ||
         (z === 3 && (x === 0 || x === SIZE - 1));
}

function isBattlement(x, z) {
  return (x + z) % 2 === 0;
}

const blocks = [];

for (let y = 0; y < H; y++) {
  for (let z = 0; z < SIZE; z++) {
    for (let x = 0; x < SIZE; x++) {
      if (y === 0) {
        blocks.push({ type: 'minecraft:stone_bricks', relativePosition: [x, y, z] });
      } else if (y === H - 1) {
        if (isWall(x, z) && isBattlement(x, z)) {
          blocks.push({ type: 'minecraft:stone_bricks', relativePosition: [x, y, z] });
        } else if (isWall(x, z)) {
          blocks.push({ type: 'minecraft:air', relativePosition: [x, y, z] });
        } else {
          blocks.push({ type: 'minecraft:stone_brick_slab', relativePosition: [x, y, z] });
        }
      } else if (isWall(x, z)) {
        if (isWindow(x, z, y)) {
          blocks.push({ type: 'minecraft:glass_pane', relativePosition: [x, y, z] });
        } else {
          blocks.push({ type: 'minecraft:stone_bricks', relativePosition: [x, y, z] });
        }
      } else {
        blocks.push({ type: 'minecraft:air', relativePosition: [x, y, z] });
      }
    }
  }
}

// Факелы внутри на разных уровнях
for (let y = 2; y < H - 1; y += 3) {
  [[2, 2], [4, 2], [2, 4], [4, 4], [3, 3]].forEach(([x, z]) => {
    blocks.push({ type: 'minecraft:torch', relativePosition: [x, y, z] });
  });
}

const structure = {
  name: 'Castle Tower',
  blocks,
  blockImage: 'minecraft:block/stone_bricks'
};

const outPath = path.join(__dirname, '..', 'src', 'main', 'resources', 'data', 'voidworld', 'structures', 'tower_castle.json');
fs.writeFileSync(outPath, JSON.stringify(structure, null, 2));
console.log(`Башня замка сохранена: ${outPath}`);
console.log(`Блоков: ${blocks.length}`);
