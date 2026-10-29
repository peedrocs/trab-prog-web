package com.mgp.lojaonline.service;

import com.mgp.lojaonline.dto.DadosCadastro;
import com.mgp.lojaonline.dto.DadosConta;
import com.mgp.lojaonline.exception.RegraNegocioException;
import com.mgp.lojaonline.model.Endereco;
import com.mgp.lojaonline.model.Perfil;
import com.mgp.lojaonline.model.Usuario;
import com.mgp.lojaonline.repository.UsuarioRepository;
import com.mgp.lojaonline.util.Cpf;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder codificadorDeSenha;
    private final NotificacaoService notificacaoService;
    private final SecureRandom sorteio = new SecureRandom();

    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder codificadorDeSenha,
                          NotificacaoService notificacaoService) {
        this.usuarioRepository = usuarioRepository;
        this.codificadorDeSenha = codificadorDeSenha;
        this.notificacaoService = notificacaoService;
    }

    public Usuario buscarPorEmail(String email) {
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RegraNegocioException("Usuário não encontrado"));
    }

    // nas paginas abertas a visitantes devolve null quando ninguem entrou
    public Usuario usuarioLogado(Principal principal) {
        if (principal == null) {
            return null;
        }
        return usuarioRepository.findByEmail(principal.getName()).orElse(null);
    }

    @Transactional
    public void cadastrar(DadosCadastro dados) {
        String email = dados.getEmail().trim().toLowerCase();
        if (usuarioRepository.existsByEmail(email)) {
            throw new RegraNegocioException("Já existe uma conta com este e-mail");
        }
        String cpf = Cpf.somenteNumeros(dados.getCpf());
        if (usuarioRepository.existsByCpf(cpf)) {
            throw new RegraNegocioException("Já existe uma conta com este CPF");
        }

        Usuario usuario = new Usuario();
        usuario.setNome(dados.getNome().trim());
        usuario.setEmail(email);
        usuario.setCpf(cpf);
        usuario.setSenha(codificadorDeSenha.encode(dados.getSenha()));
        usuario.setPerfil(Perfil.USER);
        usuarioRepository.save(usuario);

        notificacaoService.boasVindas(usuario);
    }

    @Transactional
    public void enviarCodigoDeLogin(String email) {
        Usuario usuario = buscarPorEmail(email);
        String codigo = String.format("%06d", sorteio.nextInt(1_000_000));
        usuario.setCodigoLogin(codigo);
        usuario.setValidadeCodigoLogin(LocalDateTime.now().plusMinutes(10));
        usuarioRepository.save(usuario);

        notificacaoService.codigoDeLogin(usuario, codigo);
    }

    @Transactional
    public boolean conferirCodigoDeLogin(Usuario usuario, String codigo) {
        boolean correto = usuario.getCodigoLogin() != null
                && usuario.getCodigoLogin().equals(codigo.trim())
                && usuario.getValidadeCodigoLogin().isAfter(LocalDateTime.now());

        if (correto) {
            // o codigo so serve uma vez
            usuario.setCodigoLogin(null);
            usuario.setValidadeCodigoLogin(null);
            usuarioRepository.save(usuario);
        }
        return correto;
    }

    @Transactional
    public Usuario entrarComGoogle(String email, String nome) {
        Usuario usuario = usuarioRepository.findByEmail(email.toLowerCase()).orElse(null);
        if (usuario == null) {
            usuario = new Usuario();
            usuario.setEmail(email.toLowerCase());
            usuario.setNome(nome == null ? email : nome);
            // quem entra pelo Google nao usa senha; se quiser uma, usa o "esqueci minha senha"
            usuario.setSenha(codificadorDeSenha.encode(UUID.randomUUID().toString()));
            usuario.setPerfil(Perfil.USER);
            usuarioRepository.save(usuario);
        }
        if (!usuario.isAtivo()) {
            throw new RegraNegocioException("Esta conta está desativada");
        }
        return usuario;
    }

    @Transactional
    public void completarCadastro(Usuario usuario, String cpf) {
        String numeros = Cpf.somenteNumeros(cpf);
        if (usuarioRepository.existsByCpf(numeros)) {
            throw new RegraNegocioException("Já existe uma conta com este CPF");
        }
        usuario.setCpf(numeros);
        usuarioRepository.save(usuario);

        notificacaoService.boasVindas(usuario);
    }

    // enquanto falta o CPF, a pessoa so consegue abrir a tela que pede o CPF
    public List<GrantedAuthority> permissoes(Usuario usuario) {
        if (usuario.getCpf() == null) {
            return List.of(new SimpleGrantedAuthority("CADASTRO_INCOMPLETO"));
        }
        return List.of(new SimpleGrantedAuthority("ROLE_" + usuario.getPerfil().name()));
    }

    public DadosConta dadosDaConta(Usuario usuario) {
        DadosConta dados = new DadosConta();
        dados.setNome(usuario.getNome());
        dados.setTelefone(usuario.getTelefone());
        if (usuario.getEndereco() != null) {
            dados.setEndereco(usuario.getEndereco().copiar());
        }
        return dados;
    }

    @Transactional
    public void atualizarConta(Usuario usuario, DadosConta dados) {
        usuario.setNome(dados.getNome().trim());
        usuario.setTelefone(dados.getTelefone());
        usuario.setEndereco(dados.getEndereco());
        usuarioRepository.save(usuario);
    }

    @Transactional
    public void guardarEndereco(Usuario usuario, Endereco endereco) {
        usuario.setEndereco(endereco.copiar());
        usuarioRepository.save(usuario);
    }

    @Transactional
    public void enviarLinkDeSenha(String email) {
        usuarioRepository.findByEmail(email.trim().toLowerCase())
                .filter(Usuario::isAtivo)
                .ifPresent(usuario -> {
                    String token = UUID.randomUUID().toString();
                    usuario.setTokenSenha(token);
                    usuario.setValidadeTokenSenha(LocalDateTime.now().plusHours(1));
                    usuarioRepository.save(usuario);
                    notificacaoService.linkDeSenha(usuario, token);
                });
    }

    public Usuario buscarPorTokenDeSenha(String token) {
        if (token == null || token.isBlank()) {
            throw new RegraNegocioException("Link de troca de senha inválido");
        }
        return usuarioRepository.findByTokenSenha(token)
                .filter(usuario -> usuario.getValidadeTokenSenha().isAfter(LocalDateTime.now()))
                .orElseThrow(() -> new RegraNegocioException("Este link expirou ou já foi usado. Peça um novo."));
    }

    @Transactional
    public void redefinirSenha(String token, String novaSenha) {
        Usuario usuario = buscarPorTokenDeSenha(token);
        usuario.setSenha(codificadorDeSenha.encode(novaSenha));
        usuario.setTokenSenha(null);
        usuario.setValidadeTokenSenha(null);
        usuarioRepository.save(usuario);
    }

    public List<Usuario> listar() {
        return usuarioRepository.findAllByOrderByNomeAsc();
    }

    @Transactional
    public void alternarAtivo(Long id, String emailDoAdmin) {
        Usuario usuario = buscarOutroUsuario(id, emailDoAdmin);
        usuario.setAtivo(!usuario.isAtivo());
        usuarioRepository.save(usuario);
    }

    @Transactional
    public void alternarPerfil(Long id, String emailDoAdmin) {
        Usuario usuario = buscarOutroUsuario(id, emailDoAdmin);
        usuario.setPerfil(usuario.getPerfil() == Perfil.ADMIN ? Perfil.USER : Perfil.ADMIN);
        usuarioRepository.save(usuario);
    }

    // o admin nao pode se desativar nem tirar o proprio perfil
    private Usuario buscarOutroUsuario(Long id, String emailDoAdmin) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RegraNegocioException("Usuário não encontrado"));
        if (usuario.getEmail().equals(emailDoAdmin)) {
            throw new RegraNegocioException("Você não pode alterar a própria conta por aqui");
        }
        return usuario;
    }
}
