package ipss.cl.album.modelo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Lamina {

	// Con esta entidad yo modelo una lámina dentro de un álbum de colección.

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private Integer numero;

	private String tipo;

	@Enumerated(EnumType.STRING)
	private EstadoLamina estado;

	private Integer cantidadRepetidas;

	private String urlFoto;

	@ManyToOne
	@JoinColumn(name = "album_id")
	@JsonIgnore
	private Album album;

	public Long obtenerId() {
		return id;
	}

	public void asignarId(Long id) {
		this.id = id;
	}

	public Integer obtenerNumero() {
		return numero;
	}

	public void asignarNumero(Integer numero) {
		this.numero = numero;
	}

	public String obtenerTipo() {
		return tipo;
	}

	public void asignarTipo(String tipo) {
		this.tipo = tipo;
	}

	public EstadoLamina obtenerEstado() {
		return estado;
	}

	public void asignarEstado(EstadoLamina estado) {
		this.estado = estado;
	}

	public Integer obtenerCantidadRepetidas() {
		return cantidadRepetidas;
	}

	public void asignarCantidadRepetidas(Integer cantidadRepetidas) {
		this.cantidadRepetidas = cantidadRepetidas;
	}

	public String obtenerUrlFoto() {
		return urlFoto;
	}

	public void asignarUrlFoto(String urlFoto) {
		this.urlFoto = urlFoto;
	}

	public Album obtenerAlbum() {
		return album;
	}

	public void asignarAlbum(Album album) {
		this.album = album;
	}
}
