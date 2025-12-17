package ipss.cl.album.controlador;

import ipss.cl.album.modelo.Album;
import ipss.cl.album.modelo.EstadoLamina;
import ipss.cl.album.modelo.Lamina;
import ipss.cl.album.repositorio.RepositorioAlbum;
import ipss.cl.album.repositorio.RepositorioLamina;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
public class ControladorVistaAlbum {

	private final RepositorioAlbum repositorioAlbum;
	private final RepositorioLamina repositorioLamina;

	public ControladorVistaAlbum(RepositorioAlbum repositorioAlbum, RepositorioLamina repositorioLamina) {
		this.repositorioAlbum = repositorioAlbum;
		this.repositorioLamina = repositorioLamina;
	}

	@GetMapping({"/vista", "/vista/"})
	public String mostrarInicio(Model modelo) {
		List<Album> albumes = repositorioAlbum.findAll();
		modelo.addAttribute("albumes", albumes);
		return "inicio";
	}

	@GetMapping("/vista/albumes/nuevo")
	public String mostrarFormularioNuevoAlbum(Model modelo) {
		Album album = new Album();
		modelo.addAttribute("album", album);
		return "album-formulario";
	}

	@PostMapping("/vista/albumes")
	public String crearAlbumDesdeFormulario(@ModelAttribute("album") Album album) {
		repositorioAlbum.save(album);
		return "redirect:/vista/";
	}

	@GetMapping("/vista/albumes/{id}/editar")
	public String mostrarFormularioEditarAlbum(@PathVariable Long id, Model modelo) {
		Optional<Album> posibleAlbum = repositorioAlbum.findById(id);
		if (posibleAlbum.isEmpty()) {
			return "redirect:/vista/";
		}
		modelo.addAttribute("album", posibleAlbum.get());
		return "album-formulario";
	}

	@PostMapping("/vista/albumes/{id}/editar")
	public String actualizarAlbumDesdeFormulario(@PathVariable Long id, @ModelAttribute("album") Album albumFormulario) {
		Optional<Album> posibleAlbum = repositorioAlbum.findById(id);
		if (posibleAlbum.isEmpty()) {
			return "redirect:/vista/";
		}
		Album albumExistente = posibleAlbum.get();
		albumExistente.asignarNombre(albumFormulario.obtenerNombre());
		albumExistente.asignarAnio(albumFormulario.obtenerAnio());
		albumExistente.asignarFechaLanzamiento(albumFormulario.obtenerFechaLanzamiento());
		albumExistente.asignarDescripcion(albumFormulario.obtenerDescripcion());
		albumExistente.asignarCantidadTotalLaminas(albumFormulario.obtenerCantidadTotalLaminas());
		repositorioAlbum.save(albumExistente);
		return "redirect:/vista/";
	}

	@PostMapping("/vista/albumes/{id}/eliminar")
	public String eliminarAlbumDesdeFormulario(@PathVariable Long id) {
		if (repositorioAlbum.existsById(id)) {
			repositorioAlbum.deleteById(id);
		}
		return "redirect:/vista/";
	}

	@GetMapping("/vista/albumes/{id}")
	public String mostrarDetalleAlbum(@PathVariable Long id, Model modelo) {
		Optional<Album> posibleAlbum = repositorioAlbum.findById(id);
		if (posibleAlbum.isEmpty()) {
			return "redirect:/vista/";
		}
		Album album = posibleAlbum.get();
		Integer totalLaminas = album.obtenerCantidadTotalLaminas();
		modelo.addAttribute("album", album);
		return "album";
	}

