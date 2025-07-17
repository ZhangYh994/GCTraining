const container = document.getElementById('jigsaw-container');
const board = document.getElementById('puzzle-board');
const originalImage = document.getElementById('original-image');
const imageInput = document.getElementById('image-input');
const difficultySelect = document.getElementById('difficulty');
const restartBtn = document.getElementById('restart-btn');
const message = document.getElementById('game-message');

let img = new window.Image();
let n = 3;
let pieces = [];
let pieceSize = 100;
const BOARD_WIDTH = 0.9 * 0.7 * 900;
const BOARD_HEIGHT = 0.95 * 600;
let imgUrl = '';
let choosingPieces = []; 
let offsetX = 0, offsetY = 0;
let solved = false;

function shuffle(arr) {
  for (let i = arr.length - 1; i > 0; i--) {
    const j = Math.floor(Math.random() * (i + 1));
    [arr[i], arr[j]] = [arr[j], arr[i]];
  }
}

// 设置拼图区尺寸
function setBoardSize() {
  board.style.width = BOARD_WIDTH + 'px';
  board.style.height = BOARD_HEIGHT + 'px';
  pieceSize = Math.floor(Math.min(BOARD_WIDTH, BOARD_HEIGHT) / n);
}

// 创建拼图碎片
function createPieces() {
  setBoardSize();
  // 如果碎片数量不对（如难度变化），重建碎片
  if (pieces.length !== n * n) {
    pieces = [];
    board.innerHTML = '';
    for (let row = 0; row < n; row++) {
      for (let col = 0; col < n; col++) {
        const piece = {
          row, col,
          correctLeft: col * pieceSize,
          correctTop: row * pieceSize,
          idx: row * n + col,
        };
        const div = document.createElement('div');
        div.className = 'puzzle-piece in-board';
        div.style.width = div.style.height = pieceSize + 'px';
        div.style.backgroundImage = `url('${imgUrl}')`;
        div.style.backgroundSize = `${pieceSize * n}px ${pieceSize * n}px`;
        div.style.backgroundPosition = `-${piece.col * pieceSize}px -${piece.row * pieceSize}px`;
        div.setAttribute('data-idx', piece.idx);
        div.addEventListener('click', onChoosing);
        piece.el = div;
        board.appendChild(div);
        pieces.push(piece);
      }
    }
  }
  // 洗牌并平滑移动碎片
  shuffle(pieces);
  pieces.forEach((piece, i) => {
    // 每次都同步更新图片和位置
    piece.el.style.backgroundImage = `url('${imgUrl}')`;
    piece.el.style.backgroundSize = `${pieceSize * n}px ${pieceSize * n}px`;
    piece.el.style.backgroundPosition = `-${piece.col * pieceSize}px -${piece.row * pieceSize}px`;
    const L = (i % n) * (BOARD_WIDTH / n);
    const T = Math.floor(i / n) * (BOARD_HEIGHT / n);
    piece.left = L;
    piece.top = T;
    piece.el.style.left = L + 'px';
    piece.el.style.top = T + 'px';
    piece.el.classList.remove('choosing');
  });
  board.style.backgroundImage = 'none';
  board.style.backgroundSize = '';
  board.style.backgroundPosition = '';
}

// 选择和交换碎片
function onChoosing(e) {
  if (solved) return;
  const piece = pieces.find(p => p.el === e.target);
  if (!piece) return;
  if (piece.el.classList.contains('choosing')) {
    piece.el.classList.remove('choosing');
    choosingPieces = choosingPieces.filter(p => p !== piece);
    return;
  }
  if (choosingPieces.length < 2) {
    piece.el.classList.add('choosing');
    choosingPieces.push(piece);
    if (choosingPieces.length === 2) {
      swapPieces(choosingPieces[0], choosingPieces[1]);
      choosingPieces.forEach(p => p.el.classList.remove('choosing'));
      choosingPieces = [];
    }
  }
  checkSolved();
}

// 交换两个碎片的位置
function swapPieces(p1, p2) {
  const left1 = p1.el.style.left;
  const top1 = p1.el.style.top;
  p1.el.style.left = p2.el.style.left;
  p1.el.style.top = p2.el.style.top;
  p2.el.style.left = left1;
  p2.el.style.top = top1;
  p1.left = parseFloat(p1.el.style.left);
  p1.top = parseFloat(p1.el.style.top);
  p2.left = parseFloat(p2.el.style.left);
  p2.top = parseFloat(p2.el.style.top);
}

// 检查是否拼图完成
function checkSolved() {
  solved = pieces.every(p =>
    Math.abs((p.left ?? -999) - p.correctLeft) < 10 &&
    Math.abs((p.top ?? -999) - p.correctTop) < 10
  );
  if (solved) {
    message.textContent = '🎉 恭喜你完成拼图!';
    pieces.forEach(p => p.el.style.cursor = 'default');
    // 动态加载并触发全屏礼花特效
    if (!window.launchConfetti) {
      const script = document.createElement('script');
      script.src = 'confetti.js';
      script.onload = () => window.launchConfetti && window.launchConfetti();
      document.body.appendChild(script);
    } else {
      window.launchConfetti();
    }
  } else {
    message.textContent = '';
  }
}

// 游戏初始化和重开逻辑
function startGame() {
  solved = false;
  message.textContent = '';
  if (!imgUrl) return;
  originalImage.src = imgUrl;
  createPieces();
}

difficultySelect.addEventListener('change', function() {
  n = parseInt(this.value, 10);
  startGame();
});

restartBtn.addEventListener('click', startGame);

imageInput.addEventListener('change', function(e) {
  const file = e.target.files[0];
  if (!file) return;
  const reader = new FileReader();
  reader.onload = function(e) {
    imgUrl = e.target.result;
    img.src = imgUrl;
    img.onload = () => startGame(true);
  };
  reader.readAsDataURL(file);
});

window.addEventListener('DOMContentLoaded', () => {
  imgUrl = 'https://ts2.tc.mm.bing.net/th/id/OIP-C.LrQJFtU3pE762ydAZbaEUAHaHa?rs=1&pid=ImgDetMain&o=7&rm=3';
  img.src = imgUrl;
  img.onload = () => startGame();
  originalImage.src = imgUrl;
});

