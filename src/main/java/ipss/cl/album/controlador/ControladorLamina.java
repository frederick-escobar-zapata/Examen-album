package ipss.cl.album.controlador;

import ipss.cl.album.modelo.Album;
import ipss.cl.album.modelo.EstadoLamina;
import ipss.cl.album.modelo.Lamina;
import ipss.cl.album.modelo.Usuario;
import ipss.cl.album.repositorio.RepositorioAlbum;
import ipss.cl.album.repositorio.RepositorioLamina;
import ipss.cl.album.repositorio.RepositorioUsuario;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Comparator;
import java.util.Objects;

@RestController
@RequestMapping("/api/albumes/{idAlbum}/laminas")
public class ControladorLamina {

	private final RepositorioAlbum repositorioAlbum;
	private final RepositorioLamina repositorioLamina;
	private final RepositorioUsuario repositorioUsuario;

	public ControladorLamina(RepositorioAlbum repositorioAlbum, RepositorioLamina repositorioLamina,
			RepositorioUsuario repositorioUsuario) {
		this.repositorioAlbum = repositorioAlbum;
		this.repositorioLamina = repositorioLamina;
		this.repositorioUsuario = repositorioUsuario;
	}

	// Con este controlador yo gestiono vía API las láminas asociadas a los álbumes del usuario.

	private Usuario obtenerUsuarioActual() {
		String nombreUsuario = SecurityContextHolder.getContext().getAuthentication().getName();
		if (nombreUsuario == null) {
			return null;
		}
		return repositorioUsuario.findByUsername(nombreUsuario).orElse(null);
	}

