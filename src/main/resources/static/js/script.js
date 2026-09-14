document.querySelectorAll('form[data-confirmar]').forEach(function (formulario) {
    formulario.addEventListener('submit', function (evento) {
        if (!confirm(formulario.dataset.confirmar)) {
            evento.preventDefault();
        }
    });
});

// fecha o menu sanfona ao clicar fora dele, ao sair dele com Tab ou ao apertar Esc
const menu = document.querySelector('.menu-sanfona');

if (menu) {
    function fecharSeFora(evento) {
        if (!menu.contains(evento.target)) {
            menu.open = false;
        }
    }

    document.addEventListener('click', fecharSeFora);
    document.addEventListener('focusin', fecharSeFora);

    document.addEventListener('keydown', function (evento) {
        if (evento.key === 'Escape' && menu.open) {
            menu.open = false;
            menu.querySelector('summary').focus();
        }
    });
}
