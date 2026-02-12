/**
 * Создаёт панораму: 4 боковые грани из картинки (2x2), верх и низ — чёрный 1x1.
 * Запуск: node split-panorama-mixed.js [путь_к_картинке]
 */

const fs = require('fs');
const path = require('path');
const { createCanvas, loadImage } = require('canvas');

const OUTPUT_DIR = path.join(__dirname, '..', 'src', 'main', 'resources', 'assets', 'voidworld', 'textures', 'gui', 'title', 'background');

// 0=front, 1=right, 2=back, 3=left — из картинки (2x2)
// 4=bottom, 5=top — чёрный 1x1
const LAYOUT_2x2 = [[0, 1], [2, 3]];  // front, right / back, left

async function main() {
    const inputPath = process.argv[2] || path.join(__dirname, '..', '2fw4hs55m3051.png');
    if (!fs.existsSync(inputPath)) {
        console.error('Файл не найден:', inputPath);
        process.exit(1);
    }

    console.log('Загрузка:', inputPath);
    const img = await loadImage(inputPath);
    const w = img.width;
    const h = img.height;

    const partW = Math.floor(w / 2);
    const partH = Math.floor(h / 2);

    fs.mkdirSync(OUTPUT_DIR, { recursive: true });

    for (let row = 0; row < 2; row++) {
        for (let col = 0; col < 2; col++) {
            const faceIndex = LAYOUT_2x2[row][col];
            const canvas = createCanvas(partW, partH);
            const ctx = canvas.getContext('2d');
            ctx.drawImage(img, col * partW, row * partH, partW, partH, 0, 0, partW, partH);
            fs.writeFileSync(path.join(OUTPUT_DIR, `panorama_${faceIndex}.png`), canvas.toBuffer('image/png'));
            console.log(`  panorama_${faceIndex}.png (${partW}x${partH})`);
        }
    }

    const black = createCanvas(1, 1);
    const ctx = black.getContext('2d');
    ctx.fillStyle = '#000000';
    ctx.fillRect(0, 0, 1, 1);

    fs.writeFileSync(path.join(OUTPUT_DIR, 'panorama_4.png'), black.toBuffer('image/png'));
    fs.writeFileSync(path.join(OUTPUT_DIR, 'panorama_5.png'), black.toBuffer('image/png'));
    console.log('  panorama_4.png (1x1 black)');
    console.log('  panorama_5.png (1x1 black)');

    console.log('Готово!');
}

main().catch((err) => {
    console.error(err);
    process.exit(1);
});
