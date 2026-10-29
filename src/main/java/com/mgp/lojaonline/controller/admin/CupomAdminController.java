package com.mgp.lojaonline.controller.admin;

import com.mgp.lojaonline.exception.RegraNegocioException;
import com.mgp.lojaonline.model.Cupom;
import com.mgp.lojaonline.model.TipoCupom;
import com.mgp.lojaonline.repository.CupomRepository;
import com.mgp.lojaonline.service.CupomService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;

@Controller
@RequestMapping("/admin/cupons")
public class CupomAdminController {

    private final CupomRepository cupomRepository;
    private final CupomService cupomService;

    public CupomAdminController(CupomRepository cupomRepository, CupomService cupomService) {
        this.cupomRepository = cupomRepository;
        this.cupomService = cupomService;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("cupons", cupomRepository.findAllByOrderByValidadeDesc());
        model.addAttribute("hoje", LocalDate.now());
        return "admin/cupons/lista";
    }

    @GetMapping("/novo")
    public String novo(Model model) {
        Cupom cupom = new Cupom();
        cupom.setTipo(TipoCupom.PERCENTUAL);
        cupom.setValidade(LocalDate.now().plusMonths(1));
        return abrirFormulario(cupom, model);
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model) {
        Cupom cupom = cupomRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        return abrirFormulario(cupom, model);
    }

    @PostMapping("/salvar")
    public String salvar(@Valid @ModelAttribute Cupom cupom, BindingResult resultado, Model model,
                         RedirectAttributes atributos) {
        if (resultado.hasErrors()) {
            return abrirFormulario(cupom, model);
        }
        try {
            cupomService.salvar(cupom);
        } catch (RegraNegocioException e) {
            model.addAttribute("erro", e.getMessage());
            return abrirFormulario(cupom, model);
        }
        atributos.addFlashAttribute("sucesso", "Cupom salvo com sucesso");
        return "redirect:/admin/cupons";
    }

    private String abrirFormulario(Cupom cupom, Model model) {
        model.addAttribute("cupom", cupom);
        model.addAttribute("tipos", TipoCupom.values());
        return "admin/cupons/formulario";
    }
}
