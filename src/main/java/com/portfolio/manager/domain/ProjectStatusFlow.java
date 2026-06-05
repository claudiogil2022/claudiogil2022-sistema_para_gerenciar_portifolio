package com.portfolio.manager.domain;

import java.util.EnumMap;
import java.util.Map;

public final class ProjectStatusFlow {

    private static final Map<ProjectStatus, ProjectStatus> NEXT = new EnumMap<ProjectStatus, ProjectStatus>(ProjectStatus.class);

    static {
        NEXT.put(ProjectStatus.EM_ANALISE, ProjectStatus.ANALISE_REALIZADA);
        NEXT.put(ProjectStatus.ANALISE_REALIZADA, ProjectStatus.ANALISE_APROVADA);
        NEXT.put(ProjectStatus.ANALISE_APROVADA, ProjectStatus.INICIADO);
        NEXT.put(ProjectStatus.INICIADO, ProjectStatus.PLANEJADO);
        NEXT.put(ProjectStatus.PLANEJADO, ProjectStatus.EM_ANDAMENTO);
        NEXT.put(ProjectStatus.EM_ANDAMENTO, ProjectStatus.ENCERRADO);
    }

    private ProjectStatusFlow() {
    }

    public static ProjectStatus nextOf(ProjectStatus current) {
        return NEXT.get(current);
    }

    public static boolean isValidTransition(ProjectStatus current, ProjectStatus target) {
        if (current == ProjectStatus.CANCELADO) {
            return false;
        }
        if (target == ProjectStatus.CANCELADO) {
            return true;
        }
        return target != null && target.equals(NEXT.get(current));
    }

    public static boolean isDeletionRestricted(ProjectStatus status) {
        return status == ProjectStatus.INICIADO || status == ProjectStatus.EM_ANDAMENTO || status == ProjectStatus.ENCERRADO;
    }
}

