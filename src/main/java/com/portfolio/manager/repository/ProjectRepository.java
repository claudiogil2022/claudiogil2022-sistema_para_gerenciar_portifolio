package com.portfolio.manager.repository;

import com.portfolio.manager.domain.Project;
import com.portfolio.manager.domain.ProjectStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.NonNull;

import java.util.Collection;

public interface ProjectRepository extends JpaRepository<Project, Long>, JpaSpecificationExecutor<Project> {

    @EntityGraph(attributePaths = {"manager", "members"})
    @Override
    @NonNull java.util.Optional<Project> findById(@NonNull Long id);

    @EntityGraph(attributePaths = {"manager", "members"})
    @NonNull java.util.List<Project> findAll();

    @Query("select count(distinct p) from Project p left join p.members m where p.status not in :terminalStatuses and (m.id = :memberId or p.manager.id = :memberId)")
    long countActiveProjectsByMemberId(@Param("memberId") Long memberId,
                                       @Param("terminalStatuses") Collection<ProjectStatus> terminalStatuses);

    @Query("select count(distinct p) from Project p left join p.members m where p.status not in :terminalStatuses and p.id <> :projectId and (m.id = :memberId or p.manager.id = :memberId)")
    long countActiveProjectsByMemberIdExcludingProject(@Param("memberId") Long memberId,
                                                       @Param("projectId") Long projectId,
                                                       @Param("terminalStatuses") Collection<ProjectStatus> terminalStatuses);
}

