package com.mgp.lojaonline.controller;

import com.mgp.lojaonline.dto.DadosCheckout;
import com.mgp.lojaonline.dto.ResumoCompra;
import com.mgp.lojaonline.exception.RegraNegocioException;
import com.mgp.lojaonline.model.FormaPagamento;
import com.mgp.lojaonline.model.Pedido;
import com.mgp.lojaonline.model.Usuario;
import com.mgp.lojaonline.service.CarrinhoService;
import com.mgp.lojaonline.service.PedidoService;
import com.mgp.lojaonline.service.UsuarioService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

@Controller
@RequestMapping("/checkout")
public class CheckoutController {

    private final CarrinhoService carrinhoService;
    private final PedidoService pedidoService;
    private final UsuarioService usuarioService;

    public CheckoutController(CarrinhoService carrinhoService, PedidoService pedidoService,
                              UsuarioService usuarioService) {
        this.carrinhoService = carrinhoService;
        this.pedidoService = pedidoService;
        this.usuarioService = usuarioService;
    }

    @GetMapping
    public String resumo(HttpSession sessao, Principal principal, Model model, RedirectAttributes atributos) {
        Usuario usuario = usuarioService.buscarPorEmail(principal.getName());
        ResumoCompra resumo = carrinhoService.resumo(sessao, usuario);
        if (resumo.vazio()) {
            atributos.addFlashAttribute("erro", "Seu carrinho está vazio");
            return "redirect:/carrinho";
        }

        DadosCheckout dados = new DadosCheckout();
        dados.setFormaPagamento(FormaPagamento.PIX);
        if (usuario.getEndereco() != null) {
            dados.setEndereco(usuario.getEndereco().copiar());
        }
        return abrirResumo(dados, resumo, model);
    }

    @PostMapping
    public String finalizar(@Valid @ModelAttribute("dados") DadosCheckout dados, BindingResult resultado,
                            HttpSession sessao, Principal principal, Model model) {
        Usuario usuario = usuarioService.buscarPorEmail(principal.getName());
        if (resultado.hasErrors()) {
            return abrirResumo(dados, carrinhoService.resumo(sessao, usuario), model);
        }

        Pedido pedido;
        try {
            pedido = pedidoService.finalizar(usuario, carrinhoService.resumo(sessao, usuario), dados);
        } catch (RegraNegocioException e) {
            model.addAttribute("erro", e.getMessage());
            return abrirResumo(dados, carrinhoService.resumo(sessao, usuario), model);
        }

        carrinhoService.esvaziar(sessao);
        if (pedido.getFormaPagamento() == FormaPagamento.CARTAO) {
            return "redirect:" + pedido.getLinkMercadoPago();
        }
        return "redirect:/pedidos/" + pedido.getId() + "/pagamento";
    }

    private String abrirResumo(DadosCheckout dados, ResumoCompra resumo, Model model) {
        model.addAttribute("dados", dados);
        model.addAttribute("resumo", resumo);
        model.addAttribute("formas", FormaPagamento.values());
        return "checkout";
    }
}
