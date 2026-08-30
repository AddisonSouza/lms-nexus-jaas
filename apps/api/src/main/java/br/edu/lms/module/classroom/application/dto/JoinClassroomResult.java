package br.edu.lms.module.classroom.application.dto;

/**
 * Resultado do ingresso em turma. O {@code alreadyMember} separa quem acabou de
 * entrar (201) de quem já era membro (200), como documenta o API_CONTRACT.
 */
public record JoinClassroomResult(ClassroomResponse classroom, boolean alreadyMember) {

    public static JoinClassroomResult joined(ClassroomResponse classroom) {
        return new JoinClassroomResult(classroom, false);
    }

    public static JoinClassroomResult alreadyMember(ClassroomResponse classroom) {
        return new JoinClassroomResult(classroom, true);
    }
}
