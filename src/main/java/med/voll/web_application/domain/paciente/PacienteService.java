package med.voll.web_application.domain.paciente;

import med.voll.web_application.domain.RegraDeNegocioException;
import med.voll.web_application.domain.usuario.Perfil;
import med.voll.web_application.domain.usuario.UsuarioService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PacienteService {

    private final PacienteRepository repository;
    private final UsuarioService usuarioService;

    public PacienteService(PacienteRepository repository, UsuarioService usuarioService) {
        this.repository = repository;
        this.usuarioService = usuarioService;
    }

    public Page<DadosListagemPaciente> listar(Pageable paginacao) {
        return repository.findAll(paginacao).map(DadosListagemPaciente::new);
    }

    @Transactional
    public void cadastrar(DadosCadastroPaciente dados) {
        if (repository.isJaCadastrado(dados.email(), dados.cpf(), dados.id())) {
            throw new RegraDeNegocioException("E-mail ou CPF já cadastrado para outro paciente!");
        }

        if (dados.id() == null) {
            Long usuarioId = usuarioService.salvarUsuario(dados.nome(), dados.email(),  Perfil.PACIENTE);
            repository.save(new Paciente(usuarioId, dados));
        } else {
            var paciente = repository.findById(dados.id()).orElseThrow();
            paciente.modificarDados(dados);
        }
    }

    public DadosCadastroPaciente carregarPorId(Long id) {
        var paciente = repository.findById(id).orElseThrow();
        return new DadosCadastroPaciente(paciente.getId(), paciente.getNome(), paciente.getEmail(), paciente.getTelefone(), paciente.getCpf());
    }

    @Transactional
    public void excluir(Long id) {
        repository.deleteById(id);
        usuarioService.excluir(id);
    }

}
