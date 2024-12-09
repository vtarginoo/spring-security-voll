package med.voll.web_application.controller;

import jakarta.validation.Valid;
import med.voll.web_application.domain.RegraDeNegocioException;
import med.voll.web_application.domain.consulta.ConsultaService;
import med.voll.web_application.domain.consulta.DadosAgendamentoConsulta;
import med.voll.web_application.domain.medico.Especialidade;
import med.voll.web_application.domain.usuario.Usuario;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;


@Controller
@RequestMapping("consultas")
public class ConsultaController {

    private static final String PAGINA_LISTAGEM = "consulta/listagem-consultas";
    private static final String PAGINA_CADASTRO = "consulta/formulario-consulta";
    private static final String REDIRECT_LISTAGEM = "redirect:/consultas?sucesso";

    private final ConsultaService service;

    public ConsultaController(ConsultaService consultaService){
        this.service = consultaService;
    }

    @ModelAttribute("especialidades")
    public Especialidade[] especialidades() {
        return Especialidade.values();
    }

    @GetMapping
    public String carregarPaginaListagem(@PageableDefault Pageable paginacao, Model model, @AuthenticationPrincipal Usuario logado) {
        var consultasAtivas = service.listar(paginacao, logado);
        model.addAttribute("consultas", consultasAtivas);
        return PAGINA_LISTAGEM;
    }

    @GetMapping("formulario")
    @PreAuthorize("hasRole('ATENDENTE') OR hasRole('PACIENTE')")
    public String carregarPaginaAgendaConsulta(Long id, Model model) {
        if (id != null) {
            model.addAttribute("dados", service.carregarPorId(id));
        } else {
            model.addAttribute("dados", new DadosAgendamentoConsulta(null, null, null, null, null));
        }

        return PAGINA_CADASTRO;
    }

    @PostMapping
    @PreAuthorize("hasRole('ATENDENTE') OR hasRole('PACIENTE')")
    public String cadastrar(@Valid @ModelAttribute("dados") DadosAgendamentoConsulta dados, BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("dados", dados);
            return PAGINA_CADASTRO;
        }

        try {
            service.cadastrar(dados);
            return REDIRECT_LISTAGEM;
        } catch (RegraDeNegocioException e) {
            model.addAttribute("erro", e.getMessage());
            model.addAttribute("dados", dados);
            return PAGINA_CADASTRO;
        }
    }

    @DeleteMapping
    public String excluir(Long id) {
        service.excluir(id);
        return REDIRECT_LISTAGEM;
    }

}