package dev.ioexception.community.repository;

import dev.ioexception.community.entity.ModifyWarning;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ModifyWarningRepository extends JpaRepository<ModifyWarning, Long> {
    @EntityGraph(value = "ModifyWarning.articleUser", type = EntityGraph.EntityGraphType.LOAD)
    List<ModifyWarning> findAllByUserId(Long userId);
}
