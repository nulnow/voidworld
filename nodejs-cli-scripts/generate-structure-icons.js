/**
 * Генерирует иконки 16x16 для всех построек VoidWorld.
 * Запуск: node generate-structure-icons.js
 * Требует: npm install (canvas)
 */

const fs = require('fs');
const path = require('path');
const { createCanvas } = require('canvas');

const OUTPUT_DIR = path.join(__dirname, '..', 'src', 'main', 'resources', 'assets', 'voidworld', 'textures', 'item');

// Цвета (RGB)
const C = {
    transparent: null,
    wood: [139, 90, 43],
    woodDark: [101, 67, 33],
    roof: [80, 50, 20],
    stone: [128, 128, 128],
    stoneBrick: [125, 125, 125],
    stoneBrickDark: [100, 100, 100],
    cobble: [115, 115, 115],
    cobbleDark: [95, 95, 95],
    leafOak: [34, 139, 34],
    leafDark: [0, 100, 0],
    leafBirch: [144, 238, 144],
    trunkOak: [139, 90, 43],
    trunkSpruce: [60, 45, 25],
    trunkBirch: [245, 222, 179],
    glass: [200, 220, 255],
    black: [0, 0, 0],
    white: [255, 255, 255],
};

function createImage(matrix, outputPath) {
    const size = 16;
    const canvas = createCanvas(size, size);
    const ctx = canvas.getContext('2d');
    for (let i = 0; i < size; i++) {
        for (let j = 0; j < size; j++) {
            const cell = matrix[i][j];
            if (cell === null || cell === undefined) continue;
            const [r, g, b] = Array.isArray(cell) ? cell : [cell, cell, cell];
            ctx.fillStyle = `rgb(${r},${g},${b})`;
            ctx.fillRect(j, i, 1, 1);
        }
    }
    fs.mkdirSync(OUTPUT_DIR, { recursive: true });
    fs.writeFileSync(outputPath, canvas.toBuffer('image/png'));
    console.log('  ' + path.basename(outputPath));
}

function fillRect(matrix, x1, y1, x2, y2, color) {
    for (let y = y1; y <= y2; y++) {
        for (let x = x1; x <= x2; x++) {
            if (y >= 0 && y < 16 && x >= 0 && x < 16) matrix[y][x] = color;
        }
    }
}

function createEmpty16() {
    return Array(16).fill(null).map(() => Array(16).fill(null));
}

// Деревянный дом (дубовые доски)
function matrixHouseCity() {
    const m = createEmpty16();
    fillRect(m, 2, 4, 13, 12, C.wood);
    fillRect(m, 2, 2, 13, 3, C.wood);
    fillRect(m, 2, 13, 13, 15, C.roof);
    fillRect(m, 7, 8, 8, 10, C.woodDark);
    fillRect(m, 4, 6, 5, 7, C.glass);
    fillRect(m, 10, 6, 11, 7, C.glass);
    return m;
}

// Каменный особняк
function matrixStoneMansion() {
    const m = createEmpty16();
    fillRect(m, 1, 3, 14, 12, C.stoneBrick);
    fillRect(m, 1, 1, 14, 2, C.stoneBrick);
    fillRect(m, 1, 13, 14, 15, C.stoneBrickDark);
    fillRect(m, 6, 7, 9, 10, C.glass);
    fillRect(m, 3, 5, 4, 6, C.glass);
    fillRect(m, 11, 5, 12, 6, C.glass);
    return m;
}

// Каменная башня
function matrixStoneTower() {
    const m = createEmpty16();
    fillRect(m, 4, 0, 11, 15, C.stoneBrick);
    fillRect(m, 6, 2, 9, 4, C.stoneBrickDark);
    fillRect(m, 7, 3, 8, 4, C.glass);
    fillRect(m, 6, 12, 9, 14, C.stoneBrickDark);
    fillRect(m, 7, 13, 8, 13, C.glass);
    return m;
}

