/**
 * Генерирует большую стену крепости.
 * 20 блоков в ширину, 3 в глубину, 8 в высоту.
 * Бастионы по краям, зубцы (мерлоны) наверху, факелы.
 */

const fs = require('fs');
const path = require('path');

const W = 20, D = 3, H = 8;

const blocks = [];

for (let y = 0; y < H; y++) {
  for (let z = 0; z < D; z++) {
    for (let x = 0; x < W; x++) {
      if (y === H - 1) {
        // Боевой ход с зубцами: полный блок или воздух (лакуна)
        const isMerlon = (x + 1) % 2 === 0;
        if (isMerlon) {
          blocks.push({ type: 'minecraft:stone_bricks', relativePosition: [x, y, z] });
        } else {
          blocks.push({ type: 'minecraft:air', relativePosition: [x, y, z] });
        }
      } else {
        blocks.push({ type: 'minecraft:stone_bricks', relativePosition: [x, y, z] });
      }
    }
  }
}

// Факелы через каждые 4 блока по ширине, на высоте 6
for (let x = 2; x < W; x += 4) {
  blocks.push({ type: 'minecraft:torch', relativePosition: [x, 6, 1] });
}

const structure = {
  name: 'Fortress Wall',
  blocks,
  blockImage: 'minecraft:block/stone_bricks'
};

const outPath = path.join(__dirname, '..', 'src', 'main', 'resources', 'data', 'voidworld', 'structures', 'wall_fortress.json');
fs.writeFileSync(outPath, JSON.stringify(structure, null, 2));
console.log(`Стена крепости сохранена: ${outPath}`);
console.log(`Блоков: ${blocks.length}`);