	@GetMapping("/vista/albumes/{id}/laminas/faltantes")
	public String mostrarLaminasFaltantes(@PathVariable Long id,
			@RequestParam(name = "pagina", defaultValue = "0") int pagina,
			Model modelo) {
		Optional<Album> posibleAlbum = repositorioAlbum.findById(id);
		if (posibleAlbum.isEmpty()) {
			return "redirect:/vista/";
		}
		Album album = posibleAlbum.get();
		List<Lamina> laminasDelAlbum = repositorioLamina.findByAlbumId(id);

		Integer totalLaminas = album.obtenerCantidadTotalLaminas();
		if (totalLaminas == null || totalLaminas <= 0) {
			Integer maxNumero = laminasDelAlbum.stream()
					.map(Lamina::obtenerNumero)
					.filter(java.util.Objects::nonNull)
					.max(java.util.Comparator.naturalOrder())
					.orElse(0);
			if (maxNumero == 0) {
				modelo.addAttribute("album", album);
				modelo.addAttribute("laminasFaltantes", new ArrayList<Lamina>());
				return "laminas-faltantes";
			}
			totalLaminas = maxNumero;
		}

		java.util.Set<Integer> numerosExistentes = new java.util.HashSet<>();
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

		int tamanoPagina = 10;
		int totalLaminasFaltantes = laminasFaltantes.size();
		int totalPaginas = (int) Math.ceil(totalLaminasFaltantes / (double) tamanoPagina);
		if (pagina < 0) {
			pagina = 0;
		} else if (totalPaginas > 0 && pagina >= totalPaginas) {
			pagina = totalPaginas - 1;
		}
		int desde = pagina * tamanoPagina;
		int hasta = Math.min(desde + tamanoPagina, totalLaminasFaltantes);
		List<Lamina> paginaFaltantes = laminasFaltantes.subList(desde, hasta);

		modelo.addAttribute("album", album);
		modelo.addAttribute("laminasFaltantes", paginaFaltantes);
		modelo.addAttribute("paginaActual", pagina);
		modelo.addAttribute("totalPaginas", totalPaginas);
		return "laminas-faltantes";
	}

	@GetMapping("/vista/albumes/{id}/laminas")
	public String mostrarLaminasDeAlbum(@PathVariable Long id,
			@RequestParam(name = "pagina", defaultValue = "0") int pagina,
			Model modelo) {
		Optional<Album> posibleAlbum = repositorioAlbum.findById(id);
		if (posibleAlbum.isEmpty()) {
			return "redirect:/vista/";
		}
		Album album = posibleAlbum.get();
		int tamanoPagina = 10;
		Pageable pageable = PageRequest.of(pagina, tamanoPagina, Sort.by("numero").ascending());
		Page<Lamina> paginaLaminas = repositorioLamina.findByAlbumId(id, pageable);
		modelo.addAttribute("album", album);
		modelo.addAttribute("laminas", paginaLaminas.getContent());
		modelo.addAttribute("paginaActual", pagina);
		modelo.addAttribute("totalPaginas", paginaLaminas.getTotalPages());
		return "laminas";
	}

	@GetMapping("/vista/albumes/{id}/laminas/repetidas")
	public String mostrarLaminasRepetidas(@PathVariable Long id,
			@RequestParam(name = "pagina", defaultValue = "0") int pagina,
			Model modelo) {
		Optional<Album> posibleAlbum = repositorioAlbum.findById(id);
		if (posibleAlbum.isEmpty()) {
			return "redirect:/vista/";
		}
		Album album = posibleAlbum.get();
		int tamanoPagina = 10;
		Pageable pageable = PageRequest.of(pagina, tamanoPagina, Sort.by("numero").ascending());
		Page<Lamina> paginaRepetidas = repositorioLamina.findByAlbumIdAndEstado(id, EstadoLamina.REPETIDA, pageable);
		modelo.addAttribute("album", album);
		modelo.addAttribute("laminasRepetidas", paginaRepetidas.getContent());
		modelo.addAttribute("paginaActual", pagina);
		modelo.addAttribute("totalPaginas", paginaRepetidas.getTotalPages());
		return "laminas-repetidas";
	}

	@GetMapping("/vista/albumes/{id}/laminas/nueva")
	public String mostrarFormularioNuevaLamina(@PathVariable Long id, Model modelo) {
		Optional<Album> posibleAlbum = repositorioAlbum.findById(id);
		if (posibleAlbum.isEmpty()) {
			return "redirect:/vista/";
		}
		Album album = posibleAlbum.get();
		Lamina lamina = new Lamina();
		lamina.asignarEstado(EstadoLamina.COMPLETA);
		lamina.asignarTipo("NORMAL");
		lamina.asignarCantidadRepetidas(0);
		modelo.addAttribute("album", album);
		modelo.addAttribute("lamina", lamina);
		modelo.addAttribute("modoEdicion", false);
		return "lamina-formulario";
	}

