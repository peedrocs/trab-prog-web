package com.mgp.lojaonline.controller;

import com.mgp.lojaonline.dto.DadosCadastro;
import com.mgp.lojaonline.exception.RegraNegocioException;
import com.mgp.lojaonline.model.Usuario;
import com.mgp.lojaonline.service.UsuarioService;
import com.mgp.lojaonline.util.Cpf;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

@Controller
public class AutenticacaoController {

    private static final int LIMITE_DE_TENTATIVAS = 5;

    private final UsuarioService usuarioService;
    private final SecurityContextRepository repositorioDeContexto = new HttpSessionSecurityContextRepository();
    private final RequestCache paginasPedidas = new HttpSessionRequestCache();

    public AutenticacaoController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/cadastro")
    public String cadastro(Model model) {
        model.addAttribute("dados", new DadosCadastro());
        return "cadastro";
    }

    @PostMapping("/cadastro")
    public String cadastrar(@Valid @ModelAttribute("dados") DadosCadastro dados, BindingResult resultado,
                            Model model, RedirectAttributes atributos) {
        if (!resultado.hasFieldErrors("cpf") && !Cpf.valido(dados.getCpf())) {
            resultado.rejectValue("cpf", "cpf.invalido", "CPF inválido. Confira os números digitados.");
        }
        if (resultado.hasErrors()) {
            return "cadastro";
        }

        try {
            usuarioService.cadastrar(dados);
        } catch (RegraNegocioException e) {
            model.addAttribute("erro", e.getMessage());
            return "cadastro";
        }

        atributos.addFlashAttribute("sucesso", "Conta criada! Entre com seu e-mail e senha.");
        return "redirect:/login";
    }

    @GetMapping("/login/codigo")
    public String telaDoCodigo(Principal principal, Model model) {
        model.addAttribute("email", principal.getName());
        return "login-codigo";
    }

    @PostMapping("/login/codigo")
    public String conferirCodigo(@RequestParam String codigo, Principal principal, HttpSession sessao,
                                 HttpServletRequest request, HttpServletResponse response, Model model) {
        Usuario usuario = usuarioService.buscarPorEmail(principal.getName());

        if (usuarioService.conferirCodigoDeLogin(usuario, codigo)) {
            sessao.removeAttribute("tentativasCodigo");
            liberarAcesso(usuario, request, response);
            if (usuario.getCpf() == null) {
                return "redirect:/cadastro/completar";
            }
            return "redirect:" + paginaDepoisDoLogin(request, response);
        }

        // depois de errar varias vezes, precisa entrar de novo com a senha para receber outro codigo
        Integer tentativas = (Integer) sessao.getAttribute("tentativasCodigo");
        tentativas = tentativas == null ? 1 : tentativas + 1;
        if (tentativas >= LIMITE_DE_TENTATIVAS) {
            sessao.invalidate();
            return "redirect:/login?tentativas";
        }
        sessao.setAttribute("tentativasCodigo", tentativas);

        model.addAttribute("email", principal.getName());
        model.addAttribute("erro", "Código incorreto ou vencido. Confira o e-mail e tente de novo.");
        return "login-codigo";
    }

    @PostMapping("/login/codigo/reenviar")
    public String reenviarCodigo(Principal principal, RedirectAttributes atributos) {
        usuarioService.enviarCodigoDeLogin(principal.getName());
        atributos.addFlashAttribute("sucesso", "Enviamos um código novo para o seu e-mail.");
        return "redirect:/login/codigo";
    }

    @GetMapping("/cadastro/completar")
    public String completarCadastro() {
        return "cadastro-completar";
    }

    @PostMapping("/cadastro/completar")
    public String salvarCpf(@RequestParam String cpf, Principal principal, HttpServletRequest request,
                            HttpServletResponse response, Model model, RedirectAttributes atributos) {
        Usuario usuario = usuarioService.buscarPorEmail(principal.getName());
        if (!Cpf.valido(cpf)) {
            return telaDoCpfComErro(cpf, "CPF inválido. Confira os números digitados.", model);
        }
        try {
            usuarioService.completarCadastro(usuario, cpf);
        } catch (RegraNegocioException e) {
            return telaDoCpfComErro(cpf, e.getMessage(), model);
        }

        liberarAcesso(usuario, request, response);
        atributos.addFlashAttribute("sucesso", "Cadastro completo. Boas compras!");
        return "redirect:/";
    }

    private String telaDoCpfComErro(String cpf, String erro, Model model) {
        model.addAttribute("cpf", cpf);
        model.addAttribute("erroCpf", erro);
        return "cadastro-completar";
    }

    // troca a permissao provisoria pelo perfil de verdade (cliente ou admin) e guarda na sessao
    private void liberarAcesso(Usuario usuario, HttpServletRequest request, HttpServletResponse response) {
        Authentication autenticacao = UsernamePasswordAuthenticationToken.authenticated(
                usuario.getEmail(), null, usuarioService.permissoes(usuario));
        SecurityContext contexto = SecurityContextHolder.createEmptyContext();
        contexto.setAuthentication(autenticacao);
        SecurityContextHolder.setContext(contexto);
        repositorioDeContexto.saveContext(contexto, request, response);
    }

    // volta para a pagina que a pessoa tentou abrir antes de entrar, como o checkout
    private String paginaDepoisDoLogin(HttpServletRequest request, HttpServletResponse response) {
        SavedRequest paginaPedida = paginasPedidas.getRequest(request, response);
        return paginaPedida == null ? "/" : paginaPedida.getRedirectUrl();
    }
}
