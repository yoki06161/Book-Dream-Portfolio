package com.bookdream.sbb.user;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.bookdream.sbb.DataNotFoundException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    public SiteUser create(Map<String, String> map) {
        // 중복 이메일 검사
        if (userRepository.existsByEmail(map.get("email"))) {
            throw new DataIntegrityViolationException("이미 등록된 이메일입니다.");
        }

        SiteUser user = new SiteUser();
        user.setUsername(map.get("username"));
        user.setEmail(map.get("email"));
        user.setPassword(passwordEncoder.encode(map.get("password")));
        this.userRepository.save(user);
        return user;
    }

    
    public SiteUser getUser(String email) {
        Optional<SiteUser> siteUser = this.userRepository.findByEmail(email);
        if (siteUser.isPresent()) {
            return siteUser.get();
        } else {
            throw new DataNotFoundException("siteuser not found!!");
        }
    }



    public SiteUser getUserByEmail(String email) {
    	Optional<SiteUser> siteUser = this.userRepository.findByEmail(email);
    	if (siteUser.isPresent()) {
            return siteUser.get();
        }else {
            throw new DataNotFoundException("siteuser not found!!");
        }
    }
    
    public boolean checkPassword(String email, String password) {
        Optional<SiteUser> siteUser = userRepository.findByEmail(email);

        // 사용자가 존재하지 않는 경우
        if (!siteUser.isPresent()) {
            return false; // 해당 이메일을 가진 사용자가 없음
        }

        // 데이터베이스에서 조회한 사용자 객체
        SiteUser user = siteUser.get();

        // 입력받은 비밀번호와 데이터베이스의 비밀번호 비교
        return user.getPassword().equals(password);
    }
    
    // 사용자 업데이트 메서드
    public void modifyPassword(SiteUser user, String password) {
    	user.setPassword(passwordEncoder.encode(password));
        this.userRepository.save(user);
    }
    
    public void modifySiteName(SiteUser user, String name) {
        user.setUsername(name);
        user.setLastNameChangeDate(LocalDateTime.now()); // 이름 변경 일시 설정
        this.userRepository.save(user);
    }



    public boolean isSamePassword(SiteUser user, String password){
        return passwordEncoder.matches(password, user.getPassword());
    }
    
    // 사용자 삭제 메서드
    public void deleteUser(SiteUser user) {
        this.userRepository.delete(user);
    }
    
    public boolean emailExists(String email) {
        return userRepository.existsByEmail(email);
    }


	public List<SiteUser> getAllUsers() {
		return userRepository.findAll();
	}


	public SiteUser getUserById(Long id) {
		Optional<SiteUser> siteUser = this.userRepository.findById(id);
    	if (siteUser.isPresent()) {
            return siteUser.get();
        }else {
            throw new DataNotFoundException("siteuser not found!!");
        }
	}


	public void modifyEmailOrRole(SiteUser user, String email, String role) {
        user.setEmail(email);
        user.setRole(role);
        this.userRepository.save(user);
	}
}