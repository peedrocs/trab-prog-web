package com.mgp.lojaonline.controller;

import com.mgp.lojaonline.exception.RegraNegocioException;
import com.mgp.lojaonline.service.CarrinhoService;
import com.mgp.lojaonline.service.UsuarioService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

@Controller
@RequestMapping("/carrinho")
public class CarrinhoController {

    private final CarrinhoService carrinhoService;
    private final UsuarioService usuarioService;

    public CarrinhoController(CarrinhoService carrinhoService, UsuarioService usuarioService) {
        this.carrinhoService = carrinhoService;
        this.usuarioService = usuarioService;
    }

    @GetMapping
    public String carrinho(HttpSession sessao, Principal principal, Model model) {
        model.addAttribute("resumo", carrinhoService.resumo(sessao, usuarioService.usuarioLogado(principal)));
        return "carrinho";
    }

    @PostMapping("/adicionar")
    public String adicionar(@RequestParam Long produtoId, @RequestParam(defaultValue = "1") int quantidade,
                            HttpSession sessao, RedirectAttributes atributos) {
        try {
            carrinhoService.adicionar(sessao, produtoId, quantidade);
            atributos.addFlashAttribute("sucesso", "Produto adicionado ao carrinho");
        } catch (RegraNegocioException e) {
            atributos.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/carrinho";
    }

    @PostMapping("/atualizar")
    public String atualizar(@RequestParam Long produtoId, @RequestParam int quantidade, HttpSession sessao,
                            RedirectAttributes atributos) {
        try {
            carrinhoService.atualizar(sessao, produtoId, quantidade);
        } catch (RegraNegocioException e) {
            atributos.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/carrinho";
    }

    @PostMapping("/remover")
    public String remover(@RequestParam Long produtoId, HttpSession sessao) {
        carrinhoService.remover(sessao, produtoId);
        return "redirect:/carrinho";
    }

    @PostMapping("/cupom")
    public String aplicarCupom(@RequestParam String codigo, HttpSession sessao, Principal principal,
                               RedirectAttributes atributos) {
        try {
            carrinhoService.aplicarCupom(sessao, codigo, usuarioService.usuarioLogado(principal));
            atributos.addFlashAttribute("sucesso", "Cupom aplicado");
        } catch (RegraNegocioException e) {
            atributos.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/carrinho";
    }

    @PostMapping("/cupom/remover")
    public String removerCupom(HttpSession sessao) {
        carrinhoService.removerCupom(sessao);
        return "redirect:/carrinho";
    }
}