	@GetMapping
	public ResponseEntity<?> obtenerLaminasDeAlbum(@PathVariable Long idAlbum) {
		Usuario usuarioActual = obtenerUsuarioActual();
		Optional<Album> posibleAlbum = repositorioAlbum.findByIdAndPropietarioUsername(idAlbum,
				usuarioActual.getUsername());
		if (posibleAlbum.isEmpty()) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND)
					.body(Map.of("mensaje", "Álbum no encontrado o no pertenece al usuario autenticado"));
		}
		List<Lamina> laminas = repositorioLamina.findByAlbumId(idAlbum);
		return ResponseEntity.ok(laminas);
	}

	@PostMapping
	public ResponseEntity<?> crearLaminaEnAlbum(@PathVariable Long idAlbum, @RequestBody Lamina laminaPedido) {
		Usuario usuarioActual = obtenerUsuarioActual();
		Optional<Album> posibleAlbum = repositorioAlbum.findByIdAndPropietarioUsername(idAlbum,
				usuarioActual.getUsername());
		if (posibleAlbum.isEmpty()) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND)
					.body(Map.of("mensaje", "Álbum no encontrado o no pertenece al usuario autenticado"));
		}

		Album album = posibleAlbum.get();

		laminaPedido.asignarAlbum(album);
		if (laminaPedido.obtenerEstado() == null) {
			laminaPedido.asignarEstado(EstadoLamina.COMPLETA);
		}
		if (laminaPedido.obtenerCantidadRepetidas() == null) {
			laminaPedido.asignarCantidadRepetidas(0);
		}

		Lamina laminaGuardada = repositorioLamina.save(laminaPedido);
		return ResponseEntity.status(HttpStatus.CREATED).body(laminaGuardada);
	}

	@PostMapping("/lote")
	public ResponseEntity<?> crearLaminasEnLote(@PathVariable Long idAlbum, @RequestBody List<Lamina> laminasPedido) {
		Usuario usuarioActual = obtenerUsuarioActual();
		Optional<Album> posibleAlbum = repositorioAlbum.findByIdAndPropietarioUsername(idAlbum,
				usuarioActual.getUsername());
		if (posibleAlbum.isEmpty()) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND)
					.body(Map.of("mensaje", "Álbum no encontrado o no pertenece al usuario autenticado"));
		}

		Album album = posibleAlbum.get();
		List<Lamina> laminasGuardadas = new ArrayList<>();

		for (Lamina laminaPedido : laminasPedido) {
			List<Lamina> laminasExistentes = repositorioLamina.findByAlbumIdAndNumero(idAlbum, laminaPedido.obtenerNumero());

			if (laminasExistentes.isEmpty()) {
				laminaPedido.asignarAlbum(album);
				laminaPedido.asignarEstado(EstadoLamina.COMPLETA);
				laminaPedido.asignarCantidadRepetidas(0);
				Lamina nuevaLamina = repositorioLamina.save(laminaPedido);
				laminasGuardadas.add(nuevaLamina);
			} else {
				Lamina laminaPrincipal = laminasExistentes.get(0);
				Integer cantidadActual = laminaPrincipal.obtenerCantidadRepetidas();
				if (cantidadActual == null) {
					cantidadActual = 0;
				}
				laminaPrincipal.asignarCantidadRepetidas(cantidadActual + 1);
				laminaPrincipal.asignarEstado(EstadoLamina.REPETIDA);
				Lamina laminaActualizada = repositorioLamina.save(laminaPrincipal);
				laminasGuardadas.add(laminaActualizada);
			}
		}

		return ResponseEntity.status(HttpStatus.CREATED).body(laminasGuardadas);
	}

	@GetMapping("/faltantes")
	public ResponseEntity<?> obtenerLaminasFaltantes(@PathVariable Long idAlbum) {
		Usuario usuarioActual = obtenerUsuarioActual();
		Optional<Album> posibleAlbum = repositorioAlbum.findByIdAndPropietarioUsername(idAlbum,
				usuarioActual.getUsername());
		if (posibleAlbum.isEmpty()) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND)
					.body(Map.of("mensaje", "Álbum no encontrado o no pertenece al usuario autenticado"));
		}

		Album album = posibleAlbum.get();
		List<Lamina> laminasDelAlbum = repositorioLamina.findByAlbumId(idAlbum);

		Integer totalLaminas = album.obtenerCantidadTotalLaminas();
		if (totalLaminas == null || totalLaminas <= 0) {
			Integer maxNumero = laminasDelAlbum.stream()
					.map(Lamina::obtenerNumero)
					.filter(Objects::nonNull)
					.max(Comparator.naturalOrder())
					.orElse(0);
			if (maxNumero == 0) {
				return ResponseEntity.ok(new ArrayList<>());
			}
			totalLaminas = maxNumero;
		}

		Set<Integer> numerosExistentes = new HashSet<>();
		for (Lamina lamina : laminasDelAlbum) {
			if (lamina.obtenerNumero() != null) {
				numerosExistentes.add(lamina.obtenerNumero());
			}
		}

		List<Lamina> laminasFaltantes = new ArrayList<>();
		for (int numero = 1; numero <= totalLaminas; numero++) {
			if (!numerosExistentes.contains(numero)) {
				Lamina laminaFaltante = new Lamina();
				laminaFaltante.asignarNumero(numero);
				laminaFaltante.asignarEstado(EstadoLamina.FALTANTE);
				laminaFaltante.asignarCantidadRepetidas(0);
				laminaFaltante.asignarTipo(null);
				laminaFaltante.asignarUrlFoto(null);
				laminasFaltantes.add(laminaFaltante);
			}
		}

		return ResponseEntity.ok(laminasFaltantes);
	}

	@GetMapping("/repetidas")
	public ResponseEntity<?> obtenerLaminasRepetidas(@PathVariable Long idAlbum) {
		Usuario usuarioActual = obtenerUsuarioActual();
		Optional<Album> posibleAlbum = repositorioAlbum.findByIdAndPropietarioUsername(idAlbum,
				usuarioActual.getUsername());
		if (posibleAlbum.isEmpty()) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND)
					.body(Map.of("mensaje", "Álbum no encontrado o no pertenece al usuario autenticado"));
		}
		List<Lamina> laminasRepetidas = repositorioLamina.findByAlbumIdAndEstado(idAlbum, EstadoLamina.REPETIDA);
		return ResponseEntity.ok(laminasRepetidas);
	}
}
