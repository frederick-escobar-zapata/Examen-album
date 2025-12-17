package ipss.cl.album.modelo;

import ipss.cl.album.modelo.Lamina;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Album {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String nombre;

	private Integer anio;

	private LocalDate fechaLanzamiento;

	private String descripcion;

	private Integer cantidadTotalLaminas;

	@OneToMany(mappedBy = "album", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<Lamina> laminas = new ArrayList<>();

	public Long obtenerId() {
		return id;
	}

	public String obtenerNombre() {
		return nombre;
	}

	public void asignarNombre(String nombre) {
		this.nombre = nombre;
	}

	public Integer obtenerAnio() {
		return anio;
	}

	public void asignarAnio(Integer anio) {
		this.anio = anio;
	}

	public LocalDate obtenerFechaLanzamiento() {
		return fechaLanzamiento;
	}

	public void asignarFechaLanzamiento(LocalDate fechaLanzamiento) {
		this.fechaLanzamiento = fechaLanzamiento;
	}

	public String obtenerDescripcion() {
		return descripcion;
	}

	public void asignarDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}

	public Integer obtenerCantidadTotalLaminas() {
		return cantidadTotalLaminas;
	}

	public void asignarCantidadTotalLaminas(Integer cantidadTotalLaminas) {
		this.cantidadTotalLaminas = cantidadTotalLaminas;
	}

	public List<Lamina> obtenerLaminas() {
		return laminas;
	}

	public void asignarLaminas(List<Lamina> laminas) {
		this.laminas = laminas;
	}
}
