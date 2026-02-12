const fs = require('fs');
const path = require('path');
const { createCanvas } = require('canvas');

/**
 * Создает цветное PNG из двумерного массива
 * @param {Array<Array<number|Array<number>>>} matrix - Массив чисел или RGB значений
 * @param {number} cellSize - Размер ячейки в пикселях
 * @param {string} outputPath - Путь сохранения
 */
function createColorImageFromMatrix(matrix, cellSize = 20, outputPath = 'output.png') {
    if (!matrix || matrix.length === 0) {
        throw new Error('Массив не может быть пустым');
    }

    const height = matrix.length;
    const width = matrix[0].length;

    const canvas = createCanvas(width * cellSize, height * cellSize);
    const ctx = canvas.getContext('2d');

    for (let i = 0; i < height; i++) {
        for (let j = 0; j < width; j++) {
            const cell = matrix[i][j];
            
            let color;
            if (Array.isArray(cell)) {
                const [r, g, b] = cell;
                color = `rgb(${r}, ${g}, ${b})`;
            } else {
                const val = Math.min(255, Math.max(0, cell));
                color = `rgb(${val}, ${val}, ${val})`;
            }
            
            ctx.fillStyle = color;
            ctx.fillRect(j * cellSize, i * cellSize, cellSize, cellSize);
        }
    }

    const buffer = canvas.toBuffer('image/png');
    fs.writeFileSync(outputPath, buffer);
    console.log(`Изображение сохранено: ${outputPath} (${width}x${height}, cellSize=${cellSize})`);
}

/** Генерирует тестовую матрицу 16x16 */
function createDefault16x16Matrix() {
    const matrix = [];
    for (let i = 0; i < 16; i++) {
        const row = [];
        for (let j = 0; j < 16; j++) {
            const r = Math.floor((i / 15) * 255);
            const g = Math.floor((j / 15) * 255);
            const b = 128;
            row.push([r, g, b]);
        }
        matrix.push(row);
    }
    return matrix;
}

// CLI: node create-image-from-matrix.js [matrixJSON|--default] [cellSize] [outputPath]
// Пример: node create-image-from-matrix.js '[[[255,0,0],[0,255,0]],[[0,0,255],[255,255,255]]]' 16 out.png
// Пример: node create-image-from-matrix.js --default 1 output-16x16.png
const args = process.argv.slice(2);
let matrix;
let cellSize = 1;
let outputPath = 'output.png';

if (args.length === 0 || args[0] === '--default' || args[0] === '-d') {
    matrix = createDefault16x16Matrix();
    if (args[0] === '--default' || args[0] === '-d') args.shift();
    if (args.length > 0) cellSize = parseInt(args[0], 10) || 1;
    if (args.length > 1) outputPath = args[1];
    console.log('Используется матрица по умолчанию 16x16');
} else {
    const matrixArg = args[0];
    if (fs.existsSync(matrixArg) && (matrixArg.endsWith('.json') || path.extname(matrixArg) === '.json')) {
        matrix = JSON.parse(fs.readFileSync(matrixArg, 'utf8'));
    } else {
        matrix = JSON.parse(matrixArg);
    }
    if (args.length > 1) cellSize = parseInt(args[1], 10) || 1;
    if (args.length > 2) outputPath = args[2];
}

createColorImageFromMatrix(matrix, cellSize, outputPath);
