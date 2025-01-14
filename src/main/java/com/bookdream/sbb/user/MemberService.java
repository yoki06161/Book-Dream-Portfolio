package com.bookdream.sbb.user;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@Transactional
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    public MemberService(MemberRepository memberRepository, PasswordEncoder passwordEncoder) {
        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public boolean checkLoginIdDuplicate(String loginId) {
        return memberRepository.existsByLoginId(loginId);
    }

    public void join(JoinRequest joinRequest) {
        memberRepository.save(joinRequest.toEntity());
    }

    public void securityJoin(JoinRequest joinRequest) {
        if (memberRepository.existsByLoginId(joinRequest.getLoginId())) {
            return;
        }

        joinRequest.setPassword(passwordEncoder.encode(joinRequest.getPassword()));
        memberRepository.save(joinRequest.toEntity());
    }


    public Member getLoginMemberById(Long memberId) {
        if (memberId == null) return null;

        Optional<Member> findMember = memberRepository.findById(memberId);
        return findMember.orElse(null);
    }

    public Member getLoginMemberByLoginId(String loginId) {
        if (loginId == null) return null;

        return memberRepository.findByLoginId(loginId);
    }
    
    public void modifySocialName(Member member, String name) {
    	member.setName(name);
    	member.setLastNameChangeDate(LocalDateTime.now()); // 이름 변경 일시 설정
    	this.memberRepository.save(member);
    }
    
    // 사용자 삭제 메서드
    public void deleteUser(Member member) {
        this.memberRepository.delete(member);
    }
}