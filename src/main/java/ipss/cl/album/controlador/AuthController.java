package ipss.cl.album.controlador;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

import ipss.cl.album.modelo.Usuario;
import ipss.cl.album.repositorio.RepositorioUsuario;
import ipss.cl.album.security.JwtUtil;

@Controller
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private RepositorioUsuario repositorioUsuario;

    @Autowired
    private JwtUtil jwtUtil;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @GetMapping("/login")
    public String mostrarLogin() {
        return "login";
        // Con este endpoint yo muestro la vista de inicio de sesión.
    }

    @GetMapping("/register")
    public String mostrarRegistro(Model model) {
        model.addAttribute("user", new Usuario());
        return "registro";
        // Con este endpoint yo preparo el formulario de registro de usuario.
    }

    @PostMapping("/register")
    @ResponseBody
    public ResponseEntity<String> register(@RequestBody Usuario usuario) {
        // Aquí yo registro un nuevo usuario validando que no exista previamente.
        Optional<Usuario> existente = repositorioUsuario.findByUsername(usuario.getUsername());
        if (existente.isPresent()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("El usuario ya existe");
        }
        usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
        repositorioUsuario.save(usuario);
        return ResponseEntity.status(HttpStatus.CREATED).body("Usuario registrado correctamente");
    }

    @PostMapping("/login")
    @ResponseBody
    public ResponseEntity<?> login(@RequestBody Usuario loginRequest, HttpServletResponse response) {
        // Aquí yo valido las credenciales, genero el token y lo envío en una cookie.
        Optional<Usuario> usuarioOpt = repositorioUsuario.findByUsername(loginRequest.getUsername());
        if (usuarioOpt.isPresent()) {
            Usuario usuario = usuarioOpt.get();
            if (passwordEncoder.matches(loginRequest.getPassword(), usuario.getPassword())) {
                String token = jwtUtil.generateToken(usuario.getUsername());

                Cookie cookie = new Cookie("jwtToken", token);
                cookie.setHttpOnly(true);
                cookie.setPath("/");
                response.addCookie(cookie);

                Map<String, String> respuesta = new HashMap<>();
                respuesta.put("token", token);
                return ResponseEntity.ok(respuesta);
            }
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Credenciales incorrectas");
    }

    @GetMapping("/logout")
    public String logout(HttpServletResponse response) {
        // Con este endpoint yo invalido el token y regreso al formulario de login.
        Cookie cookie = new Cookie("jwtToken", "");
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
        return "redirect:/auth/login";
    }

}

