
const container = document.getElementById('jigsaw-container');
const board = document.getElementById('puzzle-board');
const piecesPool = document.getElementById('pieces-pool');
const originalImage = document.getElementById('original-image');
const imageInput = document.getElementById('image-input');
const difficultySelect = document.getElementById('difficulty');
const restartBtn = document.getElementById('restart-btn');
const message = document.getElementById('game-message');

let img = new window.Image();

let n = 3;
let pieces = [];
let pieceSize = 100;
const BOARD_WIDTH = 0.6 * 0.7 * 1200;
const POOL_WIDTH = 0.4 * 0.7 * 1400;
const BOARD_HEIGHT = 0.85 * 600;

// 洗牌动画时长（毫秒）
const SHUFFLE_ANIMATION_DURATION = 600;
let imgUrl = '';
let draggingPiece = null;
let offsetX = 0, offsetY = 0;
let solved = false;

function shuffle(arr) {
  for (let i = arr.length - 1; i > 0; i--) {
    const j = Math.floor(Math.random() * (i + 1));
    [arr[i], arr[j]] = [arr[j], arr[i]];
  }
}


function setBoardSize() {
  // 固定拼图区大小，碎片大小随难度变化
  board.style.width = BOARD_WIDTH + 'px';
  board.style.height = BOARD_HEIGHT + 'px';
  pieceSize = Math.floor(Math.min(BOARD_WIDTH, BOARD_HEIGHT) / n);
}



function createPieces() {
  pieces = [];
  board.innerHTML = '';
  piecesPool.innerHTML = '';
  setBoardSize();
  for (let row = 0; row < n; row++) {
    for (let col = 0; col < n; col++) {
      pieces.push({
        row, col,
        correctLeft: col * pieceSize,
        correctTop: row * pieceSize,
        idx: row * n + col,
        inBoard: false // 初始都在碎片池
      });
    }
  }
  shuffle(pieces);
  // 随机分布碎片池内碎片
  pieces.forEach((piece, i) => {
    const div = document.createElement('div');
    div.className = 'puzzle-piece in-pool';
    div.style.width = div.style.height = pieceSize + 'px';
    div.style.backgroundImage = `url('${imgUrl}')`;
    div.style.backgroundSize = `${pieceSize * n}px ${pieceSize * n}px`;
    div.style.backgroundPosition = `-${piece.col * pieceSize}px -${piece.row * pieceSize}px`;
    div.setAttribute('data-idx', piece.idx);
    div.addEventListener('mousedown', onDragStart);
    div.addEventListener('touchstart', onDragStart, {passive: false});
    piecesPool.appendChild(div);
    piece.el = div;
    piece.left = null;
    piece.top = null;
    // 随机位置和z-index
    const maxL = POOL_WIDTH - pieceSize - 10;
    const maxT = BOARD_HEIGHT - pieceSize - 10;
    const randL = Math.floor(Math.random() * maxL) + 5;
    const randT = Math.floor(Math.random() * maxT) + 5;
    div.style.left = randL + 'px';
    div.style.top = randT + 'px';
    div.style.zIndex = 10 + Math.floor(Math.random() * 20);
  });
  // 移除拼图区背景
  board.style.backgroundImage = 'none';
  board.style.backgroundSize = '';
  board.style.backgroundPosition = '';
}


