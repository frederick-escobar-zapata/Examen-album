package ipss.cl.album.repositorio;

import ipss.cl.album.modelo.Album;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RepositorioAlbum extends JpaRepository<Album, Long> {
}
