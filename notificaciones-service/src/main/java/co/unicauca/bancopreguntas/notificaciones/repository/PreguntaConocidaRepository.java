package co.unicauca.bancopreguntas.notificaciones.repository;

import co.unicauca.bancopreguntas.notificaciones.domain.PreguntaConocida;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PreguntaConocidaRepository extends JpaRepository<PreguntaConocida, Long> {
}
