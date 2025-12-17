package ipss.cl.album.controlador;

import ipss.cl.album.modelo.Album;
import ipss.cl.album.repositorio.RepositorioAlbum;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/albumes")
public class ControladorAlbum {

	private final RepositorioAlbum repositorioAlbum;

	public ControladorAlbum(RepositorioAlbum repositorioAlbum) {
		this.repositorioAlbum = repositorioAlbum;
	}

	@GetMapping
	public List<Album> obtenerTodosLosAlbumes() {
		return repositorioAlbum.findAll();
	}

	@GetMapping("/{id}")
	public ResponseEntity<Album> obtenerAlbumPorId(@PathVariable Long id) {
		Optional<Album> posibleAlbum = repositorioAlbum.findById(id);
		return posibleAlbum.map(ResponseEntity::ok)
				.orElseGet(() -> ResponseEntity.notFound().build());
	}

	@PostMapping
	public ResponseEntity<Album> crearAlbum(@RequestBody Album album) {
		Album albumGuardado = repositorioAlbum.save(album);
		return ResponseEntity.status(HttpStatus.CREATED).body(albumGuardado);
	}

	@PutMapping("/{id}")
	public ResponseEntity<Album> actualizarAlbum(@PathVariable Long id, @RequestBody Album albumPedido) {
		Optional<Album> posibleAlbum = repositorioAlbum.findById(id);
		if (posibleAlbum.isEmpty()) {
			return ResponseEntity.notFound().build();
		}

		Album albumExistente = posibleAlbum.get();
		albumExistente.asignarNombre(albumPedido.obtenerNombre());
		albumExistente.asignarAnio(albumPedido.obtenerAnio());
		albumExistente.asignarFechaLanzamiento(albumPedido.obtenerFechaLanzamiento());
		albumExistente.asignarDescripcion(albumPedido.obtenerDescripcion());
		albumExistente.asignarCantidadTotalLaminas(albumPedido.obtenerCantidadTotalLaminas());

		Album albumActualizado = repositorioAlbum.save(albumExistente);
		return ResponseEntity.ok(albumActualizado);
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> eliminarAlbum(@PathVariable Long id) {
		if (!repositorioAlbum.existsById(id)) {
			return ResponseEntity.notFound().build();
		}
		repositorioAlbum.deleteById(id);
		return ResponseEntity.noContent().build();
	}
}