// Булыжный особняк
function matrixCobbleManor() {
    const m = createEmpty16();
    fillRect(m, 1, 3, 14, 12, C.cobble);
    fillRect(m, 1, 1, 14, 2, C.cobble);
    fillRect(m, 1, 13, 14, 15, C.cobbleDark);
    fillRect(m, 5, 6, 10, 9, C.glass);
    fillRect(m, 3, 5, 4, 6, C.stoneBrick);
    fillRect(m, 11, 5, 12, 6, C.stoneBrick);
    return m;
}

// Дуб
function matrixTreeOak() {
    const m = createEmpty16();
    fillRect(m, 6, 8, 9, 15, C.trunkOak);
    fillRect(m, 4, 4, 11, 9, C.leafOak);
    fillRect(m, 5, 3, 10, 5, C.leafOak);
    fillRect(m, 6, 2, 9, 3, C.leafOak);
    fillRect(m, 7, 1, 8, 2, C.leafOak);
    return m;
}

// Ель
function matrixTreeSpruce() {
    const m = createEmpty16();
    fillRect(m, 7, 10, 8, 15, C.trunkSpruce);
    fillRect(m, 3, 2, 12, 11, C.leafDark);
    fillRect(m, 5, 0, 10, 4, C.leafDark);
    fillRect(m, 6, 5, 9, 8, C.leafDark);
    return m;
}

// Берёза
function matrixTreeBirch() {
    const m = createEmpty16();
    fillRect(m, 7, 8, 8, 15, C.trunkBirch);
    fillRect(m, 4, 4, 11, 9, C.leafBirch);
    fillRect(m, 5, 2, 10, 5, C.leafBirch);
    fillRect(m, 6, 0, 9, 3, C.leafBirch);
    return m;
}

// Каменная стена
function matrixWallStone() {
    const m = createEmpty16();
    fillRect(m, 2, 4, 13, 11, C.stone);
    fillRect(m, 2, 2, 13, 3, C.stone);
    fillRect(m, 2, 12, 13, 13, C.stoneBrickDark);
    return m;
}

// Стена из каменного кирпича
function matrixWallStoneBrick() {
    const m = createEmpty16();
    for (let y = 2; y < 14; y++) {
        for (let x = 2; x < 14; x++) {
            m[y][x] = (x + y) % 4 === 0 ? C.stoneBrickDark : C.stoneBrick;
        }
    }
    return m;
}

// Крупное многоэтажное здание
function matrixBuildingLarge() {
    const m = createEmpty16();
    fillRect(m, 2, 0, 13, 15, C.stoneBrick);
    fillRect(m, 4, 1, 11, 14, C.stoneBrickDark);
    fillRect(m, 6, 2, 9, 13, C.glass);
    fillRect(m, 5, 4, 10, 5, C.stoneBrick);
    fillRect(m, 5, 8, 10, 9, C.stoneBrick);
    fillRect(m, 5, 12, 10, 13, C.stoneBrick);
    fillRect(m, 7, 0, 8, 1, C.woodDark);
    fillRect(m, 3, 3, 4, 4, C.glass);
    fillRect(m, 11, 3, 12, 4, C.glass);
    fillRect(m, 3, 7, 4, 8, C.glass);
    fillRect(m, 11, 7, 12, 8, C.glass);
    fillRect(m, 3, 11, 4, 12, C.glass);
    fillRect(m, 11, 11, 12, 12, C.glass);
    fillRect(m, 2, 15, 13, 15, C.stoneBrickDark);
    return m;
}

// Большая башня замка
function matrixTowerCastle() {
    const m = createEmpty16();
    fillRect(m, 4, 0, 11, 15, C.stoneBrick);
    fillRect(m, 5, 2, 10, 14, C.stoneBrickDark);
    fillRect(m, 6, 4, 9, 12, C.glass);
    fillRect(m, 7, 1, 8, 2, C.stoneBrickDark);
    fillRect(m, 7, 14, 8, 15, C.stoneBrickDark);
    fillRect(m, 4, 15, 5, 15, C.stoneBrick);
    fillRect(m, 10, 15, 11, 15, C.stoneBrick);
    fillRect(m, 6, 15, 9, 15, C.stoneBrickDark);
    m[7][9] = [255, 200, 100];
    m[7][5] = [255, 200, 100];
    return m;
}

