package com.manager.taskmanager.member;

import com.manager.taskmanager.common.CustomException;
import com.manager.taskmanager.common.ErrorCode;
import com.manager.taskmanager.department.DepartmentRepository;
import com.manager.taskmanager.department.entity.Department;
import com.manager.taskmanager.member.dto.*;
import com.manager.taskmanager.member.entity.Member;
import com.manager.taskmanager.member.entity.Position;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final DepartmentRepository departmentRepository;
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final MemberQueryRepository memberQueryRepository;

    // 회원 목록 조건 조회
    @Transactional(readOnly = true)
    public List<MemberListDto> getMemberList(String departmentName, String name, boolean isManager) {
        List<Member> allMemberList =
                memberQueryRepository.getAllMemberList(departmentName, name, isManager);

        List<MemberListDto> memberList = allMemberList.stream()
                .map(member -> MemberListDto.from(member))
                .collect(Collectors.toList());

        return memberList;
    }

    // 회원 가입
    @Transactional
    public void registerMember(MemberRegisterDto dto) {
        if (memberRepository.existsByEmployeeNumber(dto.getEmployeeNumber())) {
            throw new CustomException(ErrorCode.EMPLOYEE_NUMBER_DUPLICATE);
        }

        if (memberRepository.existsByEmail(dto.getEmail())) {
            throw new CustomException(ErrorCode.EMAIL_DUPLICATE);
        }

        if (memberRepository.existsByPhoneNumber(dto.getPhoneNumber())) {
            throw new CustomException(ErrorCode.PHONE_NUMBER_DUPLICATE);
        }

        Department department = departmentRepository.findById(dto.getDepartmentId())
                .orElseThrow(() -> new CustomException(ErrorCode.DEPARTMENT_NOT_FOUND));

        Member member = Member.createMember(
                dto.getEmployeeNumber(), passwordEncoder.encode(dto.getPassword()), dto.getName(),
                dto.getEmail(), dto.getPhoneNumber(), Position.valueOf(dto.getPosition()), department
        );

        memberRepository.save(member);
    }

    // 회원 정보 수정
    @Transactional
    public void updateMember(Long memberId, MemberUpdateDto dto) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        if (!member.getEmail().equals(dto.getEmail())) {
            if (memberRepository.existsByEmail(dto.getEmail())) {
                throw new CustomException(ErrorCode.EMAIL_DUPLICATE);
            }

            member.updateEmail(dto.getEmail());
        }

        if (!member.getPhoneNumber().equals(dto.getPhoneNumber())) {
            if (memberRepository.existsByPhoneNumber(dto.getPhoneNumber())) {
                throw new CustomException(ErrorCode.PHONE_NUMBER_DUPLICATE);
            }

            member.updatePhoneNumber(dto.getPhoneNumber());
        }

        Department department = departmentRepository.findById(dto.getDepartmentId())
                .orElseThrow(() -> new CustomException(ErrorCode.DEPARTMENT_NOT_FOUND));

        member.updatePosition(Position.valueOf(dto.getPosition()));
        member.updateDepartment(department);
    }

    // 회원 비밀번호 수정
    @Transactional
    public void updatePassword(Long memberId, PasswordUpdateDto dto) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        member.updatePassword(passwordEncoder.encode(dto.getPassword()));
    }

    // 회원 비밀번호 초기화(관리자용)
    @Transactional
    public void resetPassword(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < 12; i++) {
            int idx = (int) (Math.random() * chars.length());
            sb.append(chars.charAt(idx));
        }

        String rawPassword = sb.toString();

        member.updatePassword(passwordEncoder.encode(rawPassword));
    }

    // 회원 삭제(실제 삭제X, soft delete)
    @Transactional
    public void deleteMember(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        memberRepository.delete(member);
    }

    // 회원 정보 상세 조회(일반)
    @Transactional(readOnly = true)
    public MemberResponseDto getMemberById(Long memberId) {
        Member member = memberRepository.findByIdAndDeletedAtIsNull(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        MemberResponseDto memberResponseDto = MemberResponseDto.from(member);

        return memberResponseDto;
    }

    // 회원 정보 상세 조회(관리자)
    @Transactional(readOnly = true)
    public MemberResponseDto getMemberByIdForManager(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        MemberResponseDto memberResponseDto = MemberResponseDto.from(member);

        return memberResponseDto;
    }

}