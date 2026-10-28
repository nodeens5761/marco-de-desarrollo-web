document.querySelectorAll('[data-confirm-delete]').forEach(form => {
  form.addEventListener('submit', event => {
    const title = form.dataset.courseTitle || 'este curso';
    if (!window.confirm(`¿Eliminar "${title}"?`)) {
      event.preventDefault();
    }
  });
});