// Малая платформа (4x4)
function matrixPlatformSmall() {
    const m = createEmpty16();
    fillRect(m, 6, 6, 9, 9, C.stone);
    return m;
}

// Средняя платформа (9x9)
function matrixPlatformMedium() {
    const m = createEmpty16();
    fillRect(m, 3, 3, 12, 12, C.stone);
    return m;
}

// Гигантская платформа (32x32)
function matrixPlatformGiant() {
    const m = createEmpty16();
    fillRect(m, 0, 0, 15, 15, C.stone);
    fillRect(m, 2, 2, 13, 13, C.cobble);
    fillRect(m, 5, 5, 10, 10, C.stoneBrick);
    return m;
}

// Огромная платформа (16x16)
function matrixPlatformLarge() {
    const m = createEmpty16();
    fillRect(m, 0, 0, 15, 15, C.stone);
    fillRect(m, 2, 2, 13, 13, C.cobble);
    return m;
}

// Большая стена крепости
function matrixWallFortress() {
    const m = createEmpty16();
    fillRect(m, 0, 2, 15, 13, C.stoneBrick);
    fillRect(m, 0, 0, 15, 1, C.stoneBrickDark);
    fillRect(m, 0, 14, 15, 15, C.stoneBrickDark);
    for (let x = 0; x < 16; x += 2) {
        m[15][x] = C.stoneBrickDark;
    }
    fillRect(m, 2, 6, 2, 9, C.stoneBrick);
    fillRect(m, 7, 6, 7, 9, C.stoneBrick);
    fillRect(m, 13, 6, 13, 9, C.stoneBrick);
    m[8][3] = [255, 200, 100];
    m[8][7] = [255, 200, 100];
    m[8][11] = [255, 200, 100];
    return m;
}

// Булыжная стена
function matrixWallCobblestone() {
    const m = createEmpty16();
    fillRect(m, 2, 4, 13, 11, C.cobble);
    fillRect(m, 2, 2, 13, 3, C.cobble);
    fillRect(m, 2, 12, 13, 13, C.cobbleDark);
    for (let y = 4; y < 12; y += 2) {
        for (let x = 3; x < 13; x += 3) {
            m[y][x] = C.cobbleDark;
        }
    }
    return m;
}

// Иконка Undo (простая стрелка влево)
function matrixUndo() {
    const m = createEmpty16();
    const c = C.stoneBrick;
    fillRect(m, 4, 6, 11, 9, c);
    fillRect(m, 6, 4, 9, 11, c);
    fillRect(m, 2, 6, 5, 9, c);
    m[6][3] = c; m[7][2] = c; m[8][2] = c; m[9][3] = c;  // arrowhead
    return m;
}

// Запуск
console.log('Генерация иконок построек 16x16...\n');

const structures = [
    ['structure_house_city_standard', matrixHouseCity],
    ['structure_house_stone_brick_mansion', matrixStoneMansion],
    ['structure_house_stone_brick_tower', matrixStoneTower],
    ['structure_house_stone_cobble_manor', matrixCobbleManor],
    ['structure_tree_oak', matrixTreeOak],
    ['structure_tree_spruce', matrixTreeSpruce],
    ['structure_tree_birch', matrixTreeBirch],
    ['structure_wall_stone', matrixWallStone],
    ['structure_wall_stone_brick', matrixWallStoneBrick],
    ['structure_wall_cobblestone', matrixWallCobblestone],
    ['structure_building_large', matrixBuildingLarge],
    ['structure_platform_small', matrixPlatformSmall],
    ['structure_platform_medium', matrixPlatformMedium],
    ['structure_platform_large', matrixPlatformLarge],
    ['structure_platform_giant', matrixPlatformGiant],
    ['structure_wall_fortress', matrixWallFortress],
    ['structure_tower_castle', matrixTowerCastle],
    ['undo', matrixUndo],
];

structures.forEach(([name, fn]) => {
    const matrix = fn();
    const outPath = path.join(OUTPUT_DIR, name + '.png');
    createImage(matrix, outPath);
});

console.log('\nГотово. Иконки сохранены в assets/voidworld/textures/item/');
