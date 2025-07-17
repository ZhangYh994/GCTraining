(function(){
  let confettiCanvas = null;
  let confettiCtx = null;
  let confettiParticles = [];
  let confettiTimer = null;
  const COLORS = ['#f44336','#ffeb3b','#4caf50','#2196f3','#ff9800','#e91e63','#00bcd4','#8bc34a'];
  const PARTICLE_COUNT = 120;
  const DURATION = 1800; 

  function randomBetween(a, b) { return a + Math.random() * (b - a); }

  function createParticle(w, h) {
    const angle = randomBetween(-Math.PI/3, -2*Math.PI/3);
    const speed = randomBetween(3, 7);
    return {
      x: randomBetween(0, w),
      y: randomBetween(-40, 0),
      r: randomBetween(5, 10),
      color: COLORS[Math.floor(Math.random()*COLORS.length)],
      vx: Math.cos(angle) * speed,
      vy: Math.sin(angle) * speed,
      ay: 0.18 + Math.random()*0.12,
      rot: randomBetween(0, 2*Math.PI),
      vr: randomBetween(-0.1, 0.1)
    };
  }

  function drawConfetti() {
    if (!confettiCanvas) return;
    const w = window.innerWidth, h = window.innerHeight;
    confettiCtx.clearRect(0,0,w,h);
    confettiParticles.forEach(p => {
      confettiCtx.save();
      confettiCtx.translate(p.x, p.y);
      confettiCtx.rotate(p.rot);
      confettiCtx.fillStyle = p.color;
      confettiCtx.beginPath();
      confettiCtx.ellipse(0, 0, p.r, p.r*0.5, 0, 0, 2*Math.PI);
      confettiCtx.fill();
      confettiCtx.restore();
    });
  }

  function updateConfetti() {
    const w = window.innerWidth, h = window.innerHeight;
    confettiParticles.forEach(p => {
      p.x += p.vx;
      p.y += p.vy;
      p.vy += p.ay;
      p.rot += p.vr;
    });
    confettiParticles = confettiParticles.filter(p => p.y < h+40);
  }

  function loop() {
    updateConfetti();
    drawConfetti();
    if (confettiParticles.length > 0) {
      confettiTimer = requestAnimationFrame(loop);
    } else {
      removeConfettiCanvas();
    }
  }

  function removeConfettiCanvas() {
    if (confettiCanvas) {
      confettiCanvas.parentNode.removeChild(confettiCanvas);
      confettiCanvas = null;
      confettiCtx = null;
    }
    if (confettiTimer) {
      cancelAnimationFrame(confettiTimer);
      confettiTimer = null;
    }
  }

  window.launchConfetti = function() {
    if (confettiCanvas) return;
    confettiCanvas = document.createElement('canvas');
    confettiCanvas.style.position = 'fixed';
    confettiCanvas.style.left = '0';
    confettiCanvas.style.top = '0';
    confettiCanvas.style.width = '100vw';
    confettiCanvas.style.height = '100vh';
    confettiCanvas.style.pointerEvents = 'none';
    confettiCanvas.style.zIndex = 99999;
    confettiCanvas.width = window.innerWidth;
    confettiCanvas.height = window.innerHeight;
    document.body.appendChild(confettiCanvas);
    confettiCtx = confettiCanvas.getContext('2d');
    confettiParticles = [];
    for(let i=0;i<PARTICLE_COUNT;i++) {
      confettiParticles.push(createParticle(confettiCanvas.width, confettiCanvas.height));
    }
    loop();
    setTimeout(removeConfettiCanvas, DURATION);
  };
})();
