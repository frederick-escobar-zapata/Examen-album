package ipss.cl.album.controlador;

import ipss.cl.album.modelo.Album;
import ipss.cl.album.modelo.EstadoLamina;
import ipss.cl.album.modelo.Lamina;
import ipss.cl.album.repositorio.RepositorioAlbum;
import ipss.cl.album.repositorio.RepositorioLamina;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.Comparator;
import java.util.Objects;

@RestController
@RequestMapping("/api/albumes/{idAlbum}/laminas")
public class ControladorLamina {

	private final RepositorioAlbum repositorioAlbum;
	private final RepositorioLamina repositorioLamina;

	public ControladorLamina(RepositorioAlbum repositorioAlbum, RepositorioLamina repositorioLamina) {
		this.repositorioAlbum = repositorioAlbum;
		this.repositorioLamina = repositorioLamina;
	}

	@GetMapping
	public ResponseEntity<List<Lamina>> obtenerLaminasDeAlbum(@PathVariable Long idAlbum) {
		if (!repositorioAlbum.existsById(idAlbum)) {
			return ResponseEntity.notFound().build();
		}
		List<Lamina> laminas = repositorioLamina.findByAlbumId(idAlbum);
		return ResponseEntity.ok(laminas);
	}

	@PostMapping
	public ResponseEntity<Lamina> crearLaminaEnAlbum(@PathVariable Long idAlbum, @RequestBody Lamina laminaPedido) {
		Optional<Album> posibleAlbum = repositorioAlbum.findById(idAlbum);
		if (posibleAlbum.isEmpty()) {
			return ResponseEntity.notFound().build();
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
	public ResponseEntity<List<Lamina>> crearLaminasEnLote(@PathVariable Long idAlbum, @RequestBody List<Lamina> laminasPedido) {
		Optional<Album> posibleAlbum = repositorioAlbum.findById(idAlbum);
		if (posibleAlbum.isEmpty()) {
			return ResponseEntity.notFound().build();
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
	public ResponseEntity<List<Lamina>> obtenerLaminasFaltantes(@PathVariable Long idAlbum) {
		Optional<Album> posibleAlbum = repositorioAlbum.findById(idAlbum);
		if (posibleAlbum.isEmpty()) {
			return ResponseEntity.notFound().build();
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
	public ResponseEntity<List<Lamina>> obtenerLaminasRepetidas(@PathVariable Long idAlbum) {
		if (!repositorioAlbum.existsById(idAlbum)) {
			return ResponseEntity.notFound().build();
		}
		List<Lamina> laminasRepetidas = repositorioLamina.findByAlbumIdAndEstado(idAlbum, EstadoLamina.REPETIDA);
		return ResponseEntity.ok(laminasRepetidas);
	}
}
