package ipss.cl.album.repositorio;

import ipss.cl.album.modelo.EstadoLamina;
import ipss.cl.album.modelo.Lamina;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RepositorioLamina extends JpaRepository<Lamina, Long> {

	List<Lamina> findByAlbumId(Long idAlbum);

	Page<Lamina> findByAlbumId(Long idAlbum, Pageable pageable);

	List<Lamina> findByAlbumIdAndEstado(Long idAlbum, EstadoLamina estado);

	Page<Lamina> findByAlbumIdAndEstado(Long idAlbum, EstadoLamina estado, Pageable pageable);

	List<Lamina> findByAlbumIdAndNumero(Long idAlbum, Integer numero);
}
