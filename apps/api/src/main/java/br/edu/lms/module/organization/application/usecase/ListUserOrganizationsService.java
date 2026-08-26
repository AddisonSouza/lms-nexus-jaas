package br.edu.lms.module.organization.application.usecase;

import br.edu.lms.module.organization.application.dto.UserOrganizationResponse;
import br.edu.lms.module.organization.application.mapper.UserOrganizationMapper;
import br.edu.lms.module.organization.domain.port.in.ListUserOrganizationsUseCase;
import br.edu.lms.module.organization.domain.port.out.OrganizationMemberRepository;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;

import java.util.List;

@ApplicationScoped
@RequiredArgsConstructor
public class ListUserOrganizationsService implements ListUserOrganizationsUseCase {

    private final OrganizationMemberRepository organizationMemberRepository;
    private final UserOrganizationMapper userOrganizationMapper;

    @Override
    public List<UserOrganizationResponse> execute(String userId) {
        return userOrganizationMapper.toResponseList(
                organizationMemberRepository.findUserOrganizations(userId));
    }
}
