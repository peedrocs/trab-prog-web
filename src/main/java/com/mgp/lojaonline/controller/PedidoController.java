package com.mgp.lojaonline.controller;

import com.mgp.lojaonline.model.ItemPedido;
import com.mgp.lojaonline.model.Pedido;
import com.mgp.lojaonline.model.Produto;
import com.mgp.lojaonline.model.StatusPedido;
import com.mgp.lojaonline.model.Usuario;
import com.mgp.lojaonline.repository.PedidoRepository;
import com.mgp.lojaonline.repository.ProdutoRepository;
import com.mgp.lojaonline.repository.UsuarioRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Map;

@Controller
@RequestMapping("/pedidos")
public class PedidoController {

    private final PedidoRepository pedidoRepository;
    private final ProdutoRepository produtoRepository;
    private final UsuarioRepository usuarioRepository;

    public PedidoController(PedidoRepository pedidoRepository, ProdutoRepository produtoRepository,
                            UsuarioRepository usuarioRepository) {
        this.pedidoRepository = pedidoRepository;
        this.produtoRepository = produtoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @GetMapping
    public String meusPedidos(Principal principal, Model model) {
        model.addAttribute("pedidos", pedidoRepository.findByUsuarioOrderByDataDesc(usuarioLogado(principal)));
        return "pedidos/lista";
    }

    @GetMapping("/{id}")
    public String detalhe(@PathVariable Long id, Principal principal, Model model) {
        Usuario usuario = usuarioLogado(principal);
        Pedido pedido = pedidoRepository.findById(id)
                .filter(p -> p.getUsuario().getId().equals(usuario.getId()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        model.addAttribute("pedido", pedido);
        return "pedidos/detalhe";
    }

    @PostMapping("/finalizar")
    public String finalizar(Principal principal, HttpSession sessao, RedirectAttributes atributos) {
        Map<Long, Integer> carrinho = CarrinhoController.carrinho(sessao);
        if (carrinho.isEmpty()) {
            atributos.addFlashAttribute("erro", "Seu carrinho está vazio");
            return "redirect:/carrinho";
        }

        Pedido pedido = new Pedido();
        pedido.setUsuario(usuarioLogado(principal));
        pedido.setData(LocalDateTime.now());
        pedido.setStatus(StatusPedido.AGUARDANDO_PAGAMENTO);

        BigDecimal total = BigDecimal.ZERO;
        for (Map.Entry<Long, Integer> entrada : carrinho.entrySet()) {
            Produto produto = produtoRepository.findById(entrada.getKey()).orElseThrow();
            ItemPedido item = new ItemPedido();
            item.setPedido(pedido);
            item.setProduto(produto);
            item.setQuantidade(entrada.getValue());
            item.setPrecoUnitario(produto.getPreco());
            pedido.getItens().add(item);
            total = total.add(item.getSubtotal());
        }
        pedido.setTotal(total);

        pedidoRepository.save(pedido);
        carrinho.clear();
        atributos.addFlashAttribute("sucesso", "Pedido realizado com sucesso");
        return "redirect:/pedidos/" + pedido.getId();
    }

    private Usuario usuarioLogado(Principal principal) {
        return usuarioRepository.findByEmail(principal.getName()).orElseThrow();
    }
}