// 只洗牌并动画移动现有碎片，不重建碎片池
function shufflePiecesWithAnimation() {
  if (pieces.length === 0) return;
  // 生成新乱序
  let idxArr = pieces.map((_, i) => i);
  shuffle(idxArr);
  // 计算新目标位置
  const maxL = POOL_WIDTH - pieceSize - 10;
  const maxT = BOARD_HEIGHT - pieceSize - 10;
  const targets = idxArr.map(() => ({
    left: Math.floor(Math.random() * maxL) + 5,
    top: Math.floor(Math.random() * maxT) + 5
  }));
  // 1. 先获取碎片池的rect
  const poolRect = piecesPool.getBoundingClientRect();
  // 2. 先处理所有在拼图区（board）里的碎片，让它们用fixed飞回池子
  pieces.forEach(piece => {
    const el = piece.el;
    if (el.parentElement === board) {
      const rect = el.getBoundingClientRect();
      el.style.transition = 'none';
      el.style.position = 'fixed';
      el.style.left = rect.left + 'px';
      el.style.top = rect.top + 'px';
      el.style.zIndex = 9999;
      document.body.appendChild(el);
    }
  });

  // 3. 强制reflow，保证fixed生效
  void document.body.offsetHeight;

  // 4. 统一append到碎片池，并设置动画起点
  pieces.forEach((piece, i) => {
    const el = piece.el;
    // 计算目标left/top（相对碎片池）
    const targetLeft = targets[i].left;
    const targetTop = targets[i].top;
    if (el.parentElement !== piecesPool) {
      // board来的碎片：fixed->absolute，left/top为fixed时的相对池子位置
      piecesPool.appendChild(el);
      const rect = el.getBoundingClientRect();
      const startLeft = rect.left - poolRect.left;
      const startTop = rect.top - poolRect.top;
      el.style.transition = 'none';
      el.style.position = 'absolute';
      el.style.left = startLeft + 'px';
      el.style.top = startTop + 'px';
    } else {
      // 池内碎片：直接保持absolute，left/top不变
      el.style.transition = 'none';
      el.style.position = 'absolute';
      // 不动，动画起点就是当前left/top
    }
    el.style.zIndex = 10 + Math.floor(Math.random() * 20);
    el.classList.add('in-pool');
    el.classList.remove('snapping', 'dragging');
    piece.left = null;
    piece.top = null;
    piece.inBoard = false;
  });

  // 5. 强制reflow，再触发动画到目标位置
  void piecesPool.offsetHeight;
  pieces.forEach((piece, i) => {
    const el = piece.el;
    el.style.transition = `left ${SHUFFLE_ANIMATION_DURATION}ms, top ${SHUFFLE_ANIMATION_DURATION}ms, box-shadow 0.18s, transform 0.18s`;
    el.style.left = targets[i].left + 'px';
    el.style.top = targets[i].top + 'px';
  });

  // 6. 动画结束后清理zIndex
  setTimeout(() => {
    pieces.forEach(piece => {
      piece.el.style.zIndex = '';
    });
  }, SHUFFLE_ANIMATION_DURATION + 20);
}


function onDragStart(e) {
  if (solved) return;
  draggingPiece = pieces.find(p => p.el === e.target);
  if (!draggingPiece) return;
  draggingPiece.el.classList.add('dragging');
  // 记录初始鼠标与碎片左上角的偏移
  const clientX = e.touches ? e.touches[0].clientX : e.clientX;
  const clientY = e.touches ? e.touches[0].clientY : e.clientY;
  // 计算当前碎片在屏幕上的绝对位置
  const rect = draggingPiece.el.getBoundingClientRect();
  offsetX = clientX - rect.left;
  offsetY = clientY - rect.top;
  // 切换为fixed定位，left/top为当前屏幕坐标，append到body
  draggingPiece.el.style.transition = 'none';
  draggingPiece.el.style.position = 'fixed';
  draggingPiece.el.style.left = rect.left + 'px';
  draggingPiece.el.style.top = rect.top + 'px';
  draggingPiece.el.style.zIndex = 9999;
  document.body.appendChild(draggingPiece.el);
  document.addEventListener('mousemove', onDragMove);
  document.addEventListener('mouseup', onDragEnd);
  document.addEventListener('touchmove', onDragMove, {passive: false});
  document.addEventListener('touchend', onDragEnd);
}


function onDragMove(e) {
  if (!draggingPiece) return;
  e.preventDefault && e.preventDefault();
  const clientX = e.touches ? e.touches[0].clientX : e.clientX;
  const clientY = e.touches ? e.touches[0].clientY : e.clientY;
  // 允许拖拽的最大区域：拼图区和碎片池的包围盒
  const boardRect = board.getBoundingClientRect();
  const poolRect = piecesPool.getBoundingClientRect();
  const minX = Math.min(boardRect.left, poolRect.left);
  const maxX = Math.max(boardRect.right, poolRect.right) - pieceSize;
  const minY = Math.min(boardRect.top, poolRect.top);
  const maxY = Math.max(boardRect.bottom, poolRect.bottom) - pieceSize;
  let x = clientX - offsetX;
  let y = clientY - offsetY;
  // 限制只能在主区域包围盒内
  x = Math.max(minX, Math.min(maxX, x));
  y = Math.max(minY, Math.min(maxY, y));
  draggingPiece.el.style.left = x + 'px';
  draggingPiece.el.style.top = y + 'px';
}