	@PostMapping("/vista/albumes/{id}/laminas")
	public String crearLaminaDesdeFormulario(@PathVariable Long id, @ModelAttribute("lamina") Lamina lamina, Model modelo) {
		Optional<Album> posibleAlbum = repositorioAlbum.findById(id);
		if (posibleAlbum.isEmpty()) {
			return "redirect:/vista/";
		}
		Album album = posibleAlbum.get();

		Integer numero = lamina.obtenerNumero();
		Integer totalLaminas = album.obtenerCantidadTotalLaminas();
		if (totalLaminas != null && totalLaminas > 0 && numero != null && numero > totalLaminas) {
			modelo.addAttribute("album", album);
			modelo.addAttribute("lamina", lamina);
			modelo.addAttribute("modoEdicion", false);
			modelo.addAttribute("mensajeError",
					"El número de la lámina no puede ser mayor que la cantidad total de láminas del álbum (" + totalLaminas + ")");
			return "lamina-formulario";
		}

		if (numero != null) {
			List<Lamina> laminasExistentes = repositorioLamina.findByAlbumIdAndNumero(id, numero);
			if (!laminasExistentes.isEmpty()) {
				Lamina laminaPrincipal = laminasExistentes.get(0);
				Integer cantidadActual = laminaPrincipal.obtenerCantidadRepetidas();
				if (cantidadActual == null) {
					cantidadActual = 0;
				}
				laminaPrincipal.asignarCantidadRepetidas(cantidadActual + 1);
				laminaPrincipal.asignarEstado(EstadoLamina.REPETIDA);
				repositorioLamina.save(laminaPrincipal);
				return "redirect:/vista/albumes/" + id + "/laminas";
			}
		}

		lamina.asignarAlbum(album);
		lamina.asignarId(null);
		lamina.asignarEstado(EstadoLamina.COMPLETA);
		if (lamina.obtenerCantidadRepetidas() == null) {
			lamina.asignarCantidadRepetidas(0);
		}
		repositorioLamina.save(lamina);
		return "redirect:/vista/albumes/" + id + "/laminas";
	}

	@GetMapping("/vista/albumes/{idAlbum}/laminas/{idLamina}/editar")
	public String mostrarFormularioEditarLamina(@PathVariable Long idAlbum, @PathVariable Long idLamina, Model modelo) {
		Optional<Album> posibleAlbum = repositorioAlbum.findById(idAlbum);
		Optional<Lamina> posibleLamina = repositorioLamina.findById(idLamina);
		if (posibleAlbum.isEmpty() || posibleLamina.isEmpty()) {
			return "redirect:/vista/";
		}
		modelo.addAttribute("album", posibleAlbum.get());
		modelo.addAttribute("lamina", posibleLamina.get());
		modelo.addAttribute("modoEdicion", true);
		return "lamina-formulario";
	}

	@PostMapping("/vista/albumes/{idAlbum}/laminas/{idLamina}/editar")
	public String actualizarLaminaDesdeFormulario(@PathVariable Long idAlbum, @PathVariable Long idLamina,
			@ModelAttribute("lamina") Lamina laminaFormulario) {
		Optional<Lamina> posibleLamina = repositorioLamina.findById(idLamina);
		if (posibleLamina.isEmpty()) {
			return "redirect:/vista/albumes/" + idAlbum + "/laminas";
		}
		Lamina laminaExistente = posibleLamina.get();
		laminaExistente.asignarNumero(laminaFormulario.obtenerNumero());
		laminaExistente.asignarTipo(laminaFormulario.obtenerTipo());
		laminaExistente.asignarCantidadRepetidas(laminaFormulario.obtenerCantidadRepetidas());
		laminaExistente.asignarUrlFoto(laminaFormulario.obtenerUrlFoto());
		Integer cantidad = laminaExistente.obtenerCantidadRepetidas();
		if (cantidad != null && cantidad > 0) {
			laminaExistente.asignarEstado(EstadoLamina.REPETIDA);
		} else {
			laminaExistente.asignarEstado(EstadoLamina.COMPLETA);
		}
		repositorioLamina.save(laminaExistente);
		return "redirect:/vista/albumes/" + idAlbum + "/laminas";
	}

