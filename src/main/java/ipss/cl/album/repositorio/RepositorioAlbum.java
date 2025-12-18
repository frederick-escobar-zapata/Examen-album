package ipss.cl.album.repositorio;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import ipss.cl.album.modelo.Album;

public interface RepositorioAlbum extends JpaRepository<Album, Long> {

	List<Album> findByPropietarioUsername(String username);

	Optional<Album> findByIdAndPropietarioUsername(Long id, String username);
}
