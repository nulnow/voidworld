/**
 * Разрезает изображение на 6 частей для панорамы главного меню Minecraft.
 * Сетка 3×2: [front, right, back] / [left, bottom, top]
 *
 * Запуск: node split-panorama.js [путь_к_картинке]
 * По умолчанию: title_background.png
 *
 * Требует: npm install (canvas)
 */

const fs = require('fs');
const path = require('path');
const { createCanvas, loadImage } = require('canvas');

const ASSETS_DIR = path.join(__dirname, '..', 'src', 'main', 'resources', 'assets', 'voidworld', 'textures', 'gui');
const DEFAULT_INPUT = path.join(ASSETS_DIR, 'title_background.png');
const OUTPUT_DIR = path.join(ASSETS_DIR, 'title', 'background');

// Порядок для Minecraft CubeMap: 0=front, 1=right, 2=back, 3=left, 4=bottom, 5=top
// Сетка 3×2: [0, 1, 2] в первой строке, [3, 4, 5] во второй
const LAYOUT = [
    [0, 1, 2],  // row 0: front, right, back
    [3, 4, 5],  // row 1: left, bottom, top
];

async function main() {
    const inputPath = process.argv[2] || DEFAULT_INPUT;
    if (!fs.existsSync(inputPath)) {
        console.error('Файл не найден:', inputPath);
        process.exit(1);
    }

    console.log('Загрузка:', inputPath);
    const img = await loadImage(inputPath);
    const w = img.width;
    const h = img.height;

    const cols = 3;
    const rows = 2;
    const partW = Math.floor(w / cols);
    const partH = Math.floor(h / rows);

    console.log(`Размер: ${w}x${h}, каждая часть: ${partW}x${partH}`);

    fs.mkdirSync(OUTPUT_DIR, { recursive: true });

    for (let row = 0; row < rows; row++) {
        for (let col = 0; col < cols; col++) {
            const faceIndex = LAYOUT[row][col];
            const canvas = createCanvas(partW, partH);
            const ctx = canvas.getContext('2d');

            const sx = col * partW;
            const sy = row * partH;
            ctx.drawImage(img, sx, sy, partW, partH, 0, 0, partW, partH);

            const outPath = path.join(OUTPUT_DIR, `panorama_${faceIndex}.png`);
            fs.writeFileSync(outPath, canvas.toBuffer('image/png'));
            console.log(`  panorama_${faceIndex}.png`);
        }
    }

    console.log('Готово! Панорамы сохранены в:', OUTPUT_DIR);
}

main().catch((err) => {
    console.error(err);
    process.exit(1);
});
