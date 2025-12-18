package ipss.cl.album.repositorio;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import ipss.cl.album.modelo.Usuario;

public interface RepositorioUsuario extends JpaRepository<Usuario, Long> {

	Optional<Usuario> findByUsername(String username);
}
