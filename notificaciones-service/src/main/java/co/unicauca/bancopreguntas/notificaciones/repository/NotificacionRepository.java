package co.unicauca.bancopreguntas.notificaciones.repository;

import co.unicauca.bancopreguntas.notificaciones.domain.Notificacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NotificacionRepository extends JpaRepository<Notificacion, Long> {

    @Query("""
            select n from Notificacion n
            where n.usuarioId = :usuarioId or (:rol is not null and n.rol = :rol)
            order by n.fecha desc, n.id desc""")
    List<Notificacion> bandeja(@Param("usuarioId") Long usuarioId, @Param("rol") String rol);
}
