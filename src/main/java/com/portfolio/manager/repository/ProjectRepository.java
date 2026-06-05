package com.portfolio.manager.repository;

import com.portfolio.manager.domain.Project;
import com.portfolio.manager.domain.ProjectStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;

public interface ProjectRepository extends JpaRepository<Project, Long>, JpaSpecificationExecutor<Project> {

    @EntityGraph(attributePaths = {"manager", "members"})
    @Override
    java.util.Optional<Project> findById(Long id);

    @EntityGraph(attributePaths = {"manager", "members"})
    java.util.List<Project> findAll();

    @Query("select count(p) from Project p join p.members m where m.id = :memberId and p.status not in :terminalStatuses")
    long countActiveProjectsByMemberId(@Param("memberId") Long memberId,
                                       @Param("terminalStatuses") Collection<ProjectStatus> terminalStatuses);

    @Query("select count(p) from Project p join p.members m where m.id = :memberId and p.status not in :terminalStatuses and p.id <> :projectId")
    long countActiveProjectsByMemberIdExcludingProject(@Param("memberId") Long memberId,
                                                       @Param("projectId") Long projectId,
                                                       @Param("terminalStatuses") Collection<ProjectStatus> terminalStatuses);
}

