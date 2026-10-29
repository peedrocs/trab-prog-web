package com.mgp.lojaonline.service;

import com.mgp.lojaonline.model.Pagamento;
import com.mgp.lojaonline.model.Pedido;
import com.mgp.lojaonline.model.Perfil;
import com.mgp.lojaonline.model.Produto;
import com.mgp.lojaonline.model.Usuario;
import com.mgp.lojaonline.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

// monta o texto de cada e-mail; quem envia de fato e o EmailService
@Service
public class NotificacaoService {

    private static final DateTimeFormatter DATA_E_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy 'às' HH:mm");

    private final EmailService emailService;
    private final UsuarioRepository usuarioRepository;
    private final String urlLoja;

    public NotificacaoService(EmailService emailService, UsuarioRepository usuarioRepository,
                              @Value("${loja.url}") String urlLoja) {
        this.emailService = emailService;
        this.usuarioRepository = usuarioRepository;
        this.urlLoja = urlLoja;
    }

    public void boasVindas(Usuario usuario) {
        String texto = """
                Olá, %s!

                Sua conta na Papelaria MGP está pronta. Já dá para montar o carrinho, fazer pedidos e acompanhar tudo pelo site:

                %s
                """.formatted(usuario.getPrimeiroNome(), urlLoja);
        emailService.enviar(usuario.getEmail(), "Bem-vindo à Papelaria MGP", texto);
    }

    public void codigoDeLogin(Usuario usuario, String codigo) {
        String texto = """
                Olá, %s!

                Use este código para entrar na sua conta: %s

                Ele vale por 10 minutos. Se não foi você que tentou entrar, recomendamos trocar a sua senha.
                """.formatted(usuario.getPrimeiroNome(), codigo);
        emailService.enviar(usuario.getEmail(), "Seu código de acesso à Papelaria MGP", texto);
    }

    public void linkDeSenha(Usuario usuario, String token) {
        String texto = """
                Olá, %s!

                Recebemos um pedido para trocar a senha da sua conta. Para criar uma senha nova, abra o link abaixo. Ele vale por 1 hora.

                %s/senha/redefinir?token=%s

                Se não foi você, é só ignorar este e-mail.
                """.formatted(usuario.getPrimeiroNome(), urlLoja, token);
        emailService.enviar(usuario.getEmail(), "Troca de senha na Papelaria MGP", texto);
    }

    public void pedidoCriado(Pedido pedido, Pagamento pagamento) {
        String prazo = pedido.getPrazoPagamento().format(DATA_E_HORA);
        String comoPagar = switch (pedido.getFormaPagamento()) {
            case PIX -> "Para pagar com Pix, copie o código abaixo e cole no app do seu banco até " + prazo
                    + ":\n\n" + pagamento.getPixCopiaECola();
            case BOLETO -> "Para pagar com boleto, abra o link abaixo até " + prazo + ":\n\n" + pagamento.getLinkBoleto();
            case CARTAO -> "Para pagar com cartão de crédito, acesse a página do Mercado Pago até " + prazo
                    + ":\n\n" + pedido.getLinkMercadoPago();
        };

        String texto = """
                Olá, %s!

                Recebemos o seu pedido #%d, no valor de %s.

                %s

                Se o pagamento não for feito dentro do prazo, o pedido é cancelado.

                Acompanhe o pedido em %s
                """.formatted(pedido.getUsuario().getPrimeiroNome(), pedido.getId(), dinheiro(pedido.getTotal()),
                comoPagar, linkDoPedido(pedido));
        emailService.enviar(pedido.getUsuario().getEmail(), "Pedido #" + pedido.getId() + " recebido", texto);
    }

    public void pedidoPago(Pedido pedido) {
        String texto = """
                Olá, %s!

                O pagamento do pedido #%d foi confirmado. Agora vamos separar os seus produtos e avisamos assim que o pedido for enviado.

                Acompanhe o pedido em %s
                """.formatted(pedido.getUsuario().getPrimeiroNome(), pedido.getId(), linkDoPedido(pedido));
        emailService.enviar(pedido.getUsuario().getEmail(), "Pagamento confirmado: pedido #" + pedido.getId(), texto);
    }

    public void pedidoEnviado(Pedido pedido) {
        String texto = """
                Olá, %s!

                O pedido #%d saiu para entrega. O código de rastreio é %s.

                Acompanhe o pedido em %s
                """.formatted(pedido.getUsuario().getPrimeiroNome(), pedido.getId(), pedido.getCodigoRastreio(),
                linkDoPedido(pedido));
        emailService.enviar(pedido.getUsuario().getEmail(), "Pedido #" + pedido.getId() + " enviado", texto);
    }

    public void pedidoEntregue(Pedido pedido) {
        String texto = """
                Olá, %s!

                O pedido #%d foi entregue. Esperamos que goste! Se quiser, conte o que achou avaliando os produtos na página de cada um.

                %s
                """.formatted(pedido.getUsuario().getPrimeiroNome(), pedido.getId(), linkDoPedido(pedido));
        emailService.enviar(pedido.getUsuario().getEmail(), "Pedido #" + pedido.getId() + " entregue", texto);
    }

    public void pedidoCancelado(Pedido pedido, boolean estornado, String motivo) {
        String detalhe = motivo == null ? "" : " porque " + motivo;
        if (estornado) {
            detalhe += ". O valor de " + dinheiro(pedido.getTotal())
                    + " foi estornado pelo Mercado Pago e volta para a forma de pagamento que você usou";
        }

        String texto = """
                Olá, %s!

                O pedido #%d foi cancelado%s.

                Se tiver alguma dúvida, é só responder este e-mail.

                %s
                """.formatted(pedido.getUsuario().getPrimeiroNome(), pedido.getId(), detalhe, linkDoPedido(pedido));
        String assunto = estornado ? "Pedido #" + pedido.getId() + " cancelado e estornado" : "Pedido #" + pedido.getId() + " cancelado";
        emailService.enviar(pedido.getUsuario().getEmail(), assunto, texto);
    }

    public void estoqueBaixo(Produto produto) {
        String texto = """
                O produto "%s" ficou com %d unidade(s) em estoque.

                Veja os produtos em %s/admin/produtos
                """.formatted(produto.getNome(), produto.getEstoque(), urlLoja);
        for (Usuario admin : usuarioRepository.findByPerfilAndAtivoTrue(Perfil.ADMIN)) {
            emailService.enviar(admin.getEmail(), "Estoque baixo: " + produto.getNome(), texto);
        }
    }

    private String linkDoPedido(Pedido pedido) {
        return urlLoja + "/pedidos/" + pedido.getId();
    }

    private String dinheiro(BigDecimal valor) {
        return NumberFormat.getCurrencyInstance(Locale.forLanguageTag("pt-BR")).format(valor);
    }
}
