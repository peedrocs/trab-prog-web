package com.mgp.lojaonline.repository;

import com.mgp.lojaonline.model.Perfil;
import com.mgp.lojaonline.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByEmail(String email);

    Optional<Usuario> findByTokenSenha(String tokenSenha);

    boolean existsByEmail(String email);

    boolean existsByCpf(String cpf);

    List<Usuario> findAllByOrderByNomeAsc();

    List<Usuario> findByPerfilAndAtivoTrue(Perfil perfil);

    List<Usuario> findByNomeContainingIgnoreCaseOrEmailContainingIgnoreCaseOrderByNomeAsc(String nome, String email);
}
