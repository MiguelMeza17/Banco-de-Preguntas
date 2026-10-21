package co.unicauca.bancopreguntas.revision.repository;

import co.unicauca.bancopreguntas.revision.domain.Revision;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RevisionRepository extends JpaRepository<Revision, Long> {

    List<Revision> findByPreguntaId(Long preguntaId);

    List<Revision> findByRevisorId(Long revisorId);
}
