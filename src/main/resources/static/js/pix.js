// pagina do Pix: botao de copiar o codigo e conferencia do pagamento a cada 5 segundos
const areaPix = document.getElementById('pagamento-pix');

if (areaPix) {
    const codigo = document.getElementById('codigo-pix');
    const aviso = document.getElementById('aviso-pix');

    document.getElementById('copiar-codigo').addEventListener('click', async function () {
        codigo.select();
        try {
            await navigator.clipboard.writeText(codigo.value);
            aviso.textContent = 'Código copiado. Agora é só colar no app do banco.';
        } catch (erro) {
            aviso.textContent = 'Não foi possível copiar sozinho. O código já está selecionado, use Ctrl+C.';
        }
    });

    // quando o webhook confirmar o pagamento, o status muda e a pagina vai para o pedido
    setInterval(async function () {
        try {
            const resposta = await fetch(areaPix.dataset.urlStatus);
            const pedido = await resposta.json();
            if (pedido.status !== 'AGUARDANDO_PAGAMENTO') {
                aviso.textContent = 'Pagamento confirmado! Abrindo o seu pedido...';
                window.location.href = areaPix.dataset.urlPedido;
            }
        } catch (erro) {
            // sem internet por um instante: tenta de novo na proxima volta
        }
    }, 5000);
}
