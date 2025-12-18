package ipss.cl.album.controlador;

import ipss.cl.album.modelo.Album;
import ipss.cl.album.modelo.Usuario;
import ipss.cl.album.repositorio.RepositorioAlbum;
import ipss.cl.album.repositorio.RepositorioUsuario;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/albumes")
public class ControladorAlbum {

	private final RepositorioAlbum repositorioAlbum;
	private final RepositorioUsuario repositorioUsuario;

	public ControladorAlbum(RepositorioAlbum repositorioAlbum, RepositorioUsuario repositorioUsuario) {
		this.repositorioAlbum = repositorioAlbum;
		this.repositorioUsuario = repositorioUsuario;
	}

	// Con este controlador yo expongo las operaciones REST para gestionar los álbumes del usuario autenticado.

	private Usuario obtenerUsuarioActual() {
		String nombreUsuario = SecurityContextHolder.getContext().getAuthentication().getName();
		if (nombreUsuario == null) {
			return null;
		}
		return repositorioUsuario.findByUsername(nombreUsuario).orElse(null);
	}

	@GetMapping
	public List<Album> obtenerTodosLosAlbumes() {
		Usuario usuarioActual = obtenerUsuarioActual();
		return repositorioAlbum.findByPropietarioUsername(usuarioActual.getUsername());
	}

	@GetMapping("/{id}")
	public ResponseEntity<?> obtenerAlbumPorId(@PathVariable Long id) {
		Usuario usuarioActual = obtenerUsuarioActual();
		Optional<Album> posibleAlbum = repositorioAlbum.findByIdAndPropietarioUsername(id, usuarioActual.getUsername());
		if (posibleAlbum.isEmpty()) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND)
					.body(Map.of("mensaje", "Álbum no encontrado o no pertenece al usuario autenticado"));
		}
		return ResponseEntity.ok(posibleAlbum.get());
	}

	@PostMapping
	public ResponseEntity<Album> crearAlbum(@RequestBody Album album) {
		Usuario usuarioActual = obtenerUsuarioActual();
		album.asignarPropietario(usuarioActual);
		Album albumGuardado = repositorioAlbum.save(album);
		return ResponseEntity.status(HttpStatus.CREATED).body(albumGuardado);
	}

	@PutMapping("/{id}")
	public ResponseEntity<?> actualizarAlbum(@PathVariable Long id, @RequestBody Album albumPedido) {
		Usuario usuarioActual = obtenerUsuarioActual();
		Optional<Album> posibleAlbum = repositorioAlbum.findByIdAndPropietarioUsername(id, usuarioActual.getUsername());
		if (posibleAlbum.isEmpty()) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND)
					.body(Map.of("mensaje", "Álbum no encontrado o no pertenece al usuario autenticado"));
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
	public ResponseEntity<?> eliminarAlbum(@PathVariable Long id) {
		Usuario usuarioActual = obtenerUsuarioActual();
		Optional<Album> posibleAlbum = repositorioAlbum.findByIdAndPropietarioUsername(id, usuarioActual.getUsername());
		if (posibleAlbum.isEmpty()) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND)
					.body(Map.of("mensaje", "Álbum no encontrado o no pertenece al usuario autenticado"));
		}
		repositorioAlbum.delete(posibleAlbum.get());
		return ResponseEntity.noContent().build();
	}
}
