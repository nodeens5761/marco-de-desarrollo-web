

// Variables de estado del juego
let lives = 3;
let score = 0;

function startGame(char) {
  document.getElementById('screen-start').classList.add('d-none');
  document.getElementById('screen-game').classList.remove('d-none');
  // Mostrar personaje seleccionado (por ejemplo)
  console.log("Jugador:", char);
}

function updateStatus() {
  document.getElementById('lives').textContent = lives;
  document.getElementById('score').textContent = score;
}

function endGame() {
  document.getElementById('screen-game').classList.add('d-none');
  document.getElementById('screen-end').classList.remove('d-none');
  document.getElementById('finalScore').textContent = score;
}

// Ejemplo de interacción: pérdida de vida
function loseLife() {
  if (lives > 0) {
    lives--;
    updateStatus();
    if (lives == 0) endGame();
  }
}

// Inicialización de tooltips (si se usaran)
document.addEventListener('DOMContentLoaded', function () {
  const tooltipTriggerList = document.querySelectorAll('[data-bs-toggle="tooltip"]');
  [...tooltipTriggerList].map(el => new bootstrap.Tooltip(el));
});