function onDragEnd(e) {
  if (!draggingPiece) return;
  // 计算鼠标释放点，判断落在哪个区域
  const clientX = e.changedTouches ? e.changedTouches[0].clientX : e.clientX;
  const clientY = e.changedTouches ? e.changedTouches[0].clientY : e.clientY;
  const boardRect = board.getBoundingClientRect();
  const poolRect = piecesPool.getBoundingClientRect();
  let targetParent, targetLeft, targetTop, isBoard = false;
  if (
    clientX >= boardRect.left && clientX <= boardRect.right &&
    clientY >= boardRect.top && clientY <= boardRect.bottom
  ) {
    // 拼图区
    isBoard = true;
    let x = clientX - boardRect.left - offsetX;
    let y = clientY - boardRect.top - offsetY;
    x = Math.max(0, Math.min(BOARD_WIDTH - pieceSize, x));
    y = Math.max(0, Math.min(BOARD_HEIGHT - pieceSize, y));
    targetParent = board;
    targetLeft = x;
    targetTop = y;
    draggingPiece.left = x;
    draggingPiece.top = y;
    draggingPiece.inBoard = true;
  } else if (
    clientX >= poolRect.left && clientX <= poolRect.right &&
    clientY >= poolRect.top && clientY <= poolRect.bottom
  ) {
    // 碎片池
    let x = clientX - poolRect.left - pieceSize / 2;
    let y = clientY - poolRect.top - pieceSize / 2;
    x = Math.max(0, Math.min(POOL_WIDTH - pieceSize, x));
    y = Math.max(0, Math.min(BOARD_HEIGHT - pieceSize, y));
    targetParent = piecesPool;
    targetLeft = x;
    targetTop = y;
    draggingPiece.left = null;
    draggingPiece.top = null;
    draggingPiece.inBoard = false;
  }
  // 切回absolute定位，append到目标容器，吸附动画
  if (targetParent) {
    // 计算当前fixed位置相对目标容器的left/top
    const rect = draggingPiece.el.getBoundingClientRect();
    const parentRect = targetParent.getBoundingClientRect();
    let startLeft = rect.left - parentRect.left;
    let startTop = rect.top - parentRect.top;
    let finalLeft = targetLeft;
    let finalTop = targetTop;
    // 如果落在拼图区，吸附到最近格子
    if (isBoard) {
      let snapped = false;
      for (let row = 0; row < n; row++) {
        for (let col = 0; col < n; col++) {
          const gridLeft = col * pieceSize;
          const gridTop = row * pieceSize;
          if (
            Math.abs(finalLeft - gridLeft) < pieceSize * 0.5 &&
            Math.abs(finalTop - gridTop) < pieceSize * 0.5
          ) {
            finalLeft = gridLeft;
            finalTop = gridTop;
            snapped = true;
            break;
          }
        }
        if (snapped) break;
      }
    }
    draggingPiece.el.style.transition = 'none';
    draggingPiece.el.style.position = 'absolute';
    draggingPiece.el.style.left = startLeft + 'px';
    draggingPiece.el.style.top = startTop + 'px';
    draggingPiece.el.style.zIndex = 10 + Math.floor(Math.random() * 20);
    targetParent.appendChild(draggingPiece.el);
    // 强制reflow
    void draggingPiece.el.offsetHeight;
    // 触发吸附动画
    draggingPiece.el.style.transition = 'left 0.18s, top 0.18s, box-shadow 0.18s, transform 0.18s';
    draggingPiece.el.style.left = finalLeft + 'px';
    draggingPiece.el.style.top = finalTop + 'px';
    if (isBoard) {
      draggingPiece.left = finalLeft;
      draggingPiece.top = finalTop;
      draggingPiece.el.classList.remove('in-pool');
      // 如果吸附到正确格子，再加吸附动画
      if (
        Math.abs(finalLeft - draggingPiece.correctLeft) < 10 &&
        Math.abs(finalTop - draggingPiece.correctTop) < 10
      ) {
        draggingPiece.el.classList.add('snapping');
        draggingPiece.el.style.zIndex = 0;
        setTimeout(() => draggingPiece.el.classList.remove('snapping'), 200);
      }
    } else {
      draggingPiece.el.classList.add('in-pool');
      draggingPiece.el.classList.remove('snapping');
    }
  }
  draggingPiece.el.classList.remove('dragging');
  draggingPiece = null;
  document.removeEventListener('mousemove', onDragMove);
  document.removeEventListener('mouseup', onDragEnd);
  document.removeEventListener('touchmove', onDragMove);
  document.removeEventListener('touchend', onDragEnd);
  checkSolved();
}


function checkSolved() {
  solved = pieces.every(p =>
    p.inBoard &&
    Math.abs((p.left ?? -999) - p.correctLeft) < 2 &&
    Math.abs((p.top ?? -999) - p.correctTop) < 2
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




function startGame(forceRebuild = false) {
  solved = false;
  message.textContent = '';
  if (!imgUrl) return;
  originalImage.src = imgUrl;

  // 只有首次或结构变化时才重建碎片池
  if (piecesPool.children.length === 0 || forceRebuild) {
    createPieces();
  } else {
    shufflePiecesWithAnimation();
  }
}


difficultySelect.addEventListener('change', function() {
  n = parseInt(this.value, 10);
  startGame(true); // 难度变化强制重建碎片池
});

restartBtn.addEventListener('click', function() {
  startGame(false); // 只洗牌动画，不重建碎片池
});

imageInput.addEventListener('change', function(e) {
  const file = e.target.files[0];
  if (!file) return;
  const reader = new FileReader();
  reader.onload = function(evt) {
    imgUrl = evt.target.result;
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
