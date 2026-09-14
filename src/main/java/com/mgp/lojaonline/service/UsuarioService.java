package com.mgp.lojaonline.service;

import com.mgp.lojaonline.exception.RegraNegocioException;
import com.mgp.lojaonline.model.Perfil;
import com.mgp.lojaonline.model.Usuario;
import com.mgp.lojaonline.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder codificadorDeSenha;
    private final SessionRegistry registroDeSessoes;

    public void cadastrar(Usuario usuario) {
        String email = usuario.getEmail().trim().toLowerCase();
        if (usuarioRepository.existsByEmail(email)) {
            throw new RegraNegocioException("Já existe uma conta com este e-mail");
        }
        // o BCrypt só aceita senhas de até 72 bytes
        if (usuario.getSenha().getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new RegraNegocioException("A senha deve ter no máximo 72 caracteres");
        }

        // garante que o cadastro público não altere id, perfil ou status
        usuario.setId(null);
        usuario.setPerfil(Perfil.USER);
        usuario.setAtivo(true);

        usuario.setEmail(email);
        usuario.setSenha(codificadorDeSenha.encode(usuario.getSenha()));
        usuarioRepository.save(usuario);
    }

    public Usuario buscarPorEmail(String email) {
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado"));
    }

    public List<Usuario> listarTodos() {
        return usuarioRepository.findAllByOrderByNomeAsc();
    }

    public long contar() {
        return usuarioRepository.count();
    }

    public void alterarPerfil(Long id, String emailLogado) {
        Usuario usuario = buscarPorId(id);
        verificarSeNaoEhOProprio(usuario, emailLogado);
        usuario.setPerfil(usuario.getPerfil() == Perfil.ADMIN ? Perfil.USER : Perfil.ADMIN);
        usuarioRepository.save(usuario);
        encerrarSessoes(usuario);
    }

    public void alterarAtivo(Long id, String emailLogado) {
        Usuario usuario = buscarPorId(id);
        verificarSeNaoEhOProprio(usuario, emailLogado);
        usuario.setAtivo(!usuario.isAtivo());
        usuarioRepository.save(usuario);
        encerrarSessoes(usuario);
    }

    private Usuario buscarPorId(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado"));
    }

    private void verificarSeNaoEhOProprio(Usuario usuario, String emailLogado) {
        if (usuario.getEmail().equals(emailLogado)) {
            throw new RegraNegocioException("Você não pode alterar a sua própria conta");
        }
    }

    // obriga o usuário a entrar de novo para valer o novo perfil ou a desativação
    private void encerrarSessoes(Usuario usuario) {
        for (Object principal : registroDeSessoes.getAllPrincipals()) {
            if (principal instanceof UserDetails detalhes && detalhes.getUsername().equals(usuario.getEmail())) {
                for (SessionInformation sessao : registroDeSessoes.getAllSessions(principal, false)) {
                    sessao.expireNow();
                }
            }
        }
    }
}
