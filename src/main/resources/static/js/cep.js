// quando o CEP e digitado, busca o endereco no ViaCEP e preenche rua, bairro, cidade e UF
const campoCep = document.getElementById('cep');

if (campoCep) {
    const aviso = document.getElementById('cep-aviso');

    campoCep.addEventListener('change', async function () {
        const cep = campoCep.value.replace(/\D/g, '');
        if (cep.length !== 8) {
            return;
        }

        aviso.textContent = 'Buscando o endereço...';
        try {
            const resposta = await fetch('https://viacep.com.br/ws/' + cep + '/json/');
            const endereco = await resposta.json();

            if (endereco.erro) {
                aviso.textContent = 'CEP não encontrado. Preencha o endereço manualmente.';
                return;
            }

            document.getElementById('rua').value = endereco.logradouro;
            document.getElementById('bairro').value = endereco.bairro;
            document.getElementById('cidade').value = endereco.localidade;
            document.getElementById('uf').value = endereco.uf;
            aviso.textContent = 'Endereço preenchido pelo CEP. Confira e informe o número.';
            document.getElementById('numero').focus();
        } catch (erro) {
            aviso.textContent = 'Não foi possível buscar o CEP agora. Preencha o endereço manualmente.';
        }
    });
}