	@PostMapping("/vista/albumes/{idAlbum}/laminas/{idLamina}/eliminar")
	public String eliminarLaminaDesdeFormulario(@PathVariable Long idAlbum, @PathVariable Long idLamina) {
		if (repositorioLamina.existsById(idLamina)) {
			repositorioLamina.deleteById(idLamina);
		}
		return "redirect:/vista/albumes/" + idAlbum + "/laminas";
	}

	@GetMapping("/vista/albumes/{id}/laminas/lote")
	public String mostrarFormularioLote(@PathVariable Long id, Model modelo) {
		Optional<Album> posibleAlbum = repositorioAlbum.findById(id);
		if (posibleAlbum.isEmpty()) {
			return "redirect:/vista/";
		}
		Album album = posibleAlbum.get();
		modelo.addAttribute("album", album);
		modelo.addAttribute("numerosLaminas", "");
		return "laminas-lote";
	}

	@PostMapping("/vista/albumes/{id}/laminas/lote")
	public String procesarFormularioLote(@PathVariable Long id,
			@RequestParam("numerosLaminas") String numerosLaminas,
			@RequestParam("tipoLamina") String tipoLamina) {
		Optional<Album> posibleAlbum = repositorioAlbum.findById(id);
		if (posibleAlbum.isEmpty()) {
			return "redirect:/vista/";
		}
		Album album = posibleAlbum.get();
		Integer totalLaminas = album.obtenerCantidadTotalLaminas();

		String[] partes = numerosLaminas.split("[;,\\s]+");
		List<Lamina> laminasAGuardar = new ArrayList<>();
		// Para manejar números repetidos dentro del mismo lote sin crear filas duplicadas
		java.util.Map<Integer, Lamina> nuevasPorNumero = new java.util.HashMap<>();
		for (String parte : partes) {
			if (parte.isBlank()) {
				continue;
			}
			try {
				Integer numero = Integer.parseInt(parte.trim());
				if (totalLaminas != null && totalLaminas > 0 && numero != null && numero > totalLaminas) {
					// Si el número supera la cantidad total de láminas del álbum, lo ignoramos
					continue;
				}
				// Primero revisamos si ya creamos una nueva lámina con este número en este mismo lote
				Lamina laminaNuevaEnLote = nuevasPorNumero.get(numero);
				if (laminaNuevaEnLote != null) {
					Integer cantidadActual = laminaNuevaEnLote.obtenerCantidadRepetidas();
					if (cantidadActual == null) {
						cantidadActual = 0;
					}
					laminaNuevaEnLote.asignarCantidadRepetidas(cantidadActual + 1);
					laminaNuevaEnLote.asignarEstado(EstadoLamina.REPETIDA);
					continue;
				}
				// Si no hay nueva en el lote, buscamos en la base de datos
				List<Lamina> laminasExistentes = repositorioLamina.findByAlbumIdAndNumero(id, numero);
				if (!laminasExistentes.isEmpty()) {
					Lamina laminaPrincipal = laminasExistentes.get(0);
					Integer cantidadActual = laminaPrincipal.obtenerCantidadRepetidas();
					if (cantidadActual == null) {
						cantidadActual = 0;
					}
					laminaPrincipal.asignarCantidadRepetidas(cantidadActual + 1);
					laminaPrincipal.asignarEstado(EstadoLamina.REPETIDA);
					repositorioLamina.save(laminaPrincipal);
				} else {
					Lamina lamina = new Lamina();
					lamina.asignarNumero(numero);
					lamina.asignarTipo(tipoLamina);
					lamina.asignarEstado(EstadoLamina.COMPLETA);
					lamina.asignarCantidadRepetidas(0);
					lamina.asignarAlbum(album);
					laminasAGuardar.add(lamina);
					nuevasPorNumero.put(numero, lamina);
				}
			} catch (NumberFormatException e) {
				// En esta parte yo decido ignorar valores no numéricos
			}
		}

		for (Lamina lamina : laminasAGuardar) {
			repositorioLamina.save(lamina);
		}

		return "redirect:/vista/albumes/" + id + "/laminas";
	}
}
